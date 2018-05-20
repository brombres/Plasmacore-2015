#include <jni.h>
#include <cstdio>
using namespace std;

#include "RogueProgram.h"

extern "C" JNIEXPORT jstring

JNICALL
Java_org_plasmacore_app_MainActivity_stringFromJNI2(
        JNIEnv *env,
        jobject /* this */) {
  try
  {
    Rogue_quit();  // reset global state
    Rogue_configure( 0, NULL );
    Rogue_launch();
    Rogue_quit();
  }
  catch (RogueException* err)
  {
    printf( "Uncaught exception\n" );
    RogueException__display( err );
  }
    const char* hello = "Hello from C++!";
    return env->NewStringUTF( hello );
}
