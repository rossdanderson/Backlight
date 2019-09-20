#include "../public/capture.h"
#include "../include/captureUtils.h"
#include <sstream>
#include <iostream>

using namespace std;

capture::capture(std::shared_ptr<class logger> logger) {
    this->logger = std::move(logger);
}

void capture::init() {
    MONITORINFO monitorInfo;
    monitorInfo.cbSize = sizeof(MONITORINFO);

    CComPtr<IDXGIFactory1> dxgiFactory1;
    SUCCESS_OR_THROW("Unable to create DXGI Factory",
                     CreateDXGIFactory1(__uuidof(IDXGIFactory1), reinterpret_cast<void **>(&dxgiFactory1)));

    CComPtr<IDXGIAdapter1> adapter1 = nullptr;
    CComPtr<IDXGIOutput> output = nullptr;
    auto i = 0;
    CComPtr<IDXGIAdapter1> testAdapter1 = nullptr;
    while (output == nullptr && dxgiFactory1->EnumAdapters1(i, &testAdapter1) != DXGI_ERROR_NOT_FOUND) {
        DXGI_ADAPTER_DESC1 adapterDesc;
        SUCCESS_OR_THROW("Unable to access adapter description", testAdapter1->GetDesc1(&adapterDesc));

        auto j = 0;
        CComPtr<IDXGIOutput> testOutput = nullptr;
        while (output == nullptr && testAdapter1->EnumOutputs(j, &testOutput) != DXGI_ERROR_NOT_FOUND) {
            DXGI_OUTPUT_DESC outputDesc;
            SUCCESS_OR_THROW("Unable to access output description", testOutput->GetDesc(&outputDesc));

            GetMonitorInfo(outputDesc.Monitor, &monitorInfo);
            auto isPrimary = (monitorInfo.dwFlags & MONITORINFOF_PRIMARY) != 0; // NOLINT(hicpp-signed-bitwise)

            if (isPrimary && outputDesc.AttachedToDesktop) {
                adapter1 = testAdapter1;
                output = testOutput;
            }
            testOutput.Release();
            ++j;
        }

        testAdapter1.Release();
        ++i;
    }

    if (adapter1 == nullptr || output == nullptr) throw invalid_argument("No adapter found");

    DXGI_ADAPTER_DESC1 adapterDesc;
    SUCCESS_OR_THROW("Unable to access adapter description", adapter1->GetDesc1(&adapterDesc));

    DXGI_OUTPUT_DESC outputDesc;
    SUCCESS_OR_THROW("Unable to access output description", output->GetDesc(&outputDesc));

    wostringstream infoStream;
    infoStream << "Adapter output found:"
               << " Description='" << adapterDesc.Description
               << "', DeviceId='" << adapterDesc.DeviceId
               << "', DeviceName='" << outputDesc.DeviceName
               << "', Rotation='" << outputDesc.Rotation
               << "', DesktopCoordinates='(" << outputDesc.DesktopCoordinates.left
               << "," << outputDesc.DesktopCoordinates.top
               << "),(" << outputDesc.DesktopCoordinates.right
               << "," << outputDesc.DesktopCoordinates.bottom
               << ")'";
    logger->info(infoStream.str());

    CComPtr<ID3D11Device> device;
    CComPtr<ID3D11DeviceContext> deviceContext;
    auto featureLevel = D3D_FEATURE_LEVEL_9_1;
    SUCCESS_OR_THROW(
            "Unable to create D3D device",
            D3D11CreateDevice(
                    adapter1,
                    D3D_DRIVER_TYPE_UNKNOWN,
                    nullptr,
                    0,
                    nullptr,
                    0,
                    D3D11_SDK_VERSION,
                    &device,
                    &featureLevel,
                    &deviceContext
            )
    );

    CComQIPtr<IDXGIOutput1> output1(output);
    if (output1 == nullptr) throw invalid_argument("output does not implement IDXGIOutput1");

    CComQIPtr<IDXGIDevice1> device1(device);
    if (device1 == nullptr) throw invalid_argument("device does not implement IDXGIDevice1");

    CComPtr<IDXGIOutputDuplication> outputDuplication;
    SUCCESS_OR_THROW(
            "Unable to duplicate output",
            output1->DuplicateOutput(
                    device1,
                    &outputDuplication
            )
    );

    this->adapter1 = adapter1;
    this->output1 = output1;
    this->device = device;
    this->deviceContext = deviceContext;
    this->outputDuplication = outputDuplication;
    CopyRect(&dimensions, &outputDesc.DesktopCoordinates);
}

rectangle capture::getDimensions() {
    return rectangle(
            point(dimensions.top, dimensions.left),
            point(dimensions.bottom, dimensions.right)
    );
}


shared_ptr<captured> capture::getOutputBits() {
    try {
        RECT outputDimensions;
        CopyRect(&outputDimensions, &dimensions);
        long width = outputDimensions.right - outputDimensions.left;
        long height = outputDimensions.bottom - outputDimensions.top;

        size_t bufferSize = width * height * 4;
        auto buffer = make_unique<unsigned char[]>(bufferSize);

        wostringstream infoStream;
        infoStream << "Buffer size " << width << " * " << height << " * 4 = " << " " << bufferSize;
        logger->info(infoStream.str());

        DXGI_OUTPUT_DESC outDesc;
        SUCCESS_OR_THROW(
                "Unable to access output description",
                output1->GetDesc(&outDesc)
        );

        CComPtr<IDXGISurface1> dxgiSurface1 = acquireNextFrame();

        DXGI_MAPPED_RECT mappedRect;
        dxgiSurface1->Map(&mappedRect, DXGI_MAP_READ);

        RECT desktopDimensions = outDesc.DesktopCoordinates;
        long desktopWidth = desktopDimensions.right - desktopDimensions.left;
        long desktopHeight = desktopDimensions.bottom - desktopDimensions.top;

        OffsetRect(&desktopDimensions, -outputDimensions.left, -outputDimensions.top);

        int mapPitchPixels = mappedRect.Pitch / 4;

        switch (outDesc.Rotation) {
            case DXGI_MODE_ROTATION_UNSPECIFIED:
            case DXGI_MODE_ROTATION_IDENTITY: {
                for (long y = 0; y < desktopHeight; y++) {
                    auto yOffset = desktopDimensions.left + (y + desktopDimensions.top) * width;
                    for (long x = 0; x < desktopWidth; x++) {
                        long offset = yOffset + x;
                        long sourceOffset = desktopWidth - x - 1 + mapPitchPixels * (desktopHeight - y - 1);
                        BYTE source = *(mappedRect.pBits + sourceOffset);
                        buffer[offset] = source;
                    }
                }
            }
                break;
            case DXGI_MODE_ROTATION_ROTATE90: {
                for (long y = 0; y < desktopHeight; y++) {
                    for (long x = 0; x < desktopWidth; x++) {
                        long offset = desktopDimensions.left + (y + desktopDimensions.top) * width + x;
                        long sourceOffset = y + mapPitchPixels * (desktopWidth - x - 1);
                        BYTE source = *(mappedRect.pBits + sourceOffset);
                        buffer[offset] = source;
                    }
                }
            }
                break;
            case DXGI_MODE_ROTATION_ROTATE180: {
                for (long y = 0; y < desktopHeight; y++) {
                    auto yOffset = desktopDimensions.left + (y + desktopDimensions.top) * width;
                    for (long x = 0; x < desktopWidth; x++) {
                        long offset = yOffset + x;
                        long sourceOffset = desktopWidth - x - 1 + mapPitchPixels * (desktopHeight - y - 1);
                        BYTE source = *(mappedRect.pBits + sourceOffset);
                        buffer[offset] = source;
                    }
                }
            }
                break;
            case DXGI_MODE_ROTATION_ROTATE270: {
                for (long y = 0; y < desktopHeight; y++) {
                    long yOffset = desktopDimensions.left + (y + desktopDimensions.top) * width;
                    for (long x = 0; x < desktopWidth; x++) {
                        long offset = yOffset + x;
                        long sourceOffset = (desktopHeight - y - 1) + mapPitchPixels * x;
                        BYTE source = *(mappedRect.pBits + sourceOffset);
                        buffer[offset] = source;
                    }
                }
            }
                break;
        }
        dxgiSurface1->Unmap();
        outputDuplication->ReleaseFrame();


        wostringstream infoStream1;
        infoStream1 << "Released";
        logger->info(infoStream1.str());
        return make_shared<class captured>(buffer.release());
    } catch (exception &e) {
        cout << e.what() << endl;
        return make_shared<class captured>();
    }
}

CComPtr<IDXGISurface1> capture::acquireNextFrame() {
    DXGI_OUTDUPL_FRAME_INFO frameInfo;
    CComPtr<IDXGIResource> dxgiResource;
    SUCCESS_OR_THROW(
            "Could not acquire next frame",
            outputDuplication->AcquireNextFrame(INFINITE, &frameInfo, &dxgiResource)
    );

    CComQIPtr<ID3D11Texture2D> fromTexture2D(dxgiResource);

    D3D11_TEXTURE2D_DESC fromTexture2DDesc;
    fromTexture2D->GetDesc(&fromTexture2DDesc);

    D3D11_TEXTURE2D_DESC toTexture2DDesc;
    ZeroMemory(&toTexture2DDesc, sizeof(toTexture2DDesc));
    toTexture2DDesc.Width = fromTexture2DDesc.Width;
    toTexture2DDesc.Height = fromTexture2DDesc.Height;
    toTexture2DDesc.MipLevels = 1;
    toTexture2DDesc.ArraySize = 1;
    toTexture2DDesc.SampleDesc.Count = 1;
    toTexture2DDesc.SampleDesc.Quality = 0;
    toTexture2DDesc.Usage = D3D11_USAGE_STAGING;
    toTexture2DDesc.Format = fromTexture2DDesc.Format;
    toTexture2DDesc.BindFlags = 0;
    toTexture2DDesc.CPUAccessFlags = D3D11_CPU_ACCESS_READ;
    toTexture2DDesc.MiscFlags = 0;

    CComPtr<ID3D11Texture2D> toTexture2D = nullptr;
    SUCCESS_OR_THROW("", device->CreateTexture2D(&toTexture2DDesc, nullptr, &toTexture2D));

    deviceContext->CopyResource(toTexture2D, fromTexture2D);

    return CComQIPtr<IDXGISurface1>(toTexture2D);
}
