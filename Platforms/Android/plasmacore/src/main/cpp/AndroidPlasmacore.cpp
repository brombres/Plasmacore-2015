#include <jni.h>
#include <cstdio>
using namespace std;

#include "RogueProgram.h"

#define ROGUE_FN(return_type,class_name,fn_name) \
  extern "C" JNIEXPORT return_type JNICALL Java_org_plasmacore_##class_name##_##fn_name

ROGUE_FN( void, Rogue,nativeLaunch )( JNIEnv* env )
{
  try
  {
    Rogue_quit();  // reset global state if necessary
    Rogue_configure( 0, NULL );
    Rogue_launch();
  }
  catch (RogueException* err)
  {
    printf( "Uncaught exception\n" );
    RogueException__display( err );
  }
}

ROGUE_FN( void, Rogue,nativeQuit )( JNIEnv* env )
{
  try
  {
    Rogue_quit();
  }
  catch (RogueException* err)
  {
    printf( "Uncaught exception\n" );
    RogueException__display( err );
  }
}

