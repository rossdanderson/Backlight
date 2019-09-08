#ifndef DXGICAPTURE_HRESULTEXCEPTION_H
#define DXGICAPTURE_HRESULTEXCEPTION_H

#include <Windows.h>
#include <string>

class hResultException : public std::exception {
public:
    hResultException(const std::string &message, HRESULT hResult, const std::string &file, int line);

    [[nodiscard]] const char *what() const override;

private:
    std::string message;
};

#endif //DXGICAPTURE_HRESULTEXCEPTION_H
