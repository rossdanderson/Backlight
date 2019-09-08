#ifndef DXGI_CAPTURE_LOGGER_H
#define DXGI_CAPTURE_LOGGER_H

#include <string>

class logger {
public:
    virtual ~logger() = default;
    virtual void info(std::wstring message) = 0;
    virtual void warn(std::wstring message) = 0;
    virtual void error(std::wstring message) = 0;
};

#endif //DXGI_CAPTURE_LOGGER_H
