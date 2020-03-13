#ifndef DXGI_CAPTURE_CAPTUREUTILS_H
#define DXGI_CAPTURE_CAPTUREUTILS_H

#define SUCCESS_OR_THROW(message, hresult) captureUtils::checkHResult(message, hresult, __FILE__, __LINE__)

#include "hresultException.h"

namespace captureUtils {

    inline void checkHResult(const std::string &message, HRESULT hResult, const std::string &file, int line) {
        if (FAILED(hResult)) {
            throw hresultException(message, hResult, file, line);
        }
    }
}

#endif //DXGI_CAPTURE_CAPTUREUTILS_H
