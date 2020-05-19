#ifndef DXGI_CAPTURE_WRAPPER_H
#define DXGI_CAPTURE_WRAPPER_H

#include <Windows.h>

#ifdef __cplusplus
extern "C" {
#endif

struct captor;

typedef struct captor captorT;

captorT *captorCreate();
void captorDestroy(captorT *pCaptor);

HRESULT captorInit(captorT *pCaptor, long sampleStep, size_t *outBufferSize);
HRESULT captorGetOutputBits(captorT *pCaptor, unsigned char *inoutBuffer, size_t inoutBufferSize);

#ifdef __cplusplus
}
#endif

#endif //DXGI_CAPTURE_WRAPPER_H
