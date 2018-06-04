package org.plasmacore;

public class ByteList
{
  public byte[] bytes;
  public int    count;

  public ByteList()
  {
    this( 1 );//28 );
  }

  public ByteList( int initialCapacity )
  {
    bytes = new byte[ initialCapacity ];
  }

  public ByteList add( int value )
  {
    reserve( 1 ).bytes[ count++ ] = (byte) value;
    return this;
  }

  public ByteList add( byte[] bytes, int i1, int n )
  {
    reserve( n );
    byte[] this_bytes = this.bytes;
    count += n;
    int count = this.count;
    for (int i=i1+n; --i>=i1; )
    {
      this_bytes[ --count ] = bytes[ i ];
    }
    return this;
  }

  public int capacity()
  {
    return bytes.length;
  }

  public ByteList clear()
  {
    count = 0;
    return this;
  }

  public ByteList limitCapacity( int maxCapacity )
  {
    if (bytes.length > maxCapacity)
    {
      if (maxCapacity <= 0)
      {
        bytes = new byte[0];
        count = 0;
      }
      else
      {
        count = Math.min( count, maxCapacity );
        byte[] newData = new byte[ maxCapacity ];
        for (int i=count; --i>=0; )
        {
          newData[i] = bytes[i];
        }
        bytes = newData;
      }
    }
    return this;
  }

  public ByteList reserve( int additional )
  {
    int requiredCapacity = count + additional;
    if (requiredCapacity <= bytes.length) return this;

    int newCapacity = bytes.length * 2;
    if (requiredCapacity > newCapacity) newCapacity = requiredCapacity;

    byte[] newData = new byte[ requiredCapacity ];
    for (int i=count; --i>=0; )
    {
      newData[i] = bytes[i];
    }
    bytes = newData;

    return this;
  }

  public String toString()
  {
    StringBuilder buffer = new StringBuilder();
    buffer.append( '[' );
    for (int i=0; i<count; ++i)
    {
      if (i > 0) buffer.append( ',' );
      buffer.append( ((int)bytes[i])&255 );
    }
    buffer.append( ']' );
    return buffer.toString();
  }
}
