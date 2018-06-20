package org.plasmacore;

public class IntList
{
  public int[] data;
  public int   count;

  public IntList()
  {
    this( 10 );
  }

  public IntList( int initialCapacity )
  {
    data = new int[ initialCapacity ];
  }

  public IntList add( int value )
  {
    reserve( 1 ).data[ count++ ] = (int) value;
    return this;
  }

  public IntList add( IntList other )
  {
    return add( other.data, 0, other.count );
  }

  public IntList add( int[] data, int offset, int n )
  {
    reserve( n );
    int[] this_data = this.data;
    count += n;
    int count = this.count;
    for (int i=offset+n; --i>=offset; )
    {
      this_data[ --count ] = data[ i ];
    }
    return this;
  }

  public int capacity()
  {
    return data.length;
  }

  public IntList clear()
  {
    count = 0;
    return this;
  }

  public int first()
  {
    return data[ 0 ];
  }

  public int get( int index )
  {
    return data[ index ];
  }

  public int last()
  {
    return data[ count-1 ];
  }

  public IntList limitCapacity( int maxCapacity )
  {
    if (data.length > maxCapacity)
    {
      if (maxCapacity <= 0)
      {
        data = new int[0];
        count = 0;
      }
      else
      {
        count = Math.min( count, maxCapacity );
        int[] newData = new int[ maxCapacity ];
        for (int i=count; --i>=0; )
        {
          newData[i] = data[i];
        }
        data = newData;
      }
    }
    return this;
  }

  public int readInt32( int startIndex )
  {
    if (startIndex + 4 > count) return 0;
    int result = ((int)data[ startIndex ]) << 24;
    result |= ((int)data[ startIndex+1 ]) << 16;
    result |= ((int)data[ startIndex+2 ]) << 8;
    result |= ((int)data[ startIndex+3 ]);
    return result;
  }

  public int removeAt( int index )
  {
    int result = data[ index ];
    --count;
    int n = count;
    for (int i=index; i<n; ++i)
    {
      data[ i ] = data[ i+1 ];
    }
    data[ count ] = 0;
    return result;
  }

  public int removeFirst()
  {
    return removeAt( 0 );
  }

  public int removeLast()
  {
    return removeAt( count-1 );
  }

  public IntList reserve( int additional )
  {
    int requiredCapacity = count + additional;
    if (requiredCapacity <= data.length) return this;

    int newCapacity = data.length * 2;
    if (requiredCapacity > newCapacity) newCapacity = requiredCapacity;

    int[] newData = new int[ requiredCapacity ];
    for (int i=count; --i>=0; )
    {
      newData[i] = data[i];
    }
    data = newData;

    return this;
  }

  public IntList set( int index, int value )
  {
    data[ index ] = value;
    return this;
  }

  public String toString()
  {
    StringBuilder buffer = new StringBuilder();
    buffer.append( '[' );
    for (int i=0; i<count; ++i)
    {
      if (i > 0) buffer.append( ',' );
      buffer.append( ((int)data[i])&255 );
    }
    buffer.append( ']' );
    return buffer.toString();
  }

  public IntList writeInt32( int value )
  {
    reserve( 4 );
    data[ count   ] = (int) (value >> 24);
    data[ count+1 ] = (int) (value >> 16);
    data[ count+2 ] = (int) (value >> 8);
    data[ count+3 ] = (int) value;
    count += 4;
    return this;
  }
}
