#include <jni.h>
#include <cstdio>
using namespace std;

#include "RogueProgram.h"

#define ROGUE_FN(return_type,class_name,fn_name) \
  extern "C" JNIEXPORT return_type JNICALL Java_org_plasmacore_##class_name##_##fn_name

static JNIEnv* Plasmacore_jni_env;  // updated every Java JNI call

ROGUE_FN( void, Plasmacore, nativeLaunch )( JNIEnv* env )
{
  ROGUE_LOG( "Launching Plasmacore..." );
  Plasmacore_jni_env = env;
  try
  {
    Rogue_quit();  // reset global state if necessary
    Rogue_configure( 0, NULL );
    Rogue_launch();
  }
  catch (RogueException* err)
  {
    ROGUE_LOG_ERROR( "Uncaught exception\n" );
    RogueException__display( err );
  }
}

ROGUE_FN( void, Plasmacore, nativeQuit )( JNIEnv* env )
{
  Plasmacore_jni_env = env;
  try
  {
    Rogue_quit();
  }
  catch (RogueException* err)
  {
    ROGUE_LOG_ERROR( "Uncaught exception\n" );
    RogueException__display( err );
  }
}

extern "C" RogueString* Plasmacore_find_asset( RogueString* name )
{
  return NULL;
}

extern "C" RogueString* Plasmacore_get_user_data_folder()
{
  return RogueString_create_from_utf8( "." );
}

extern "C" RogueString* Plasmacore_get_application_data_folder()
{
  return RogueString_create_from_utf8( "." );
}

bool PlasmacoreMessage_send( RogueByte_List* data )
{
  ROGUE_LOG( "TODO: PlasmacoreMessage_send()" );
  return false;
}
