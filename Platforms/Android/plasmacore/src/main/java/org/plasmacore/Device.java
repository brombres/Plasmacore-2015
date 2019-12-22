package org.plasmacore;

import android.app.Activity;
import android.util.DisplayMetrics;

public class Device
{
  public Activity activity;
  public int      isTablet = -1;
  public double   displayDensity;

  public Device( Activity activity )
  {
    this.activity = activity;
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
}

