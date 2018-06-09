package org.plasmacore;

import android.app.*;
import android.graphics.*;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.view.KeyEvent;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class PlasmacoreView extends GLSurfaceView
{
  static public class Args
  {
    public Activity activity;
    public String   displayName;
    public boolean  translucent;

    public Args( Activity activity )              { this.activity = activity; }
    public Args setName( String name )            { this.displayName = name; return this; }
    public Args setTranslucent( boolean setting ) { this.translucent = setting; return this; }

    public PlasmacoreView create() { return new PlasmacoreView(this); }
  }

  static public Args args( Activity activity )
  {
    return new Args( activity );
  }

  // PROPERTIES
  public Activity                activity;
  public String                  displayName;
  public PlasmacoreView.Renderer renderer;

  // METHODS
  public PlasmacoreView( Args args )
  {
    super( args.activity );
    this.activity = args.activity;
    this.displayName = args.displayName;

    if (args.translucent)
    {
      getHolder().setFormat( PixelFormat.TRANSLUCENT );
    }

    // Set OpenGL ES 2.0
    setEGLContextClientVersion( 2 );

    setPreserveEGLContextOnPause( true );
    renderer = new PlasmacoreView.Renderer();
    setRenderer( renderer );
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

  /*
  public boolean onTouchEvent( final MotionEvent event )
  {
    try
    {
      switch(event.getAction())
      {
        case MotionEvent.ACTION_DOWN:
        case 5:  //MotionEvent.ACTION_POINTER_1_DOWN:
          begin_touch( event.getX(), event.getY() );
          break;

        case MotionEvent.ACTION_UP:
        case 6:  //MotionEvent.ACTION_POINTER_1_UP:
          end_touch( event.getX(), event.getY() );
          break;

        case 261: //MotionEvent.ACTION_POINTER_2_DOWN:
          if (m_getX != null)
          {
            double x = (Float) m_getX.invoke( event, 1 );
            double y = (Float) m_getY.invoke( event, 1 );
            begin_touch( x, y );
          }
          break;

        case 262: //MotionEvent.ACTION_POINTER_2_UP:
          if (m_getX != null)
          {
            double x = (Float) m_getX.invoke( event, 1 );
            double y = (Float) m_getY.invoke( event, 1 );
            end_touch( x, y );
          }
          break;


        case 517: //MotionEvent.ACTION_POINTER_3_DOWN:
          if (m_getX != null)
          {
            double x = (Float) m_getX.invoke( event, 2 );
            double y = (Float) m_getY.invoke( event, 2 );
            begin_touch( x, y );
          }
          break;

        case 518: //MotionEvent.ACTION_POINTER_3_UP:
          if (m_getX != null)
          {
            double x = (Float) m_getX.invoke( event, 2 );
            double y = (Float) m_getY.invoke( event, 2 );
            end_touch( x, y );
          }
          break;

        case MotionEvent.ACTION_MOVE:
          if (m_getPointerCount == null)
          {
            update_touch( event.getX(), event.getY() );
          }
          else
          {
            int count = (Integer) m_getPointerCount.invoke( event );
            for(int i = 0; i < count; ++i)
            {
              int id = (Integer) m_getPointerId.invoke( event, i );
              double x = (Float) m_getX.invoke( event, id );
              double y = (Float) m_getY.invoke( event, id );
              update_touch( x, y );
            }
          }
          break;
      }
    }
    catch (Exception err)
    {
      // never gonna happen
    }

    return true;
  }
*/

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
    int displayWidth, displayHeight;

    public void onDrawFrame( GL10 gl )
    {
      Plasmacore.update();  // flush posted messages

      PlasmacoreMessage m = PlasmacoreMessage.create( "Display.on_render" );
      //m.set( "window_id", windowID )
      m.set( "display_name", "Main" );;
      m.set( "display_width",  displayWidth );
      m.set( "display_height", displayHeight );
      m.send();
    }

    public void onSurfaceChanged( GL10 gl, int width, int height )
    {
      displayWidth = width;
      displayHeight = height;
    }

    public void onSurfaceCreated( GL10 gl, EGLConfig config )
    {
    }
  }
}

