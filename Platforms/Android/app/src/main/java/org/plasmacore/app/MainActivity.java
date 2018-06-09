package org.plasmacore.app;

import android.app.Activity;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

import org.plasmacore.*;

public class MainActivity extends Activity
{
  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);

    // Turn off title bar
    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        WindowManager.LayoutParams.FLAG_FULLSCREEN);

    setContentView( PlasmacoreView.args(this).create() );
  }
}

