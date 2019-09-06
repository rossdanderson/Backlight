#ifndef DXGICAPTURE_CAPTURE_H
#define DXGICAPTURE_CAPTURE_H

#include <dxgi1_2.h>
#include <atlbase.h>
#include <d3d11.h>

struct point {
    long x;
    long y;

    point(long x, long y) : x(x), y(y) {}
};

struct rectangle {
    point topLeft;
    point bottomRight;

    rectangle(long top, long left, long bottom, long right) : topLeft(point(top, left)), bottomRight(point(bottom, right)) {}

    rectangle(const point &topLeft, const point &bottomRight) : topLeft(topLeft), bottomRight(bottomRight) {}
};

class capture {
public:
    capture();

    void init();

    rectangle getDimensions();

private:
    RECT dimensions = RECT();
    CComPtr<IDXGIAdapter1> adapter1 = nullptr;
    CComQIPtr<IDXGIOutput1> output1 = nullptr;
    CComPtr<ID3D11Device> device = nullptr;
    CComPtr<ID3D11DeviceContext> deviceContext = nullptr;
    CComPtr<IDXGIOutputDuplication> outputDuplication = nullptr;
};

#endif //DXGICAPTURE_CAPTURE_H
