#ifndef DXGICAPTURE_CAPTURE_H
#define DXGICAPTURE_CAPTURE_H

#include <ostream>
#include <dxgi1_2.h>
#include <atlbase.h>
#include <d3d11.h>

struct point {
    long x;
    long y;

    friend std::ostream &operator<<(std::ostream &stream, const point &point);

    point(long x, long y) : x(x), y(y) {}
};

inline std::ostream &operator<<(std::ostream &stream, const point &point) {
    return stream << "Point(x='" << point.x << "', y='" << point.y << "')";
}

struct rectangle {
    point point1;
    point point2;

    friend std::ostream &operator<<(std::ostream &stream, const rectangle &rectangle);

    rectangle(long x1, long y1, long x2, long y2) : point1(point(x1, y1)),
                                                    point2(point(x2, y2)) {}

    rectangle(const point &point1, const point &point2) : point1(point1), point2(point2) {}
};

inline std::ostream &operator<<(std::ostream &stream, const rectangle &rectangle) {
    return stream << "Rectangle(point1='" << rectangle.point1 << "', point2='" << rectangle.point2 << "')";
}

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
