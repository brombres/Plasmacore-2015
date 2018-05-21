package org.plasmacore.framework;

public class Rogue
{
  static
  {
    System.loadLibrary("plasmacore");

  }

  static public void launch()
  {
    nativeLaunch();
  }

  native static void nativeLaunch();
  native static void nativeQuit();
}

