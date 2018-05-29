package org.plasmacore;

//==============================================================================
// PlasmacoreMessage.rogue
//
// Communication mechanism between Plasmacore and Native Layer.
//
// post()ing a message adds it to a queue that is sent en mass during the next
// update or before a send().
//
// send()ing a message pushes the message queue and then sends the current
// message directly. It allows for a message in response.
//
// High-low byte order for Int32, Int64, and Real64
//
// Message Queue
//   while (has_another)
//     message_size : Int32              # number of bytes that follow not including this
//     message      : Byte[message_size]
//   endWhile
//
// Message
//   type_name_count : Int32                 # 0 means a reply
//   type_name_utf8  : UTF8[type_name_count]
//   message_id      : Int32                 # serial number (always present, only needed with RSVP replies)
//   timestamp       : Real64
//   while (position < message_size)
//     arg_name_count : Int32
//     arg_name       : UTF8[arg_name_count]
//     arg_data_type  : Byte
//     arg_data_size  : Int32
//     arg_data       : Byte[arg_data_size]
//   endWhile
//
// Data Types
//   DATA_TYPE_REAL64 = 1   # value:Int64 (Real64.integer_bits stored)
//   DATA_TYPE_INT64  = 2   # high:Int32, low:Int32
//   DATA_TYPE_INT32  = 3   # value:Int32
//   DATA_TYPE_BYTE   = 4   # value:Byte
//
//==============================================================================

import java.util.*;

public class PlasmacoreMessage
{
  /*
  // ENUMERATE
  final static int DATA_TYPE_REAL64 = 1;
  final static int DATA_TYPE_INT64  = 2;
  final static int DATA_TYPE_INT32  = 3;
  final static int DATA_TYPE_BYTE   = 4;

  // GLOBAL PROPERTIES
  static int nextMessageID = 1;
  static HashMap<String,String> consolidationTable = new HashMap<String,String>();
  static ArrayList<PlasmacoreMessage> messagePool = new ArrayList<PlasmacoreMessage>();

  // GLOBAL METHODS
  static String consolidate( String text )
  {
    String result = consolidationTable.get( text );
    if (result != null) return result;
    consolidationTable.put( text, text );
    return text;
  }

  static PlasmacoreMessage create()
  {
    int n = messagePool.size();
    if (n == 0) return new PlasmacoreMessage();
    return messagePool.remove( n - 1 ).reset();
  }

  static PlasmacoreMessage create( String type )
  {
    return create().init( type, nextMessageID++ );
  }

  static PlasmacoreMessage create( byte[] data )
  {
    return create().init( data );
  }

  // PROPERTIES
  public String  type;
  public int     messageID;
  public double  timestamp;
  public byte[]  data = new byte[128];
  public int     count;    // data size; data.length is capacity
  public int     position; // read position (count is write position)
  public boolean isSent;
  public boolean isRecycled;
  public PlasmacoreMessage reply;

  // METHODS
  public PlasmacoreMessage()
  {
    // no action
    type = "";
  }

  public PlasmacoreMessage reset()
  {
    count = 0;
    position = 0;
    isSent = false;
    isRecycled = false;
    reply = null;
  }

  public PlasmacoreMessage init( String type, int messageID )
  {
    reset();
    this.type = consolidate( type );
    this.messageID = messageID;
    timestamp = System.currentTimeMillis() / 1000.0;

    _writeString( type );
    _writeInt32( messageID );
    _writeReal64( timestamp );
  }

  public PlasmacoreMessage init( byte[] data )
  {
    reset();

    // Copy in data
    if (data.length > this.data.length)
    {
      this.data = new byte[ data.length ];
    }
    for (int i=data.length; --i>=0; )
    {
      this.data[i] = data[i];
    }

    // Read out header
    type = consolidate( _readString() );
    messageID = _readInt32();
    timestamp = _readReal64();
  }

  public int _readByte()
  {
    if (position == count) return 0;
    return ((int) data[ position++ ]) & 255;
  }

  public int _readInt32()
  {
    int result = _readByte() << 24;
    result |= _readByte() << 16;
    result |= _readByte() << 8;
    return result | _readByte();
  }

  public long _readInt64()
  {
    long result = ((long)_readInt32()) << 32;
    return result | (((long)_readInt32()) & 0xFFFFffffL);
  }

  public double _readReal64()
  {
    return Double.longBitsToDouble( _readInt64() );
  }

  public String _readString()
  {
  }

  public void _recycle()
  {
    if (isRecycled) return;
    isRecycled = true;
    if (data.length > 1024)
    {
      data = new byte[ 1024 ];
    }
    messagePool.add( this );
  }

  public PlasmacoreMessage _reserve( int additional )
  {
    int requiredCapacity = count + additional;
    if (requiredCapacity <= data.length) return this;

    int newCapacity = data.length * 2;
    if (requiredCapacity > newCapacity) newCapacity = requiredCapacity;

    byte[] newData = new byte[ requiredCapacity ];
    for (int i=count; --i>=0; )
    {
      newData[i] = data[i];
    }
    data = newData;

    return this;
  }

  public void _writeByte( int value )
  {
    _reserve( 1 );
    data[ count++ ] = (byte) value;
  }

  public void _writeInt32( int value )
  {
    _reserve( 4 );
    _writeByte( value >> 24 );
    _writeByte( value >> 16 );
    _writeByte( value >> 8 );
    _writeByte( value );
  }

  public void _writeInt64( long value )
  {
    _reserve( 8 );
    _writeInt32( (int) (value >> 32) );
    _writeInt32( (int) value );
  }

  public void _writeReal64( double value )
  {
    _writeInt64( Double.doubleToLongBits(value) );
  }
  */
}

