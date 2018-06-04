#include <jni.h>
#include <cstdio>
using namespace std;

#include "RogueProgram.h"

#define ROGUE_FN(return_type,class_name,fn_name) \
  extern "C" JNIEXPORT return_type JNICALL Java_org_plasmacore_##class_name##_##fn_name

static JNIEnv* Plasmacore_env;  // updated every Java JNI call

//-----------------------------------------------------------------------------
// Java Plasmacore
//-----------------------------------------------------------------------------
static jclass    jclass_Plasmacore;
static jclass    jclass_PlasmacoreMessage;
static jclass    jclass_ByteList;
static jfieldID  jfieldID_ByteList_bytes;
static jfieldID  jfieldID_ByteList_count;
static jmethodID jmethodID_ByteList_clear;
static jmethodID jmethodID_ByteList_reserve;

static jclass Rogue_find_class( JNIEnv* env, const char* class_name )
{
  jclass cls = env->FindClass( class_name );
  return reinterpret_cast<jclass>( env->NewGlobalRef(cls) );
}

ROGUE_FN( void, Plasmacore, nativeLaunch )( JNIEnv* env )
{
  ROGUE_LOG( "Launching Plasmacore..." );
  Plasmacore_env = env;

  jclass_Plasmacore        = Rogue_find_class( env, "org/plasmacore/Plasmacore" );
  jclass_PlasmacoreMessage = Rogue_find_class( env, "org/plasmacore/PlasmacoreMessage" );
  jclass_ByteList          = Rogue_find_class( env, "org/plasmacore/ByteList" );

  jfieldID_ByteList_bytes = env->GetFieldID( jclass_ByteList, "bytes", "[B" );
  jfieldID_ByteList_count = env->GetFieldID( jclass_ByteList, "count", "I" );
  jmethodID_ByteList_clear   = env->GetMethodID( jclass_ByteList, "clear", "()Lorg/plasmacore/ByteList;" );
  jmethodID_ByteList_reserve = env->GetMethodID( jclass_ByteList, "reserve", "(I)Lorg/plasmacore/ByteList;" );

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

ROGUE_FN( jboolean, Plasmacore, nativePostMessages )( JNIEnv* env, jobject static_context, jobject data )
{
  try
  {
    Rogue_collect_garbage();  // only happens if new allocation threshold has been reached

    RogueClassPlasmacore__MessageManager* mm =
      (RogueClassPlasmacore__MessageManager*) ROGUE_SINGLETON(Plasmacore__MessageManager);
    RogueByte_List* list = mm->io_buffer;

    RogueByte_List__clear( list );
    int count = env->GetIntField( data, jfieldID_ByteList_count );
    list->count = count;

    jobject bytes = env->GetObjectField( data, jfieldID_ByteList_bytes );

    if (count)
    {
      // Copy the Java message bytes to the Rogue list
      RogueByte_List__reserve__Int32( list, count );
      env->GetByteArrayRegion( (jbyteArray) bytes, 0, count, (signed char*) list->data->as_bytes );
    }

    // Call Rogue MessageManager.update(), which sends back a reference to another byte
    // list containing messages to us.
    list = RoguePlasmacore__MessageManager__update( mm );

    if ( !list || !list->count ) return false;

    // Clear the Java ByteList that was sent to us and copy the result in
    env->CallObjectMethod( bytes, jmethodID_ByteList_clear );
    env->CallObjectMethod( bytes, jmethodID_ByteList_reserve, list->count );
    env->SetIntField( bytes, jfieldID_ByteList_count, list->count );
    env->SetByteArrayRegion( (jbyteArray) bytes, 0, list->count, (signed char*) list->data->as_bytes );
    return true;
  }
  catch (RogueException* err)
  {
    RogueException__display( err );
    return false;
  }
  return false;
}

ROGUE_FN( void, Plasmacore, nativeQuit )( JNIEnv* env )
{
  Plasmacore_env = env;
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

ROGUE_FN( jboolean, Plasmacore, nativeSendMessage )( JNIEnv* env, jobject static_context, jobject data )
{
  try
  {
    RogueClassPlasmacore__MessageManager* mm =
      (RogueClassPlasmacore__MessageManager*) ROGUE_SINGLETON(Plasmacore__MessageManager);
    RogueByte_List* list = mm->direct_message_buffer;
    RogueByte_List__clear( list );

    int count = env->GetIntField( data, jfieldID_ByteList_count );
    list->count = count;

    jobject bytes = env->GetObjectField( data, jfieldID_ByteList_bytes );

    // Copy the Java message bytes to the Rogue list
    RogueByte_List__reserve__Int32( list, count );
    env->GetByteArrayRegion( (jbyteArray) bytes, 0, count, (signed char*) list->data->as_bytes );

    if (RoguePlasmacore__MessageManager__receive_message(mm))
    {
      // direct_message_buffer has been filled with result bytes.
      // Copy those into the ByteList that was sent to us.
      env->CallObjectMethod( bytes, jmethodID_ByteList_clear );
      env->CallObjectMethod( bytes, jmethodID_ByteList_reserve, list->count );
      env->SetIntField( bytes, jfieldID_ByteList_count, list->count );
      env->SetByteArrayRegion( (jbyteArray) bytes, 0, list->count, (signed char*) list->data->as_bytes );
      return true;
    }
    else
    {
      return false;
    }

  }
  catch (RogueException* err)
  {
    RogueException__display( err );
    return false;
  }
  return false;
}

//-----------------------------------------------------------------------------
// Rogue Plasmacore
//-----------------------------------------------------------------------------
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
