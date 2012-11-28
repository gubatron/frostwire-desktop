#include <jni.h>
#include "SystemUtils.h"
#include "ToggleFullScreen.h"

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT jboolean JNICALL Java_org_limewire_util_SystemUtils_toggleFullScreenNative(JNIEnv *e, jclass clazz, jlong hwnd) {

    return toggleFullScreen(hwnd);
}

#ifdef __cplusplus
}
#endif

