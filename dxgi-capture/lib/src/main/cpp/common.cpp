#include "common.h"
#include <cmath>

long rectangleWidth(rectangle *rectangle) {
    return abs(rectangle->point1.x - rectangle->point2.x);
}

long rectangleHeight(rectangle *rectangle) {
    return abs(rectangle->point1.y - rectangle->point2.y);
}