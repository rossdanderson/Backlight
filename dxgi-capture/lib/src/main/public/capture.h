#ifndef DXGICAPTURE_CAPTURE_H
#define DXGICAPTURE_CAPTURE_H

#include <memory>
#include <ostream>
#include <utility>
#include <Windows.h>
#include <dxgi1_2.h>
#include <atlbase.h>
#include <d3d11.h>
#include "common.h"

inline std::ostream &operator<<(std::ostream &stream, const point &point) {
    return stream << "Point(x='" << point.x << "', y='" << point.y << "')";
}

inline std::ostream &operator<<(std::ostream &stream, const rectangle &rectangle) {
    return stream << "Rectangle(point1='" << rectangle.point1 << "', point2='" << rectangle.point2 << "')";
}

class capture {
public:
    HRESULT init(long sampleStep, size_t *outBufferSize);

    void reset();

    rectangle getDimensions();

    HRESULT getOutputBits(unsigned char *inoutBuffer, size_t inoutBufferSize);

private:
    boolean initialised = false;
    long sampleStep = 1;
    long width = 0;
    long height = 0;
    size_t requiredBufferSize = 0;
    RECT dimensions = RECT();
    CComQIPtr<IDXGIOutput1> output1 = nullptr;
    CComPtr<ID3D11Device> device = nullptr;
    CComPtr<ID3D11DeviceContext> deviceContext = nullptr;
    CComPtr<IDXGIOutputDuplication> outputDuplication = nullptr;
};

#endif //DXGICAPTURE_CAPTURE_H
