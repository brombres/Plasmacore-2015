package org.plasmacore;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.*;
import android.util.*;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.*;
import java.util.*;

public class Plasmacore
{
  static public boolean  isLaunched;
  static public boolean  isConfigured;
  static public ByteList inputMessageQueue   = new ByteList( 1024 );
  static public ByteList outputMessageQueue  = new ByteList( 1024 );
  static public ByteList ioBuffer = new ByteList( 128 );  // used for direct message i/o and bitmap decoding
  static public String   mutex = new String( "mutex" );

  static public Activity activity;
  static public Device   device;

  static public String   applicationDataFolder;
  static public String   userDataFolder;
  static public String   cacheFolder;

  static public PlasmacoreSoundManager soundManager;

  static public HashMap<String,PlasmacoreMessageListener> messageListeners = new HashMap<String,PlasmacoreMessageListener>();

  static
  {
    System.loadLibrary("plasmacore");
  }

  static public void launch( Activity activity )
  {
    if ( !isLaunched )
    {
      synchronized (mutex)
      {
        configure( activity );
        isLaunched = true;
        nativeLaunch();

        PlasmacoreMessage m = PlasmacoreMessage.create( "Application.on_launch" );
        m.set( "application_data_folder", applicationDataFolder );
        m.set( "user_data_folder", userDataFolder );
        m.set( "cache_folder", cacheFolder );
        m.post();
      }
    }
  }

  static public void configure( Activity activity )
  {
    Plasmacore.activity = activity;
    Plasmacore.device = new Device( activity );

    if (isConfigured) return;
    isConfigured = true;

    try
    {
      cacheFolder = activity.getCacheDir().getCanonicalPath();
    }
    catch (IOException err)
    {
      cacheFolder = activity.getCacheDir().getPath();
    }

    try
    {
      applicationDataFolder = activity.getFilesDir().getCanonicalPath();
    }
    catch (IOException err)
    {
      applicationDataFolder = activity.getFilesDir().getPath();
    }

    userDataFolder = applicationDataFolder;

    soundManager = new PlasmacoreSoundManager();

    setMessageListener(
        "Plasmacore.find_asset",
        new PlasmacoreMessageListener()
        {
          public void on( PlasmacoreMessage m )
          {
            try
            {
              String filepath = m.getString( "filepath" );  // will be e.g.: Assets/Images/Image.png

              int last_slash = filepath.lastIndexOf( '/' );
              File destFolder = new File( (last_slash==-1)?cacheFolder:cacheFolder+"/"+filepath.substring(0,last_slash) );
              if ( !destFolder.exists() ) destFolder.mkdirs();

              String resolvedFilepath = cacheFolder + "/" + filepath;
              File file = new File( resolvedFilepath );

              // If a requested asset is not in the cache folder then we attempt
              // to find and copy it from bundled assets.
              if (file.exists() || Plasmacore.copyAsset(filepath,file))
              {
                m.reply().set( "filepath", resolvedFilepath );
                return;
              }
            }
            catch (Exception failed)
            {
              // no response
            }
          }
        }
    );

    setMessageListener(
        "Display.density",
        new PlasmacoreMessageListener()
        {
          public void on( PlasmacoreMessage m )
          {
            m.reply().set( "density", Plasmacore.device.displayDensity() );
          }
        }
    );

    setMessageListener(
        "Display.is_tablet",
        new PlasmacoreMessageListener()
        {
          public void on( PlasmacoreMessage m )
          {
            m.reply().set( "is_tablet", Plasmacore.device.isTablet() );
          }
        }
    );

    final Activity ACTIVITY = activity;
    setMessageListener(
        "Display.allow_orientation",
        new PlasmacoreMessageListener()
        {
          public void on( PlasmacoreMessage m )
          {
            boolean allow_portrait  = m.getBoolean( "allow_portrait" );
            boolean allow_landscape = m.getBoolean( "allow_landscape" );
            int orientation = 0;
            if (allow_portrait ^ allow_landscape)
            {
              if (allow_landscape)
              {
                orientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE;
              }
              else
              {
                orientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT;
              }
            }
            else
            {
              orientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE | ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT;
            }
            ACTIVITY.setRequestedOrientation( orientation );
          }
        }
    );

    setMessageListener(
        "Display.safe_insets",
        new PlasmacoreMessageListener()
        {
          public void on( PlasmacoreMessage m )
          {
            PlasmacoreMessage reply = m.reply();
            reply.set( "left",   device.safeInsetLeft() );
            reply.set( "right",  device.safeInsetRight() );
            reply.set( "top",    device.safeInsetTop() );
            reply.set( "bottom", device.safeInsetBottom() );
            reply.send();
          }
        }
    );
  }

  static boolean copyAsset( String filepath, File toFile )
  {
    if (filepath.startsWith("Assets/"))
    {
      filepath = filepath.substring( 7 );
    }

    BufferedInputStream infile = null;
    BufferedOutputStream outfile = null;
    try
    {
      infile  = new BufferedInputStream( activity.getAssets().open(filepath), 1024 );
      outfile = new BufferedOutputStream( new FileOutputStream(toFile), 1024 );

      for (int ch=infile.read(); ch!=-1; ch=infile.read())
      {
        outfile.write( ch );
      }

      infile.close();
      infile = null;

      outfile.close();
      outfile = null;

      return true;
    }
    catch (IOException err)
    {
      try
      {
        if (infile != null)  infile.close();
        if (outfile != null) outfile.close();
      }
      catch (IOException ignore)
      {
      }
    }
    return false;
  }

  static public boolean dispatchDirectMessage()
  {
    PlasmacoreMessage m = PlasmacoreMessage.create( ioBuffer );
    dispatch( m );
    if (m._reply != null)
    {
      ioBuffer.clear().add( m._reply.data );
      m._reply.isSent = true;
      m.recycle();
      return true;
    }
    else
    {
      m.recycle();
      return false;
    }
  }

  static public void dispatch( PlasmacoreMessage m )
  {
    PlasmacoreMessageListener listener = messageListeners.get( m.type );
    if (listener != null)
    {
      listener.on( m );
    }
  }

  static public int decodeImage()
  {
    // Decodes image bytes that have been placed in ioBuffer.
    // On success, places decoded data in ioBuffer and returns width of image.
    // On failure returns 0.
    Plasmacore.log( "Decode image: " + ioBuffer.count + " bytes" );
    try
    {
      Bitmap bitmap = BitmapFactory.decodeByteArray( ioBuffer.bytes, 0, ioBuffer.count );
      int width  = bitmap.getWidth();
      int height = bitmap.getHeight();
      Plasmacore.log( "Decoded image is " + width + "x" + height );
      int byteCount = width * height * 4;
      ioBuffer.clear().reserve( byteCount );
      ioBuffer.count = byteCount;
      ByteBuffer buffer = ByteBuffer.wrap( ioBuffer.bytes );
      bitmap.copyPixelsToBuffer( buffer );
      return width;
    }
    catch (Exception err)
    {
      Plasmacore.logError( "Failed to decode image." );
      return 0;
    }
  }

  static public void log( String message )
  {
    Log.i( "Plasmacore", message );
  }

  static public void logError( String message )
  {
    Log.e( "Plasmacore", message );
  }

  static public void pause()
  {
    log( "Plasmacore.pause()" );
    if (soundManager != null) soundManager.pauseAll();
    PlasmacoreMessage.create( "Application.on_save" ).send();
    PlasmacoreMessage.create( "Application.on_stop" ).send();
  }

  static public void resume()
  {
    log( "Plasmacore.resume()" );
    if (soundManager != null) soundManager.resumeAll();
    PlasmacoreMessage.create( "Application.on_start" ).post();
  }

  static public void post( PlasmacoreMessage m )
  {
    synchronized (mutex)
    {
      outputMessageQueue.reserve( m.data.count + 4 );
      outputMessageQueue.writeInt32( m.data.count );
      outputMessageQueue.add( m.data );
      m.isSent = true;
      m.recycle();
    }
  }

  static public PlasmacoreMessage send( PlasmacoreMessage m )
  {
    synchronized (mutex)
    {
      ioBuffer.clear().add( m.data );
      if (nativeSendMessage(ioBuffer))
      {
        // ioBuffer message has been replace with reply.
        return PlasmacoreMessage.create( ioBuffer );
      }
      else
      {
        return null;
      }
    }
  }

  static public void setMessageListener( String type, PlasmacoreMessageListener listener )
  {
    messageListeners.put( type, listener );
  }

  static public void removeMessageListener( String type )
  {
    messageListeners.put( type, null );
  }

  static public void sendPostedMessages()
  {
    if ( !isLaunched ) launch( activity );

    synchronized (mutex)
    {
      ByteList temp = inputMessageQueue.clear();
      inputMessageQueue = outputMessageQueue;
      outputMessageQueue = temp;

      if (nativePostMessages(inputMessageQueue))
      {
        int readPos = 0;
        while (readPos < inputMessageQueue.count)
        {
          int messageSize = inputMessageQueue.readInt32( readPos );
          readPos += 4;
          PlasmacoreMessage m = PlasmacoreMessage.create( inputMessageQueue.bytes, readPos, messageSize );
          readPos += messageSize;
          dispatch( m );
          m.recycle();
        }
      }
    }
  }

  native static void    nativeLaunch();
  native static boolean nativePostMessages( ByteList queue );
  native static void    nativeQuit();
  native static boolean nativeSendMessage( ByteList data );
}

