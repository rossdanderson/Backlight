%module(directors="1") DXGICapture

%{
#include <sstream>
#include <string>
#include "capture.h"
%}

%include "typemaps.i"
%include "windows.i"
%include "std_string.i"
%include "std_wstring.i"
%include "std_shared_ptr.i"

%ignore operator<<;

%apply long long * INOUT { size_t * };
%apply signed char * INOUT { unsigned char * };

%rename (Capture) capture;
%rename (CaptureResult) captureResult;
%rename (Point) point;
%rename (Rectangle) rectangle;

%include "common.h"
%include "capture.h"

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


