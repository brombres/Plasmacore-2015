package org.plasmacore;

import android.graphics.*;
import android.util.*;

import java.nio.*;
import java.util.*;

public class Plasmacore
{
  static boolean  isLaunched;
  static boolean  isConfigured;
  static ByteList inputMessageQueue   = new ByteList( 1024 );
  static ByteList outputMessageQueue  = new ByteList( 1024 );
  static ByteList ioBuffer = new ByteList( 128 );  // used for direct message i/o and bitmap decoding
  static String   mutex = new String( "mutex" );

  static public HashMap<String,PlasmacoreMessageListener> messageListeners = new HashMap<String,PlasmacoreMessageListener>();

  static
  {
    System.loadLibrary("plasmacore");
    configure();
  }

  static public void launch()
  {
    if ( !isLaunched )
    {
      synchronized (mutex)
      {
        isLaunched = true;
        nativeLaunch();
        PlasmacoreMessage.create( "Application.on_launch" ).post();
      }
    }
  }

  static public void configure()
  {
    if (isConfigured) return;
    isConfigured = true;
  }

  static public boolean dispatchDirectMessage()
  {
    PlasmacoreMessage m = PlasmacoreMessage.create( ioBuffer );
    dispatch( m );
    m.recycle();
    return false;
  }

  static public void dispatch( PlasmacoreMessage m )
  {
    log( "TODO: dispatch received message " + m.type );
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

  static public void update()
  {
    if ( !isLaunched ) launch();

    synchronized (mutex)
    {
      if (nativePostMessages(outputMessageQueue))
      {
        ByteList temp = inputMessageQueue;
        inputMessageQueue = outputMessageQueue;
        outputMessageQueue = temp.clear();

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
      else
      {
        outputMessageQueue.clear();
      }
    }
  }

  native static void    nativeLaunch();
  native static boolean nativePostMessages( ByteList queue );
  native static void    nativeQuit();
  native static boolean nativeSendMessage( ByteList data );
}

