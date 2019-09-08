%module(directors="1") DXGICapture

%{
#include <sstream>
#include <string>
#include "logger.h"
#include "capture.h"
%}

%include "std_string.i"
%include "std_wstring.i"
%include "std_shared_ptr.i"

%shared_ptr(logger);
%feature("director") logger;

%ignore operator<<;

%rename (Capture) capture;
%rename (Point) point;
%rename (Rectangle) rectangle;
%rename (Logger) logger;

%include "logger.h"

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


