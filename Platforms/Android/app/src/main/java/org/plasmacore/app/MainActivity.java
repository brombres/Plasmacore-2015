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

    plasmacoreView = PlasmacoreView.builder(this).build();
    setContentView( plasmacoreView );
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
}

