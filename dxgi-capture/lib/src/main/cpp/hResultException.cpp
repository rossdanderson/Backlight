#include <sstream>
#include "../include/hResultException.h"

using namespace std;

hResultException::hResultException(const string &message, HRESULT hResult, const string &file, int line)
        : exception() {
    ostringstream stringStream;
    stringStream << message << " - HREASON(" << hResult << ") - " << file << ":" << line;
    this->message = stringStream.str();
}

const char *hResultException::what() const {
    return message.c_str();
}