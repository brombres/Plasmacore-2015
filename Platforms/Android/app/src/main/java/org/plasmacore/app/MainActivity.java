package org.plasmacore.app;

import android.app.Activity;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

public class MainActivity extends Activity
{

  // Used to load the 'native-lib' library on application startup.
  static
  {
    System.loadLibrary("native-plasmacore");

  }

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);

    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        WindowManager.LayoutParams.FLAG_FULLSCREEN);

    // Example of a call to a native method
    TextView tv = new TextView( this );
    int MATCH_PARENT = RelativeLayout.LayoutParams.MATCH_PARENT;
    RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams( MATCH_PARENT, MATCH_PARENT );
    tv.setLayoutParams( layoutParams );
    tv.setText(stringFromJNI2());
    setContentView( tv );

  }

  /**
   * A native method that is implemented by the 'native-lib' native library,
   * which is packaged with this application.
   */
  public native String stringFromJNI2();
}
