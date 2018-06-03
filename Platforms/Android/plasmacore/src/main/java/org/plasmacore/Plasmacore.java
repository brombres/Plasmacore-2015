package org.plasmacore;

public class Plasmacore
{
  static boolean isLaunched;

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

  native static void nativeLaunch();
  native static void nativeQuit();
}

