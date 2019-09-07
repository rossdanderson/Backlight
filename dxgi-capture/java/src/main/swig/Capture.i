%module DXGICapture

%include "std_string.i"

%{
#include <sstream>
#include "capture.h"
%}

%rename (Capture) capture;
%rename (Point) point;
%rename (Rectangle) rectangle;

%include "capture.h"
%ignore operator<<;

%extend rectangle {
    std::string toString() const {
        std::ostringstream stream;
        stream << (*$self);
        return stream.str();
    }
};

%extend point {
    std::string toString() const {
        std::ostringstream stream;
        stream << (*$self);
        return stream.str();
    }
};