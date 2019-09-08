#include "../public/capture.h"
#include "../include/captureUtils.h"
#include <sstream>

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
        testAdapter1->GetDesc1(&adapterDesc);

        auto j = 0;
        CComPtr<IDXGIOutput> testOutput = nullptr;
        while (output == nullptr && testAdapter1->EnumOutputs(j, &testOutput) != DXGI_ERROR_NOT_FOUND) {
            DXGI_OUTPUT_DESC outputDesc;
            testOutput->GetDesc(&outputDesc);

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
    adapter1->GetDesc1(&adapterDesc);

    DXGI_OUTPUT_DESC outputDesc;
    output->GetDesc(&outputDesc);

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
    if (output1 == nullptr) {
        throw invalid_argument("output does not implement IDXGIOutput1");
    }

    CComQIPtr<IDXGIDevice1> device1(device);
    if (device1 == nullptr) {
        throw invalid_argument("device does not implement IDXGIDevice1");
    }

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
