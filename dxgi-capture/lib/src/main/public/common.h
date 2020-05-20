#ifndef DXGI_CAPTURE_COMMON_H
#define DXGI_CAPTURE_COMMON_H

#ifdef __cplusplus
extern "C" {
#endif

struct point {
    long x;
    long y;
};

struct rectangle {
    struct point point1;
    struct point point2;
};

#ifdef __cplusplus
}
#endif

#endif //DXGI_CAPTURE_COMMON_H
