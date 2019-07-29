package org.plasmacore;

import android.app.*;
import android.graphics.*;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.view.KeyEvent;
import android.view.MotionEvent;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.opengles.GL10;

public class PlasmacoreView extends GLSurfaceView
{
  // Uses GL context-preserving technique from:
  //   https://stackoverflow.com/a/37298497/135791
  final static public int
    POINTER_MOVE    = 0,
    POINTER_PRESS   = 1,
    POINTER_RELEASE = 2;

  final  static public int EGL_CONTEXT_CLIENT_VERSION_VALUE = 2;
  static public EGLContext glContext = null;

  static public Builder builder( Activity activity )
  {
    return new Builder( activity );
  }

  static public class Builder
  {
    public Activity activity;
    public String   displayName = "Main";
    public boolean  translucent;

    public Builder( Activity activity )              { this.activity = activity; }
    public Builder setName( String name )            { this.displayName = name; return this; }
    public Builder setTranslucent( boolean setting ) { this.translucent = setting; return this; }

    public PlasmacoreView build() { return new PlasmacoreView(this); }
  }

  // PROPERTIES
  public Activity                activity;
  public String                  displayName;
  public PlasmacoreView.Renderer renderer;
  public boolean isChangingConfiguration;

  // METHODS
  public PlasmacoreView( Builder args )
  {
    super( args.activity );
    this.activity = args.activity;
    this.displayName = args.displayName;

    if (args.translucent)
    {
      getHolder().setFormat( PixelFormat.TRANSLUCENT );
    }

    // Set OpenGL ES 2.0
    setEGLContextClientVersion( EGL_CONTEXT_CLIENT_VERSION_VALUE );
    setEGLContextFactory(
        new GLSurfaceView.EGLContextFactory()
        {
          private final int EGL_CONTEXT_CLIENT_VERSION = 0x3098;

          public EGLContext createContext(EGL10 egl, EGLDisplay display, EGLConfig config )
          {
            if (glContext != null)
            {
              // Return retained context
              EGLContext eglContext = glContext;
              glContext = null;
              return eglContext;
            }

            int[] attributeList = { EGL_CONTEXT_CLIENT_VERSION, EGL_CONTEXT_CLIENT_VERSION_VALUE, EGL10.EGL_NONE };
            return egl.eglCreateContext(display, config, EGL10.EGL_NO_CONTEXT, attributeList );
          }

          public void destroyContext( EGL10 egl, EGLDisplay display, EGLContext context )
          {
            if (isChangingConfiguration)
            {
              // Don't destroy; retain
              glContext = context;
              return;
            }

            if ( !egl.eglDestroyContext(display,context) )
            {
              throw new RuntimeException("eglDestroyContext failed: error " + egl.eglGetError());
            }
          }
        }
    );

    setPreserveEGLContextOnPause( true );
    renderer = new PlasmacoreView.Renderer();
    setRenderer( renderer );
  }

  @Override
  public void onPause()
  {
    isChangingConfiguration = activity.isChangingConfigurations();
    super.onPause();
    Plasmacore.pause();
  }

  @Override
  public void onResume()
  {
    super.onResume();
    Plasmacore.resume();
  }


  public boolean onKeyDown( int keyCode, final KeyEvent event )
  {
    switch (keyCode)
    {
      case KeyEvent.KEYCODE_VOLUME_UP:
      case KeyEvent.KEYCODE_VOLUME_DOWN:
        return false;
    }

    int unicode      = event.getUnicodeChar();
    boolean isRepeat = (event.getRepeatCount() > 0);
    postKeyEvent( keyCode, PlasmacoreKeycode.fromSyscode(keyCode), unicode, true, isRepeat );

    return true;
  }

  public boolean onKeyUp( int keyCode, final KeyEvent event )
  {
    switch (keyCode)
    {
      case KeyEvent.KEYCODE_VOLUME_UP:
      case KeyEvent.KEYCODE_VOLUME_DOWN:
        return false;
    }

    int unicode      = event.getUnicodeChar();
    boolean isRepeat = (event.getRepeatCount() > 0);
    postKeyEvent( keyCode, PlasmacoreKeycode.fromSyscode(keyCode), unicode, false, isRepeat );

    return true;
  }

  public boolean onTouchEvent( MotionEvent e )
  {
    int type;
    switch (e.getActionMasked())
    {
      case MotionEvent.ACTION_MOVE: type = POINTER_MOVE;  break;
      case MotionEvent.ACTION_DOWN: type = POINTER_PRESS; break;
      default: type = POINTER_RELEASE;  // UP and CANCEL both count as release for Plasmacore (CANCEL sends extra cancelled:true)
    }

    int n = e.getPointerCount();

    int historyCount = e.getHistorySize();
    if (historyCount > 0)
    {
      long msDelta = System.currentTimeMillis() - e.getEventTime();
      for (int h=0; h<historyCount; ++h)
      {
        for (int i=0; i<n; ++i)
        {
          PlasmacoreMessage m = PlasmacoreMessage.create( "Display.on_pointer_event", (e.getHistoricalEventTime(h)+msDelta)/1000.0 );
          m.set( "display_name", displayName );
          m.set( "type", type );
          m.set( "x", e.getHistoricalX(i,h) );
          m.set( "y", e.getHistoricalY(i,h) );
          if (e.getActionMasked() == MotionEvent.ACTION_CANCEL) m.set( "cancelled", true );
          m.post();
        }
      }
    }

    for (int i=0; i<n; ++i)
    {
      PlasmacoreMessage m = PlasmacoreMessage.create( "Display.on_pointer_event" );
      m.set( "display_name", displayName );
      m.set( "type", type );
      m.set( "x", e.getX(i) );
      m.set( "y", e.getY(i) );
      m.post();
    }

    return true;
  }

  public void postKeyEvent( int syscode, int keycode, int unicode, boolean isPress, boolean isRepeat )
  {
    {
      PlasmacoreMessage m = PlasmacoreMessage.create( "Display.on_key_event" );
      m.set( "display_name", displayName );
      m.set( "keycode", keycode );
      m.set( "syscode", syscode );
      m.set( "is_press", isPress );
      if (isRepeat) m.set( "is_repeat", true );
      m.post();
    }

    if (unicode != 0)
    {
      PlasmacoreMessage m = PlasmacoreMessage.create( "Display.on_text_event" );
      m.set( "display_name", displayName );
      m.set( "character", unicode );
      m.post();
    }
  }

  public class Renderer implements GLSurfaceView.Renderer
  {
    public int displayWidth, displayHeight;

    public Renderer()
    {
      Plasmacore.configure( activity );
    }

    public void onDrawFrame( GL10 gl )
    {
      Plasmacore.sendPostedMessages();

      PlasmacoreMessage m = PlasmacoreMessage.create( "Display.on_render" );
      //m.set( "window_id", windowID )
      m.set( "display_name", displayName );;
      m.set( "display_width",  displayWidth );
      m.set( "display_height", displayHeight );
      m.send();
    }

    public void onSurfaceChanged( GL10 gl, int width, int height )
    {
      if (width != displayWidth || height != displayHeight)
      {
        Plasmacore.log( "Display surface size changed: " + width + "x" + height );
        displayWidth = width;
        displayHeight = height;
      }
    }

    public void onSurfaceCreated( GL10 gl, EGLConfig config )
    {
      Plasmacore.log( "Display surface created" );
      PlasmacoreMessage m = PlasmacoreMessage.create( "Display.on_graphics_lost" );
      m.set( "display_name", displayName );
      m.post();
      Plasmacore.sendPostedMessages();
    }
  }
}
