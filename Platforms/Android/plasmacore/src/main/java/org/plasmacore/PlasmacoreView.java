package org.plasmacore;

import android.app.*;
import android.graphics.*;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class PlasmacoreView extends GLSurfaceView
{
  public Activity                activity;
  public PlasmacoreView.Renderer renderer;

  public PlasmacoreView( Activity activity )
  {
    this( activity, false );
  }

  public PlasmacoreView( Activity activity, boolean translucent )
  {
    super( activity );
    this.activity = activity;
    if (translucent)
    {
      getHolder().setFormat( PixelFormat.TRANSLUCENT );
    }

    // Set OpenGL ES 2.0
    setEGLContextClientVersion( 2 );

    setPreserveEGLContextOnPause( true );
    renderer = new PlasmacoreView.Renderer();
    setRenderer( renderer );
  }

  public class Renderer implements GLSurfaceView.Renderer
  {
    int displayWidth, displayHeight;

    public void onDrawFrame( GL10 gl )
    {
      Plasmacore.launch();

      gl.glClearColor( 0, 0, 1, 1 );
      gl.glClear( GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT );
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

