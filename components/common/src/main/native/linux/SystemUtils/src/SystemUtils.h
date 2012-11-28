#include <jni.h>

#ifndef __SYSTEM_UTILS_INCLUDE__
#define __SYSTEM_UTILS_INCLUDE__
#ifdef __cplusplus
extern "C" {
#endif

    JNIEXPORT void JNICALL Java_org_limewire_util_SystemUtils_toggleFullScreen(JNIEnv *, jclass, jboolean);

#ifdef __cplusplus
}
#endif
#endif //__SYSTEM_UTILS_INCLUDE__
