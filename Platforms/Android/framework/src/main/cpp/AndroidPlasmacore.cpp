#include <jni.h>

extern "C" JNIEXPORT jstring

JNICALL
Java_org_plasmacore_app_MainActivity_stringFromJNI2(
        JNIEnv *env,
        jobject /* this */) {
    const char* hello = "Hello from Plasmacore!";
    return env->NewStringUTF( hello );
}
