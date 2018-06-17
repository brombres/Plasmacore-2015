package org.plasmacore;

//==============================================================================
// PlasmacoreMessage.rogue
//
// Communication mechanism between Plasmacore and Native Layer (Java).
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
  // ENUMERATE
  final static int DATA_TYPE_REAL64 = 1;
  final static int DATA_TYPE_INT64  = 2;
  final static int DATA_TYPE_INT32  = 3;
  final static int DATA_TYPE_BYTE   = 4;

  // GLOBAL PROPERTIES
  static int nextMessageID = 1;
  static HashMap<Comparable<String>,String> consolidationTable = new HashMap<Comparable<String>,String>();
  static ArrayList<PlasmacoreMessage> messagePool = new ArrayList<PlasmacoreMessage>();
  static String mutex = new String( "mutex" );

  // GLOBAL METHODS
  static String consolidate( String text )
  {
    synchronized (mutex)
    {
      String result = consolidationTable.get( text );
      if (result != null) return result;
      consolidationTable.put( text, text );
      return text;
    }
  }

  static String consolidate( ComparableStringBuilder builder )
  {
    synchronized (mutex)
    {
      String result = consolidationTable.get( builder );
      if (result != null) return result;
      String text = builder.toString();
      consolidationTable.put( text, text );
      return text;
    }
  }

  static PlasmacoreMessage create()
  {
    synchronized (mutex)
    {
      int n = messagePool.size();
      if (n == 0) return new PlasmacoreMessage();
      return messagePool.remove( n - 1 ).reset();
    }
  }

  static PlasmacoreMessage create( String type ) {
    synchronized (mutex)
    {
      return create( type, nextMessageID++ );
    }
  }

  static PlasmacoreMessage create( String type, double timestamp )
  {
    synchronized (mutex)
    {
      return create( type, nextMessageID++, timestamp );
    }
  }

  static PlasmacoreMessage create( int replyToMessageID )
  {
    return create( "", replyToMessageID );
  }

  static PlasmacoreMessage create( String type, int messageID )
  {
    return create( type, messageID, (System.currentTimeMillis() / 1000.0) );
  }

  static PlasmacoreMessage create( String type, int messageID, double timestamp )
  {
    return create().init( type, messageID, timestamp );
  }

  static PlasmacoreMessage create( ByteList data )
  {
    return create( data.bytes, 0, data.count );
  }

  static PlasmacoreMessage create( byte[] data )
  {
    return create( data, 0, data.length );
  }

  static PlasmacoreMessage create( byte[] data, int offset, int n )
  {
    return create().init( data, offset, n );
  }

  // PROPERTIES
  public String   type;
  public int      messageID;
  public double   timestamp;
  public ByteList data = new ByteList();
  public int      position; // read position (data.count is write position)
  public int      argStartPosition;
  public boolean  isSent;
  public boolean  isRecycled;
  public PlasmacoreMessage _reply;

  ComparableStringBuilder builder = new ComparableStringBuilder();
  UTF8Writer utf8Writer = new UTF8Writer();

  // METHODS
  public PlasmacoreMessage()
  {
    // no action
    type = "";
  }

  public PlasmacoreMessage reset()
  {
    data.clear();
    position = 0;
    isSent = false;
    isRecycled = false;
    _reply = null;
    argStartPosition = 0;
    builder.clear();
    return this;
  }

  public PlasmacoreMessage init( String type, int messageID, double timestamp )
  {
    reset();
    this.type = consolidate( type );
    this.messageID = messageID;
    this.timestamp = timestamp;

    _writeString( type );
    _writeInt32( messageID );
    _writeReal64( timestamp );

    argStartPosition = data.count;

    return this;
  }

  public PlasmacoreMessage init( byte[] data, int offset, int n )
  {
    reset();

    this.data.clear().add( data, offset, n );
    initFromData();

    return this;
  }

  public void initFromData()
  {
    // Assumes that reset() has been called and that 'data' has been configured.
    type = consolidate( _readString() );
    messageID = _readInt32();
    timestamp = _readReal64();

    argStartPosition = position;
  }

  public boolean getBoolean( String key )
  {
    return getByte( key ) != 0;
  }

  public byte getByte( String key )
  {
    if ( !_seek(key) ) return 0;

    int arg_type = _readByte();
    int arg_size = _readInt32();
    if (arg_size == 0) return 0;

    switch (arg_type)
    {
      case DATA_TYPE_BYTE:   return (byte)_readByte();
      case DATA_TYPE_INT32:  return (byte)_readInt32();
      case DATA_TYPE_INT64:  return (byte)_readInt64();
      case DATA_TYPE_REAL64: return (byte)_readReal64();
    }

    return 0;
  }

  public byte[] getBytes( String key )
  {
    if ( !_seek(key) ) return new byte[0];
    _readByte();
    int count = _readInt32();
    byte[] result = new byte[ count ];
    getBytes( key, result, 0, count );
    return result;
  }

  public int getBytes( String key, byte[] buffer, int offset, int max )
  {
    if ( !_seek(key) ) return 0;

    int arg_type = _readByte();
    int arg_size = _readInt32();
    if (arg_size == 0) return 0;

    int n = Math.min( arg_size, max );
    for (int i=0; i<n; ++i)
    {
      buffer[ i+offset ] = (byte)_readByte();
    }

    return n;
  }

  public double getDouble( String key )
  {
    if ( !_seek(key) ) return 0;

    int arg_type = _readByte();
    int arg_size = _readInt32();
    if (arg_size == 0) return 0;

    switch (arg_type)
    {
      case DATA_TYPE_BYTE:   return _readByte();
      case DATA_TYPE_INT32:  return _readInt32();
      case DATA_TYPE_INT64:  return _readInt64();
      case DATA_TYPE_REAL64: return _readReal64();
    }

    return 0;
  }

  public int getInt( String key )
  {
    if ( !_seek(key) ) return 0;

    int arg_type = _readByte();
    int arg_size = _readInt32();
    if (arg_size == 0) return 0;

    switch (arg_type)
    {
      case DATA_TYPE_BYTE:   return _readByte();
      case DATA_TYPE_INT32:  return _readInt32();
      case DATA_TYPE_INT64:  return (int)_readInt64();
      case DATA_TYPE_REAL64: return (int)_readReal64();
    }

    return 0;
  }

  public long getLong( String key )
  {
    if ( !_seek(key) ) return 0;

    int arg_type = _readByte();
    int arg_size = _readInt32();
    if (arg_size == 0) return 0;

    switch (arg_type)
    {
      case DATA_TYPE_BYTE:   return _readByte();
      case DATA_TYPE_INT32:  return _readInt32();
      case DATA_TYPE_INT64:  return _readInt64();
      case DATA_TYPE_REAL64: return (long) _readReal64();
    }

    return 0;
  }

  public String getString( String key )
  {
    if ( !_seek(key) ) return "";

    int arg_type = _readByte();
    if (arg_type == DATA_TYPE_BYTE)
    {
      return _readString().toString();
    }

    int arg_size = _readInt32();
    if (arg_size == 0) return "";

    switch (arg_type)
    {
      case DATA_TYPE_INT32:  return "" + _readInt32();
      case DATA_TYPE_INT64:  return "" + _readInt64();
      case DATA_TYPE_REAL64: return "" + _readReal64();
    }

    return "";
  }

  public void post()
  {
    Plasmacore.post( this );
  }

  public void print()
  {
    for (int i=0; i<data.count; ++i)
    {
      if (i > 0) System.out.print( " " );
      int b = ((int) data.bytes[i]) & 255;
      System.out.print( "0123456789ABCDEF".charAt(b>>4) );
      System.out.print( "0123456789ABCDEF".charAt(b&15) );
    }
    System.out.println();
    for (int i=0; i<data.count; ++i)
    {
      if (i > 0) System.out.print( " " );
      System.out.print( " " );
      int b = ((int) data.bytes[i]) & 255;
      if (b >= ' ' && b <= 126) System.out.print( (char) b );
      else                      System.out.print( '.' );
    }
    System.out.println();
  }

  public void recycle()
  {
    if (isRecycled) return;

    synchronized (mutex)
    {
      if (_reply != null)
      {
        _reply.recycle();
        _reply = null;
      }
      isRecycled = true;
      data.limitCapacity( 1024 );
      messagePool.add( this );
    }
  }

  public PlasmacoreMessage reply()
  {
    if (this._reply == null) this._reply = PlasmacoreMessage.create( "", messageID );
    return this._reply;
  }

  public PlasmacoreMessage reserve( int additional )
  {
    data.reserve( additional );
    return this;
  }

  public void send()
  {
    Plasmacore.send( this );
  }

  public PlasmacoreMessage set( String key, boolean value )
  {
    return set( key, (byte)(value?1:0) );
  }

  public PlasmacoreMessage set( String key, byte value )
  {
    _writeString( key );
    _writeByte( DATA_TYPE_BYTE )._writeInt32( 1 );
    _writeByte( value );
    return this;
  }

  public PlasmacoreMessage set( String key, byte[] bytes )
  {
    return set( key, bytes, 0, bytes.length );
  }

  public PlasmacoreMessage set( String key, byte[] bytes, int offset, int n )
  {
    _writeString( key );
    _writeByte( DATA_TYPE_BYTE )._writeInt32( n );
    for (int i=0; i<n; ++i)
    {
      _writeByte( bytes[i+offset] );
    }
    return this;
  }

  public PlasmacoreMessage set( String key, double value )
  {
    _writeString( key );
    _writeByte( DATA_TYPE_REAL64 )._writeInt32( 8 );
    _writeReal64( value );
    return this;
  }

  public PlasmacoreMessage set( String key, int value )
  {
    _writeString( key );
    _writeByte( DATA_TYPE_INT32 )._writeInt32( 4 );
    _writeInt32( value );
    return this;
  }

  public PlasmacoreMessage set( String key, long value )
  {
    _writeString( key );
    _writeByte( DATA_TYPE_INT64 )._writeInt32( 8 );
    _writeInt64( value );
    return this;
  }

  public PlasmacoreMessage set( String key, String value )
  {
    _writeString( key );
    _writeByte( DATA_TYPE_BYTE );
    _writeString( value );
    return this;
  }

  public int _readByte()
  {
    if (position >= data.count) return 0;
    return ((int) data.bytes[ position++ ]) & 255;
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

  public ComparableStringBuilder _readString()
  {
    builder.clear();
    int n = _readInt32();
    builder.reserve( n );
    while (--n >= 0)
    {
      builder.writeUTF8Byte( _readByte() );
    }
    return builder;
  }

  public boolean _seek( String key )
  {
    position = argStartPosition;
    while (position < data.count)
    {
      if (_readString().equals(key)) return true;  // leaves read position set correctly
      _readByte(); // skip type
      int skipSize = _readInt32(); // skip data to advance to next property
      position += skipSize;
    }
    return false; // never found it
  }

  public PlasmacoreMessage _writeByte( int value )
  {
    data.add( value );
    return this;
  }

  public PlasmacoreMessage _writeInt32( int value )
  {
    data.writeInt32( value );
    return this;
  }

  public PlasmacoreMessage _writeInt64( long value )
  {
    reserve( 8 );
    _writeInt32( (int) (value >> 32) );
    _writeInt32( (int) value );
    return this;
  }

  public PlasmacoreMessage _writeReal64( double value )
  {
    _writeInt64( Double.doubleToLongBits(value) );
    return this;
  }

  public PlasmacoreMessage _writeString( String value )
  {
    utf8Writer.clear().write( value );
    int n = utf8Writer.count;
    byte[] bytes = utf8Writer.utf8;
    _writeInt32( n );
    for (int i=0; i<n; ++i)
    {
      _writeByte( bytes[i] );
    }
    return this;
  }

  //static public void main( String[] args )
  //{
  //  PlasmacoreMessage m1 = PlasmacoreMessage.create( "Alpha" );
  //  PlasmacoreMessage m2 = PlasmacoreMessage.create( "Beta" );
  //  m1.print();
  //  m2.print();
  //  System.out.println( m1.messageID );
  //  System.out.println( m2.messageID );
  //}
}

