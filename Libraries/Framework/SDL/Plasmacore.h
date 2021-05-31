#ifndef PLASMACORE_H
#define PLASMACORE_H

#include <cstdint>

#include "PlasmacoreMessage.h"
#include "PlasmacoreUtility.h"

typedef int HID;
typedef int RID;
typedef int Int;
typedef PlasmacoreList<uint8_t> Buffer;

//=============================================================================
//  PlasmacoreLauncher
//=============================================================================
struct PlasmacoreLauncher
{
  // PROPERTIES
  int               argc;
  char**            argv;
  PlasmacoreCString default_window_title;
  int               default_display_width;
  int               default_display_height;

  // METHODS
  PlasmacoreLauncher( int argc, char* argv[] );
  PlasmacoreLauncher( int argc, char* argv[], PlasmacoreCString default_window_title, int default_display_width, int default_display_height );

  int launch();
};


class PlasmacoreMessageHandler
{
public:
  const char* type;
  HandlerCallback callback;

  PlasmacoreMessageHandler (const char* type, HandlerCallback callback)
  : type(type), callback(callback)
  {
  }

  PlasmacoreMessageHandler ()
  : type("<invalid>")
  {
  }
};



class Plasmacore
{
public:
  static Plasmacore singleton;

  bool is_configured = false;
  bool is_launched   = false;

  double idleUpdateFrequency = 0.5;

  Buffer pending_message_data;
  Buffer io_buffer;
  Buffer decode_buffer;

  bool is_sending = false;
  bool update_requested = false;

  PlasmacoreStringTable<PlasmacoreMessageHandler*> handlers;
  PlasmacoreIntTable<PlasmacoreMessageHandler*>    reply_handlers;
  PlasmacoreIntTable<void*> resources;

  bool update_timer = false; // true if running

  void set_message_handler( const char * type, HandlerCallback handler );
  Plasmacore & configure();
  RID getResourceID( void * resource);
  Plasmacore & launch();
  Plasmacore & relaunch();
  void remove_message_handler( const char* type );
  void post( PlasmacoreMessage & m );
  void post_rsvp( PlasmacoreMessage & m, HandlerCallback callback );
  Plasmacore & setIdleUpdateFrequency( double f );
  void start();
  void stop();
  static void update(void * dummy);
  static void fast_update(void * dummy);
  void real_update(bool reschedule);
  void dispatch( PlasmacoreMessage& m );
};

//-----------------------------------------------------------------------------
// Plasmacore Keycodes
// Does not include shifted characters such as '(' because these correspond to
// physical keys such as LEFT_SHIFT and NUMBER_9.
//-----------------------------------------------------------------------------
#define PKC_LEFT_ARROW        1
#define PKC_UP_ARROW          2
#define PKC_RIGHT_ARROW       3
#define PKC_DOWN_ARROW        4

#define PKC_BACKSPACE         8
#define PKC_TAB               9
#define PKC_ENTER            10

#define PKC_CAPS_LOCK        11
#define PKC_LEFT_SHIFT       12
#define PKC_RIGHT_SHIFT      13
#define PKC_LEFT_CONTROL     14
#define PKC_RIGHT_CONTROL    15
#define PKC_LEFT_ALT         16
#define PKC_RIGHT_ALT        17

#define PKC_LEFT_OS          18
#define PKC_RIGHT_OS         19
#define PKC_LEFT_WINDOWS     LEFT_OS
#define PKC_RIGHT_WINDOWS    RIGHT_OS
#define PKC_LEFT_COMMAND     LEFT_OS
#define PKC_RIGHT_COMMAND    RIGHT_OS

#define PKC_FUNCTION         26
#define PKC_ESCAPE           27

#define PKC_SPACE            32

#define PKC_APOSTROPHE       39
#define PKC_COMMA            44
#define PKC_MINUS            45
#define PKC_PERIOD           46
#define PKC_SLASH            47
#define PKC_NUMBER_0         48
#define PKC_NUMBER_1         49
#define PKC_NUMBER_2         50
#define PKC_NUMBER_3         51
#define PKC_NUMBER_4         52
#define PKC_NUMBER_5         53
#define PKC_NUMBER_6         54
#define PKC_NUMBER_7         55
#define PKC_NUMBER_8         56
#define PKC_NUMBER_9         57
#define PKC_SEMICOLON        59
#define PKC_EQUALS           61

#define PKC_AT               64
#define PKC_A                65
#define PKC_B                66
#define PKC_C                67
#define PKC_D                68
#define PKC_E                69
#define PKC_F                70
#define PKC_G                71
#define PKC_H                72
#define PKC_I                73
#define PKC_J                74
#define PKC_K                75
#define PKC_L                76
#define PKC_M                77
#define PKC_N                78
#define PKC_O                79
#define PKC_P                80
#define PKC_Q                81
#define PKC_R                82
#define PKC_S                83
#define PKC_T                84
#define PKC_U                85
#define PKC_V                86
#define PKC_W                87
#define PKC_X                88
#define PKC_Y                89
#define PKC_Z                90

#define PKC_OPEN_BRACKET     91
#define PKC_BACKSLASH        92
#define PKC_CLOSE_BRACKET    93
#define PKC_BACKQUOTE        96

#define PKC_NUMPAD_ENTER    110

#define PKC_SYS_REQUEST     124
#define PKC_SCROLL_LOCK     125
#define PKC_BREAK           126

#define PKC_DELETE          127
#define PKC_INSERT          128
#define PKC_HOME            129
#define PKC_END             130
#define PKC_PAGE_UP         131
#define PKC_PAGE_DOWN       132

#define PKC_NUMPAD_ASTERISK 142
#define PKC_NUMPAD_PLUS     143
#define PKC_NUMPAD_MINUS    145
#define PKC_NUMPAD_PERIOD   146
#define PKC_NUMPAD_SLASH    147
#define PKC_NUMPAD_0        148
#define PKC_NUMPAD_1        149
#define PKC_NUMPAD_2        150
#define PKC_NUMPAD_3        151
#define PKC_NUMPAD_4        152
#define PKC_NUMPAD_5        153
#define PKC_NUMPAD_6        154
#define PKC_NUMPAD_7        155
#define PKC_NUMPAD_8        156
#define PKC_NUMPAD_9        157
#define PKC_NUMPAD_NUM_LOCK 158
#define PKC_NUMPAD_EQUALS   161

#define PKC_F1              201
#define PKC_F2              202
#define PKC_F3              203
#define PKC_F4              204
#define PKC_F5              205
#define PKC_F6              206
#define PKC_F7              207
#define PKC_F8              208
#define PKC_F9              209
#define PKC_F10             210
#define PKC_F11             211
#define PKC_F12             212
#define PKC_F13             213
#define PKC_F14             214
#define PKC_F15             215
#define PKC_F16             216
#define PKC_F17             217
#define PKC_F18             218
#define PKC_F19             219
#define PKC_F20             220
#define PKC_F21             221
#define PKC_F22             222
#define PKC_F23             223
#define PKC_F24             224

#endif
