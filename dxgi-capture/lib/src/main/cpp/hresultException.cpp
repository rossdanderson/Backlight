#include <sstream>
#include "hresultException.h"

using namespace std;

hresultException::hresultException(const string &message, HRESULT hresult, const string &file, int line)
        : exception() {
    this->hresult = hresult;
    ostringstream stringStream;
    stringStream << message << " - HRESULT(" << hresult << ") - " << file << ":" << line;
    this->message = stringStream.str();
}

const char *hresultException::what() const {
    return message.c_str();
}

HRESULT hresultException::getHresult() {
    return hresult;
}
