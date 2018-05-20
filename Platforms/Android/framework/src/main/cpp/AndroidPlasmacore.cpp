#include <jni.h>
#include <cstdio>
using namespace std;

#include "RogueProgram.h"

extern "C" JNIEXPORT void JNICALL Java_org_plasmacore_framework_Rogue_rogueLaunch( JNIEnv *env )
{
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
}

