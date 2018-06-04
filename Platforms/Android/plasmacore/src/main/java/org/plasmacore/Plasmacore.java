package org.plasmacore;

import android.util.*;

public class Plasmacore
{
  static boolean    isLaunched;
  static ByteBuffer messageBuffer = new ByteBuffer( 1024 );

  static
  {
    System.loadLibrary("plasmacore");

  }

  static public void launch()
  {
    if ( !isLaunched )
    {
      isLaunched = true;
      nativeLaunch();
    }
  }

  static public void log( String message )
  {
    Log.i( "Plasmacore", message );
  }

  native static void nativeLaunch();
  native static void nativeQuit();
}

