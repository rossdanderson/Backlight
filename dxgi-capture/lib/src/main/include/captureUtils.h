//
// Created by rossd on 29/08/2019.
//

#ifndef DXGICAPTURE_CAPTUREUTILS_H
#define DXGICAPTURE_CAPTUREUTILS_H

#define SUCCESS_OR_THROW(message, hresult) captureUtils::checkHResult(message, hresult, __FILE__, __LINE__)

#include "hresultException.h"

namespace captureUtils {

    inline void checkHResult(const std::string &message, HRESULT hResult, const std::string &file, int line) {
        if (FAILED(hResult)) {
            throw hresultException(message, hResult, file, line);
        }
    }
}

#endif //DXGICAPTURE_CAPTUREUTILS_H
