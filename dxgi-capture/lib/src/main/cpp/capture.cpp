#include "../public/capture.h"
#include <iostream>
#include <sstream>
#include <cmath>

using namespace std;

HRESULT capture::init(long sampleStep, size_t *outBufferSize) {
    reset();

    MONITORINFO monitorInfo;
    monitorInfo.cbSize = sizeof(MONITORINFO);

    CComPtr<IDXGIFactory1> dxgiFactory1;

    HRESULT hr = CreateDXGIFactory1(__uuidof(IDXGIFactory1), reinterpret_cast<void **>(&dxgiFactory1));
    if (FAILED(hr)) {
        printf("Unable to create DXGI Factory");
        return hr;
    }

    CComPtr<IDXGIAdapter1> adapter1 = nullptr;
    CComPtr<IDXGIOutput> output = nullptr;
    auto i = 0;
    CComPtr<IDXGIAdapter1> testAdapter1 = nullptr;
    while (output == nullptr && dxgiFactory1->EnumAdapters1(i, &testAdapter1) != DXGI_ERROR_NOT_FOUND) {
        DXGI_ADAPTER_DESC1 adapterDesc;
        hr = testAdapter1->GetDesc1(&adapterDesc);
        if (FAILED(hr)) {
            std::cout << "Unable to access adapter description";
            return hr;
        }

        auto j = 0;
        CComPtr<IDXGIOutput> testOutput = nullptr;
        while (output == nullptr && testAdapter1->EnumOutputs(j, &testOutput) != DXGI_ERROR_NOT_FOUND) {
            DXGI_OUTPUT_DESC outputDesc;
            hr = testOutput->GetDesc(&outputDesc);
            if (FAILED(hr)) {
                std::cout << "Unable to access output description";
                return hr;
            }

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
    hr = adapter1->GetDesc1(&adapterDesc);
    if (FAILED(hr)) {
        std::cout << "Unable to access adapter description";
        return hr;
    }

    DXGI_OUTPUT_DESC outputDesc;
    hr = output->GetDesc(&outputDesc);
    if (FAILED(hr)) {
        std::cout << "Unable to access output description";
        return hr;
    }

    RECT &desktopCoordinates = outputDesc.DesktopCoordinates;

    stringstream infoStream;
    infoStream << "Adapter output found:"
               << " Description='" << adapterDesc.Description
               << "', DeviceId='" << adapterDesc.DeviceId
               << "', DeviceName='" << outputDesc.DeviceName
               << "', Rotation='" << outputDesc.Rotation
               << "', DesktopCoordinates='(" << desktopCoordinates.left
               << "," << desktopCoordinates.top
               << "),(" << desktopCoordinates.right
               << "," << desktopCoordinates.bottom
               << ")'";
    std::cout << infoStream.str();

    CComPtr<ID3D11Device> device;
    CComPtr<ID3D11DeviceContext> deviceContext;
    auto featureLevel = D3D_FEATURE_LEVEL_9_1;

    hr = D3D11CreateDevice(
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
    );

    if (FAILED(hr)) {
        std::cout << "Unable to create D3D device";
        return hr;
    }

    CComQIPtr<IDXGIOutput1> output1(output);
    if (output1 == nullptr) {
        std::cout << "output does not implement IDXGIOutput1";
        return E_FAIL;
    }

    CComQIPtr<IDXGIDevice1> device1(device);
    if (device1 == nullptr) {
        std::cout << "device does not implement IDXGIDevice1";
        return E_FAIL;
    }

    CComPtr<IDXGIOutputDuplication> outputDuplication;

    hr = output1->DuplicateOutput(
            device1,
            &outputDuplication
    );

    if (FAILED(hr)) {
        std::cout << "Unable to duplicate output";
        return hr;
    }

    this->sampleStep = sampleStep;
    this->output1 = output1;
    this->device = device;
    this->deviceContext = deviceContext;
    this->outputDuplication = outputDuplication;
    CopyRect(&dimensions, &desktopCoordinates);
    initialised = true;

    width = (desktopCoordinates.right - desktopCoordinates.left) / sampleStep;
    height = (desktopCoordinates.bottom - desktopCoordinates.top) / sampleStep;
    requiredBufferSize = width * height * 4;

    *outBufferSize = requiredBufferSize;
    return NOERROR;
}

rectangle capture::getDimensions() {
    return {
            {0,     height},
            {width, 0}
    };
}

HRESULT capture::getOutputBits(unsigned char *inoutBuffer, size_t inoutBufferSize) {
    if (!initialised) return E_FAIL;
    if (inoutBufferSize < requiredBufferSize) return E_FAIL;

    DXGI_OUTPUT_DESC outDesc;
    HRESULT hr = output1->GetDesc(&outDesc);
    if (FAILED(hr)) {
        std::cout << "Unable to access output description";
        return hr;
    }

    DXGI_OUTDUPL_FRAME_INFO frameInfo;
    CComPtr<IDXGIResource> dxgiResource;

    hr = outputDuplication->AcquireNextFrame(INFINITE, &frameInfo, &dxgiResource);
    if (FAILED(hr)) {
        std::cout << "Could not acquire next frame";
        return hr;
    }

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
    hr = device->CreateTexture2D(&toTexture2DDesc, nullptr, &toTexture2D);
    if (FAILED(hr)) {
        std::cout << "Could not create texture";
        return hr;
    }

    deviceContext->CopyResource(toTexture2D, fromTexture2D);

    CComPtr<IDXGISurface1> dxgiSurface1 = CComQIPtr<IDXGISurface1>(toTexture2D);

    DXGI_MAPPED_RECT mappedRect;
    dxgiSurface1->Map(&mappedRect, DXGI_MAP_READ);

    switch (outDesc.Rotation) {
        case DXGI_MODE_ROTATION_IDENTITY: {
            for (long y = 0; y < height; y++) {
                auto sourceYOffset = y * mappedRect.Pitch * sampleStep;
                auto bufferYOffset = y * width * 4;
                for (long x = 0; x < width; x++) {
                    auto sourceXOffset = x * 4 * sampleStep;
                    auto bufferXOffset = x * 4;
                    auto sourceOffset = sourceYOffset + sourceXOffset;
                    auto bufferOffset = bufferYOffset + bufferXOffset;
                    // Copies 4 bytes (BGRA)
                    memcpy_s(
                            inoutBuffer + bufferOffset,
                            4,
                            mappedRect.pBits + sourceOffset,
                            4
                    );
                }
            }
        }
            break;
        default:
            // TODO handle different rotations...
            return E_FAIL;
    }
    dxgiSurface1->Unmap();
    outputDuplication->ReleaseFrame();

    return NOERROR;
}

void capture::reset() {
    if (initialised) {
        initialised = false;
        sampleStep = 0;
        output1.Release();
        device.Release();
        deviceContext.Release();
        outputDuplication.Release();
    }
}