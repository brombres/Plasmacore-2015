package org.plasmacore;

import android.util.*;

public class Plasmacore
{
  static boolean  isLaunched;
  static ByteList inputMessageQueue   = new ByteList( 1024 );
  static ByteList outputMessageQueue  = new ByteList( 1024 );
  static ByteList directMessageBuffer = new ByteList( 128 );
  static String   mutex = new String( "mutex" );

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
      PlasmacoreMessage.create( "Application.on_launch" ).post();
    }
  }

  static public boolean dispatchDirectMessage()
  {
    PlasmacoreMessage m = PlasmacoreMessage.create( directMessageBuffer );
    dispatch( m );
    m.recycle();
    return false;
  }

  static public void dispatch( PlasmacoreMessage m )
  {
    log( "TODO: dispatch received message " + m.type );
  }

  static public void log( String message )
  {
    Log.i( "Plasmacore", message );
  }

  static public void post( PlasmacoreMessage m )
  {
    outputMessageQueue.reserve( m.data.count + 4 );
    outputMessageQueue.writeInt32( m.data.count );
    outputMessageQueue.add( m.data );
    m.isSent = true;
    m.recycle();
  }

  static public PlasmacoreMessage send( PlasmacoreMessage m )
  {
    directMessageBuffer.clear().add( m.data );
    if (nativeSendMessage(directMessageBuffer))
    {
      // directMessageBuffer message has been replace with reply.
      return PlasmacoreMessage.create( directMessageBuffer );
    }
    else
    {
      return null;
    }
  }

  static public void update()
  {
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

