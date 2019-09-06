#ifndef DXGICAPTURE_CAPTUREMACROS_H
#define DXGICAPTURE_CAPTUREMACROS_H

#ifdef WIN32
#  ifdef DXGICaptureLib_EXPORTS
#    define EXPORT __declspec(dllexport)
#  else
#    define EXPORT __declspec(dllimport)
#  endif
#else
#  define EXPORT
#endif

#endif //DXGICAPTURE_CAPTUREMACROS_H
