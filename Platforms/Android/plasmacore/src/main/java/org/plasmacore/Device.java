package org.plasmacore;

import android.app.Activity;
import android.os.Build;
import android.view.DisplayCutout;
import android.util.DisplayMetrics;
import android.view.WindowInsets;

public class Device
{
  public Activity activity;
  public int      isTablet = -1;
  public double   displayDensity;

  public int      safeInsetLeft   = -1;
  public int      safeInsetTop    = -1;
  public int      safeInsetRight  = -1;
  public int      safeInsetBottom = -1;

  public Device( Activity activity )
  {
    this.activity = activity;
  }

  public void cacheSafeInsets()
  {
    safeInsetLeft = safeInsetTop = safeInsetRight = safeInsetBottom = 0;
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
    {
      WindowInsets windowInsets = activity.getWindow().getDecorView().getRootWindowInsets();
      if (windowInsets != null)
      {
        DisplayCutout displayCutout = windowInsets.getDisplayCutout();
        if (displayCutout != null)
        {
          safeInsetLeft   = displayCutout.getSafeInsetLeft();
          safeInsetRight  = displayCutout.getSafeInsetRight();
          safeInsetTop    = displayCutout.getSafeInsetTop();
          safeInsetBottom = displayCutout.getSafeInsetBottom();
        }
      }
    }
  }

  public double displayDensity()
  {
    if (displayDensity == 0)
    {
      // On iOS display density correlates to 200 PPI bins. We'll use that approach here.
      double ppi = ppi();
      displayDensity = Math.floor( ppi / 200 ) + 1;
    }
    return displayDensity;
  }

  public double ppi()
  {
    DisplayMetrics dm = activity.getResources().getDisplayMetrics();
    return (dm.xdpi + dm.ydpi) / 2.0;
  }

  public boolean isTablet()
  {
    if (isTablet == -1)
    {
      DisplayMetrics dm = activity.getResources().getDisplayMetrics();
      double ppi = ppi();
      double w = dm.widthPixels  / ppi;
      double h = dm.heightPixels / ppi;

      isTablet = (w*w + h*h >= 6.5*6.5) ? 1 : 0;  // Diagonal of 6.5"+ counts as a tablet
    }
    return (isTablet == 1);
  }

  public int safeInsetLeft()
  {
    if (safeInsetLeft == -1) cacheSafeInsets();
    return safeInsetLeft;
  }

  public int safeInsetRight()
  {
    if (safeInsetRight == -1) cacheSafeInsets();
    return safeInsetRight;
  }

  public int safeInsetTop()
  {
    if (safeInsetTop == -1) cacheSafeInsets();
    return safeInsetTop;
  }

  public int safeInsetBottom()
  {
    if (safeInsetBottom == -1) cacheSafeInsets();
    return safeInsetBottom;
  }
}

