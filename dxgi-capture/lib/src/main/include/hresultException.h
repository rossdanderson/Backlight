#ifndef DXGI_CAPTURE_HRESULTEXCEPTION_H
#define DXGI_CAPTURE_HRESULTEXCEPTION_H

#include <Windows.h>
#include <string>

class hresultException : public std::exception {
public:
    hresultException(const std::string &message, HRESULT hresult, const std::string &file, int line);

    [[nodiscard]] const char *what() const override;

    HRESULT getHresult();

private:
    std::string message;
    HRESULT hresult;
};

#endif //DXGI_CAPTURE_HRESULTEXCEPTION_H
