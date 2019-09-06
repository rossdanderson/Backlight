%module DXGICapture

%include "std_string.i"

%{
#include "capture.h"
%}

%rename (Capture) capture;
%rename (Point) point;
%rename (Rectangle) rectangle;

%include "capture.h"
