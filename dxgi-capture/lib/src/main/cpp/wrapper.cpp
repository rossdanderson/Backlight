#include <cstdlib>
#include "wrapper.h"
#include "capture.h"

struct captor {
    void *obj;
};

captorT *captorCreate() {
    captorT *m;
    capture *obj;

    m = (decltype(m)) malloc(sizeof(*m));
    obj = new capture();
    m->obj = obj;

    return m;
}

void captorDestroy(captorT *pCaptor) {
    if (pCaptor == nullptr)
        return;
    delete static_cast<capture *>(pCaptor->obj);
    free(pCaptor);
}

//HRESULT captorInit(captorT *pCaptor, long sampleStep, size_t *outBufferSize) {
//    capture *obj;
//
//    if (pCaptor == nullptr)
//        return E_FAIL;
//
//    obj = static_cast<capture *>(pCaptor->obj);
//    return obj->init(sampleStep, outBufferSize);
//}

HRESULT captorGetOutputBits(captorT *pCaptor, unsigned char *inoutBuffer, size_t inoutBufferSize) {
    capture *obj;

    if (pCaptor == nullptr)
        return E_FAIL;

    obj = static_cast<capture *>(pCaptor->obj);
    return obj->getOutputBits(inoutBuffer, inoutBufferSize);
}
