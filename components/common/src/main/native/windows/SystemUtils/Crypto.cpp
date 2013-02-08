#include "stdafx.h"
#include "SystemUtilities.h"
#include "Crypto.h"

JNIEXPORT jboolean JNICALL Java_org_limewire_util_SystemUtils_verifyExecutableSignature(JNIEnv *e, jclass c, jstring path, jbyteArray cert) {
	LPCTSTR execPath = GetJavaString(e, path);
	jbyte* bufferPtr = e->GetByteArrayElements(cert, NULL);
	jsize certLength = e->GetArrayLength(cert);


	e->ReleaseByteArrayElements(cert,bufferPtr,0);

	return TRUE;
}