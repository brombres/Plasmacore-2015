package org.plasmacore.app;

import android.app.Activity;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

import org.plasmacore.*;

public class MainActivity extends Activity
{
  public PlasmacoreView plasmacoreView;

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);

    // Turn off title bar
    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        WindowManager.LayoutParams.FLAG_FULLSCREEN);

    enableAutoHideNavBar();

    final MainActivity THIS = this;
    View decorView = getWindow().getDecorView();
    decorView.setOnSystemUiVisibilityChangeListener(
        new View.OnSystemUiVisibilityChangeListener()
        {
          @Override
          public void onSystemUiVisibilityChange( int isVisible )
          {
            if((isVisible & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0)
            {
              THIS.enableAutoHideNavBar();
            }
          }
        }
    );

    plasmacoreView = PlasmacoreView.builder(this).build();
    setContentView( plasmacoreView );
  }

  protected void enableAutoHideNavBar()
  {
    View decorView = getWindow().getDecorView();
    if (decorView != null)
    {
      int flags = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
      decorView.setSystemUiVisibility( flags );
    }
  }

  @Override
  protected void onPause()
  {
    super.onPause();
    plasmacoreView.onPause();
  }

  @Override
  protected void onResume()
  {
    super.onResume();
    plasmacoreView.onResume();
  }

  @Override
  public void onWindowFocusChanged( boolean hasFocus )
  {
    super.onWindowFocusChanged( hasFocus );
    enableAutoHideNavBar();
  }

}
