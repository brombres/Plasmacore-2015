#include <SDL2/SDL.h>
#include <SDL2/SDL_opengles2.h>
#ifdef __EMSCRIPTEN__
#include <emscripten.h>
#include <emscripten/html5.h>
#endif

#include "Plasmacore.h"
#include "PlasmacoreUtility.h"
#include "PlasmacoreView.h"

PlasmacoreIntTable<PlasmacoreView*> sdl_windows;

const char* PlasmacoreView::default_window_title   = "Plasmacore";
int         PlasmacoreView::default_display_width  = 1024;
int         PlasmacoreView::default_display_height = 768;
bool        PlasmacoreView::display_size_changed   = false;

PlasmacoreView * plasmacore_get_window (int swindow_id)
{
  if ( !sdl_windows.contains(swindow_id)) return 0;
  return sdl_windows[swindow_id];
}

void plasmacore_redraw_all_windows (void)
{
  for (auto const & iter : sdl_windows)
  {
    iter->value->redraw();
  }
}

void PlasmacoreView::destroy ()
{
  //TODO
  //SDL_GL_DeleteContext(gl_context);
  //sdl_windows.erase swindowID
}

/// Override to change just window creation
void PlasmacoreView::create_window ()
{
#ifdef __EMSCRIPTEN__
  double w, h;
  emscripten_get_element_css_size( 0, &w, &h );
  printf( "LOG: initial display size: %dx%d\n", (int)w, (int)h );
  initial_width  = (int) w;
  initial_height = (int) h;
  //emscripten_set_canvas_element_size( 0, initial_width, initial_height );
#endif

  window = SDL_CreateWindow(default_window_title, SDL_WINDOWPOS_UNDEFINED, SDL_WINDOWPOS_UNDEFINED, initial_width, initial_height, window_flags|SDL_WINDOW_OPENGL);
}

/// Called by view factory to create window
void PlasmacoreView::init ()
{
  printf("LOG: PlasmacoreView::init()\n");
#ifdef __EMSCRIPTEN__
  SDL_GL_SetAttribute(SDL_GL_CONTEXT_PROFILE_MASK, SDL_GL_CONTEXT_PROFILE_ES);
#endif
  SDL_GL_SetAttribute(SDL_GL_CONTEXT_MAJOR_VERSION, 2);
  SDL_GL_SetAttribute(SDL_GL_CONTEXT_MINOR_VERSION, 0);
  SDL_GL_SetAttribute(SDL_GL_DOUBLEBUFFER, 1);
  SDL_GL_SetAttribute(SDL_GL_ACCELERATED_VISUAL, 1);

  create_window();
  if (!window) throw "Didn't create window";

  gl_context = SDL_GL_CreateContext(window);
  if (!gl_context) throw "Didn't create GL context";

  swindowID = SDL_GetWindowID(window);
  sdl_windows[swindowID] = this;

  if (SDL_GL_MakeCurrent(window, gl_context))
  {
    fprintf( stderr, "SDL_GL_MakeCurrent() failed: %s\n", SDL_GetError() );
    return;
  }

  glClearColor(0,0,0,1);
  glClear(GL_COLOR_BUFFER_BIT);

  configure(); // Is there a reason not to call this here?
}


void PlasmacoreView::configure()
{
  if (isConfigured) { return; }
  isConfigured = true;

  pwindowID = Plasmacore::singleton.getResourceID( this );

  SDL_RaiseWindow(window); // Should we immediately post a focus event? (Mac version does...)

  fprintf( stderr, "PlasmacoreView %s:\n", name );
  fprintf( stderr, "  SDL   WID %d:\n", swindowID );
  fprintf( stderr, "  PCore WID %d:\n", pwindowID );
}


void PlasmacoreView::redraw ()
{
  configure();
  if (!window) return;
  if (!gl_context) return;

#ifdef __EMSCRIPTEN__
  if (display_size_changed)
  {
    double w, h;
    emscripten_get_element_css_size( 0, &w, &h );
    printf( "LOG: Display size changed to %dx%d\n", (int)w, (int)h );
    SDL_SetWindowSize( window, (int)w, (int) h );
  }
#endif

  auto flags = SDL_GetWindowFlags(window);
  if (flags & SDL_WINDOW_MINIMIZED) return;
  if (!(flags & SDL_WINDOW_SHOWN)) return;

  if (SDL_GL_MakeCurrent(window, gl_context))
  {
    fprintf( stderr, "SDL_GL_MakeCurrent() failed: %s\n", SDL_GetError() );
    return;
  }

  int display_width, display_height;
  SDL_GetWindowSize(window, &display_width, &display_height);
  auto m = PlasmacoreMessage( "Display.on_render" );
  m.set( "window_id", pwindowID ).set( "display_name", name );
  m.set( "display_width",  display_width );
  m.set( "display_height", display_height );
  m.set( "viewport_width",  display_width );
  m.set( "viewport_height", display_height );
  m.send();
  SDL_GL_SwapWindow(window);
}


void PlasmacoreView::show ()
{
  SDL_ShowWindow(window);
}

void PlasmacoreView::on_mouse_down (int x, int y, int button)
{
  configure();
  // button 0 = left, 1 = right
  auto m = PlasmacoreMessage( "Display.on_pointer_event" );
  m.set( "window_id", pwindowID ).set( "display_name", name );
  m.set( "type", 1 );  // 1=press
  m.set( "x", x );
  m.set( "y", y );
  m.set( "index", button );
  m.post();
}

void PlasmacoreView::on_mouse_up (int x, int y, int button)
{
  configure();
  auto m = PlasmacoreMessage( "Display.on_pointer_event" );
  m.set( "window_id", pwindowID ).set( "display_name", name );
  m.set( "type", 2 );  // 2=release
  m.set( "x", x );
  m.set( "y", y );
  m.set( "index", button );
  m.post();
}

void PlasmacoreView::on_mouse_move (int x, int y)
{
  configure();
  auto m = PlasmacoreMessage( "Display.on_pointer_event" );
  m.set( "window_id", pwindowID ).set( "display_name", name );
  m.set( "type", 0 );  // 0=move
  m.set( "x", x );
  m.set( "y", y );
  m.post();
}

int Plasmacore_syscode_to_keycode( int syscode );

void PlasmacoreView::on_key_event( int syscode, bool is_press, bool is_repeat )
{
  configure();
  auto m = PlasmacoreMessage( "Display.on_key_event" );
  m.set( "window_id", pwindowID ).set( "display_name", name );
  m.set( "syscode", syscode );
  m.set( "keycode", Plasmacore_syscode_to_keycode(syscode) );
  if (is_repeat) m.set( "is_repeat", true );
  m.set( "is_press",  is_press );
  m.post();
}

void PlasmacoreView::on_focus_gained  (void)
{
  configure();
  auto m = PlasmacoreMessage( "Display.focus_gained" );
  m.set( "window_id", pwindowID );
  m.set( "display_name", name );
  m.post();
}



PlasmacoreStringTable<ViewFactory>* plasmacore_views = 0;

void plasmacore_register_view_factory (const char* name, ViewFactory factory)
{
  if (!plasmacore_views) plasmacore_views = new PlasmacoreStringTable<ViewFactory>();
  (*plasmacore_views)[name] = factory;
}

static PlasmacoreView * default_factory (const char* name)
{
  auto v = new PlasmacoreView();
  v->name = name;
  v->init();
  return v;
}

PlasmacoreView * plasmacore_new_view ( const char* name )
{
  ViewFactory factory = 0;
  if ( (!plasmacore_views) || !plasmacore_views->contains(name) )
  {
    factory = default_factory;
  }
  else if (plasmacore_views->contains(name))
  {
    factory = (*plasmacore_views)[name];
  }
  else if (plasmacore_views->contains(DEFAULT_VIEW_FACTORY))
  {
    factory = (*plasmacore_views)[DEFAULT_VIEW_FACTORY];
  }
  if (!factory) throw "Null view factory?";
  return factory(name);
}

int Plasmacore_syscode_to_keycode( int syscode )
{
  switch (syscode)
  {
    case SDL_SCANCODE_0:                  return PKC_NUMBER_0;
    case SDL_SCANCODE_1:                  return PKC_NUMBER_1;
    case SDL_SCANCODE_2:                  return PKC_NUMBER_2;
    case SDL_SCANCODE_3:                  return PKC_NUMBER_3;
    case SDL_SCANCODE_4:                  return PKC_NUMBER_4;
    case SDL_SCANCODE_5:                  return PKC_NUMBER_5;
    case SDL_SCANCODE_6:                  return PKC_NUMBER_6;
    case SDL_SCANCODE_7:                  return PKC_NUMBER_7;
    case SDL_SCANCODE_8:                  return PKC_NUMBER_8;
    case SDL_SCANCODE_9:                  return PKC_NUMBER_9;
    case SDL_SCANCODE_A:                  return PKC_A;
    // Application Control Keypad
    //case SDL_SCANCODE_AC_BACK:            return PKC_;
    //case SDL_SCANCODE_AC_BOOKMARKS:       return PKC_;
    //case SDL_SCANCODE_AC_FORWARD:         return PKC_;
    //case SDL_SCANCODE_AC_HOME:            return PKC_;
    //case SDL_SCANCODE_AC_REFRESH:         return PKC_;
    //case SDL_SCANCODE_AC_SEARCH:          return PKC_;
    //case SDL_SCANCODE_AC_STOP:            return PKC_;
    //case SDL_SCANCODE_AGAIN:              return PKC_;
    //case SDL_SCANCODE_ALTERASE:           return PKC_;
    case SDL_SCANCODE_APOSTROPHE:         return PKC_APOSTROPHE;
    //case SDL_SCANCODE_APPLICATION:        return PKC_;
    //case SDL_SCANCODE_AUDIOMUTE:          return PKC_;
    //case SDL_SCANCODE_AUDIONEXT:          return PKC_;
    //case SDL_SCANCODE_AUDIOPLAY:          return PKC_;
    //case SDL_SCANCODE_AUDIOPREV:          return PKC_;
    //case SDL_SCANCODE_AUDIOSTOP:          return PKC_;
    case SDL_SCANCODE_B:                  return PKC_B;
    case SDL_SCANCODE_BACKSLASH:          return PKC_BACKSLASH;
    case SDL_SCANCODE_BACKSPACE:          return PKC_BACKSPACE;
    //case SDL_SCANCODE_BRIGHTNESSDOWN:     return PKC_;
    //case SDL_SCANCODE_BRIGHTNESSUP:       return PKC_;
    case SDL_SCANCODE_C:                  return PKC_C;
    //case SDL_SCANCODE_CALCULATOR:         return PKC_;
    //case SDL_SCANCODE_CANCEL:             return PKC_;
    case SDL_SCANCODE_CAPSLOCK:           return PKC_CAPS_LOCK;
    //case SDL_SCANCODE_CLEAR:              return PKC_;
    //case SDL_SCANCODE_CLEARAGAIN:         return PKC_;
    case SDL_SCANCODE_COMMA:              return PKC_COMMA;
    //case SDL_SCANCODE_COMPUTER:           return PKC_;
    //case SDL_SCANCODE_COPY:               return PKC_;
    //case SDL_SCANCODE_CRSEL:              return PKC_;
    //case SDL_SCANCODE_CURRENCYSUBUNIT:    return PKC_;
    //case SDL_SCANCODE_CURRENCYUNIT:       return PKC_;
    //case SDL_SCANCODE_CUT:                return PKC_;
    case SDL_SCANCODE_D:                  return PKC_D;
    //case SDL_SCANCODE_DECIMALSEPARATOR:   return PKC_;
    case SDL_SCANCODE_DELETE:             return PKC_DELETE;
    //case SDL_SCANCODE_DISPLAYSWITCH:      return PKC_;
    case SDL_SCANCODE_DOWN:               return PKC_DOWN_ARROW;
    case SDL_SCANCODE_E:                  return PKC_E;
    //case SDL_SCANCODE_EJECT:              return PKC_;
    case SDL_SCANCODE_END:                return PKC_END;
    case SDL_SCANCODE_EQUALS:             return PKC_EQUALS;
    case SDL_SCANCODE_ESCAPE:             return PKC_ESCAPE;
    //case SDL_SCANCODE_EXECUTE:            return PKC_;
    //case SDL_SCANCODE_EXSEL:              return PKC_;
    case SDL_SCANCODE_F:                  return PKC_F;
    case SDL_SCANCODE_F1:                 return PKC_F1;
    case SDL_SCANCODE_F10:                return PKC_F10;
    case SDL_SCANCODE_F11:                return PKC_F11;
    case SDL_SCANCODE_F12:                return PKC_F12;
    case SDL_SCANCODE_F13:                return PKC_F13;
    case SDL_SCANCODE_F14:                return PKC_F14;
    case SDL_SCANCODE_F15:                return PKC_F15;
    case SDL_SCANCODE_F16:                return PKC_F16;
    case SDL_SCANCODE_F17:                return PKC_F17;
    case SDL_SCANCODE_F18:                return PKC_F18;
    case SDL_SCANCODE_F19:                return PKC_F19;
    case SDL_SCANCODE_F2:                 return PKC_F2;
    case SDL_SCANCODE_F20:                return PKC_F20;
    case SDL_SCANCODE_F21:                return PKC_F21;
    case SDL_SCANCODE_F22:                return PKC_F22;
    case SDL_SCANCODE_F23:                return PKC_F23;
    case SDL_SCANCODE_F24:                return PKC_F24;
    case SDL_SCANCODE_F3:                 return PKC_F3;
    case SDL_SCANCODE_F4:                 return PKC_F4;
    case SDL_SCANCODE_F5:                 return PKC_F5;
    case SDL_SCANCODE_F6:                 return PKC_F6;
    case SDL_SCANCODE_F7:                 return PKC_F7;
    case SDL_SCANCODE_F8:                 return PKC_F8;
    case SDL_SCANCODE_F9:                 return PKC_F9;
    //case SDL_SCANCODE_FIND:               return PKC_;
    case SDL_SCANCODE_G:                  return PKC_G;
    case SDL_SCANCODE_GRAVE:              return PKC_BACKQUOTE;
    case SDL_SCANCODE_H:                  return PKC_H;
    //case SDL_SCANCODE_HELP:               return PKC_;
    case SDL_SCANCODE_HOME:               return PKC_HOME;
    case SDL_SCANCODE_I:                  return PKC_I;
    case SDL_SCANCODE_INSERT:             return PKC_INSERT;
    case SDL_SCANCODE_J:                  return PKC_J;
    case SDL_SCANCODE_K:                  return PKC_K;
    // Keyboard Illumination
    //case SDL_SCANCODE_KBDILLUMDOWN:       return PKC_;
    //case SDL_SCANCODE_KBDILLUMTOGGLE:     return PKC_;
    //case SDL_SCANCODE_KBDILLUMUP:         return PKC_;
    case SDL_SCANCODE_KP_0:               return PKC_NUMPAD_0;
    //case SDL_SCANCODE_KP_00:              return PKC_;
    //case SDL_SCANCODE_KP_000:             return PKC_;
    case SDL_SCANCODE_KP_1:               return PKC_NUMPAD_1;
    case SDL_SCANCODE_KP_2:               return PKC_NUMPAD_2;
    case SDL_SCANCODE_KP_3:               return PKC_NUMPAD_3;
    case SDL_SCANCODE_KP_4:               return PKC_NUMPAD_4;
    case SDL_SCANCODE_KP_5:               return PKC_NUMPAD_5;
    case SDL_SCANCODE_KP_6:               return PKC_NUMPAD_6;
    case SDL_SCANCODE_KP_7:               return PKC_NUMPAD_7;
    case SDL_SCANCODE_KP_8:               return PKC_NUMPAD_8;
    case SDL_SCANCODE_KP_9:               return PKC_NUMPAD_9;
    //case SDL_SCANCODE_KP_A:               return PKC_;
    //case SDL_SCANCODE_KP_AMPERSAND:       return PKC_;
    //case SDL_SCANCODE_KP_AT:              return PKC_;
    //case SDL_SCANCODE_KP_B:               return PKC_;
    //case SDL_SCANCODE_KP_BACKSPACE:       return PKC_;
    //case SDL_SCANCODE_KP_BINARY:          return PKC_;
    //case SDL_SCANCODE_KP_C:               return PKC_;
    //case SDL_SCANCODE_KP_CLEAR:           return PKC_;
    //case SDL_SCANCODE_KP_CLEARENTRY:      return PKC_;
    //case SDL_SCANCODE_KP_COLON:           return PKC_;
    //case SDL_SCANCODE_KP_COMMA:           return PKC_;
    //case SDL_SCANCODE_KP_D:               return PKC_;
    //case SDL_SCANCODE_KP_DBLAMPERSAND:    return PKC_;
    //case SDL_SCANCODE_KP_DBLVERTICALBAR:  return PKC_;
    //case SDL_SCANCODE_KP_DECIMAL:         return PKC_;
    case SDL_SCANCODE_KP_DIVIDE:          return PKC_NUMPAD_SLASH;
    //case SDL_SCANCODE_KP_E:               return PKC_;
    case SDL_SCANCODE_KP_ENTER:           return PKC_NUMPAD_ENTER;
    case SDL_SCANCODE_KP_EQUALS:          return PKC_NUMPAD_EQUALS;
    //case SDL_SCANCODE_KP_EQUALSAS400:     return PKC_;
    //case SDL_SCANCODE_KP_EXCLAM:          return PKC_;
    //case SDL_SCANCODE_KP_F:               return PKC_;
    //case SDL_SCANCODE_KP_GREATER:         return PKC_;
    //case SDL_SCANCODE_KP_HASH:            return PKC_;
    //case SDL_SCANCODE_KP_HEXADECIMAL:     return PKC_;
    //case SDL_SCANCODE_KP_LEFTBRACE:       return PKC_;
    //case SDL_SCANCODE_KP_LEFTPAREN:       return PKC_;
    //case SDL_SCANCODE_KP_LESS:            return PKC_;
    //case SDL_SCANCODE_KP_MEMADD:          return PKC_;
    //case SDL_SCANCODE_KP_MEMCLEAR:        return PKC_;
    //case SDL_SCANCODE_KP_MEMDIVIDE:       return PKC_;
    //case SDL_SCANCODE_KP_MEMMULTIPLY:     return PKC_;
    //case SDL_SCANCODE_KP_MEMRECALL:       return PKC_;
    //case SDL_SCANCODE_KP_MEMSTORE:        return PKC_;
    //case SDL_SCANCODE_KP_MEMSUBTRACT:     return PKC_;
    case SDL_SCANCODE_KP_MINUS:           return PKC_NUMPAD_MINUS;
    case SDL_SCANCODE_KP_MULTIPLY:        return PKC_NUMPAD_ASTERISK;
    //case SDL_SCANCODE_KP_OCTAL:           return PKC_;
    //case SDL_SCANCODE_KP_PERCENT:         return PKC_;
    case SDL_SCANCODE_KP_PERIOD:          return PKC_NUMPAD_PERIOD;
    case SDL_SCANCODE_KP_PLUS:            return PKC_NUMPAD_PLUS;
    //case SDL_SCANCODE_KP_PLUSMINUS:       return PKC_;
    //case SDL_SCANCODE_KP_POWER:           return PKC_;
    //case SDL_SCANCODE_KP_RIGHTBRACE:      return PKC_;
    //case SDL_SCANCODE_KP_RIGHTPAREN:      return PKC_;
    //case SDL_SCANCODE_KP_SPACE:           return PKC_;
    //case SDL_SCANCODE_KP_TAB:             return PKC_;
    //case SDL_SCANCODE_KP_VERTICALBAR:     return PKC_;
    //case SDL_SCANCODE_KP_XOR:             return PKC_;
    case SDL_SCANCODE_L:                  return PKC_L;
    case SDL_SCANCODE_LALT:               return PKC_LEFT_ALT;
    case SDL_SCANCODE_LCTRL:              return PKC_LEFT_CONTROL;
    case SDL_SCANCODE_LEFT:               return PKC_LEFT_ARROW;
    case SDL_SCANCODE_LEFTBRACKET:        return PKC_OPEN_BRACKET;
    case SDL_SCANCODE_LGUI:               return PKC_LEFT_OS;
    case SDL_SCANCODE_LSHIFT:             return PKC_LEFT_SHIFT;
    case SDL_SCANCODE_M:                  return PKC_M;
    //case SDL_SCANCODE_MAIL:               return PKC_;
    //case SDL_SCANCODE_MEDIASELECT:        return PKC_;
    //case SDL_SCANCODE_MENU:               return PKC_;
    case SDL_SCANCODE_MINUS:              return PKC_MINUS;
    //case SDL_SCANCODE_MODE:               return PKC_;
    //case SDL_SCANCODE_MUTE:               return PKC_;
    case SDL_SCANCODE_N:                  return PKC_N;
    //case SDL_SCANCODE_NUMLOCKCLEAR:       return PKC_;
    case SDL_SCANCODE_O:                  return PKC_O;
    //case SDL_SCANCODE_OPER:               return PKC_;
    //case SDL_SCANCODE_OUT:                return PKC_;
    case SDL_SCANCODE_P:                  return PKC_P;
    case SDL_SCANCODE_PAGEDOWN:           return PKC_PAGE_DOWN;
    case SDL_SCANCODE_PAGEUP:             return PKC_PAGE_UP;
    //case SDL_SCANCODE_PASTE:              return PKC_;
    //case SDL_SCANCODE_PAUSE:              return PKC_;
    case SDL_SCANCODE_PERIOD:             return PKC_PERIOD;
    //case SDL_SCANCODE_POWER:              return PKC_;
    //case SDL_SCANCODE_PRINTSCREEN:        return PKC_;
    //case SDL_SCANCODE_PRIOR:              return PKC_;
    case SDL_SCANCODE_Q:                  return PKC_Q;
    case SDL_SCANCODE_R:                  return PKC_R;
    case SDL_SCANCODE_RALT:               return PKC_RIGHT_ALT;
    case SDL_SCANCODE_RCTRL:              return PKC_RIGHT_CONTROL;
    case SDL_SCANCODE_RETURN:             return PKC_ENTER;
    case SDL_SCANCODE_RETURN2:            return PKC_ENTER;
    case SDL_SCANCODE_RGUI:               return PKC_RIGHT_OS;
    case SDL_SCANCODE_RIGHT:              return PKC_RIGHT_ARROW;
    case SDL_SCANCODE_RIGHTBRACKET:       return PKC_CLOSE_BRACKET;
    case SDL_SCANCODE_RSHIFT:             return PKC_RIGHT_SHIFT;
    case SDL_SCANCODE_S:                  return PKC_S;
    case SDL_SCANCODE_SCROLLLOCK:         return PKC_SCROLL_LOCK;
    //case SDL_SCANCODE_SELECT:             return PKC_;
    case SDL_SCANCODE_SEMICOLON:          return PKC_SEMICOLON;
    //case SDL_SCANCODE_SEPARATOR:          return PKC_;
    case SDL_SCANCODE_SLASH:              return PKC_SLASH;
    //case SDL_SCANCODE_SLEEP:              return PKC_;
    case SDL_SCANCODE_SPACE:              return PKC_SPACE;
    //case SDL_SCANCODE_STOP:               return PKC_;
    //case SDL_SCANCODE_SYSREQ:             return PKC_;
    case SDL_SCANCODE_T:                  return PKC_T;
    case SDL_SCANCODE_TAB:                return PKC_TAB;
    //case SDL_SCANCODE_THOUSANDSSEPARATOR: return PKC_;
    case SDL_SCANCODE_U:                  return PKC_U;
    //case SDL_SCANCODE_UNDO:               return PKC_;
    //case SDL_SCANCODE_UNKNOWN:            return PKC_;
    case SDL_SCANCODE_UP:                 return PKC_UP_ARROW;
    case SDL_SCANCODE_V:                  return PKC_V;
    //case SDL_SCANCODE_VOLUMEDOWN:         return PKC_;
    //case SDL_SCANCODE_VOLUMEUP:           return PKC_;
    case SDL_SCANCODE_W:                  return PKC_W;
    //case SDL_SCANCODE_WWW:                return PKC_;
    case SDL_SCANCODE_X:                  return PKC_X;
    case SDL_SCANCODE_Y:                  return PKC_Y;
    case SDL_SCANCODE_Z:                  return PKC_Z;
    default:                              return 0;
  }
}

