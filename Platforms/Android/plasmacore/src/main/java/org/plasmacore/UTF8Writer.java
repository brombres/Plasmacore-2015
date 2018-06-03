package org.plasmacore;

class UTF8Writer
{
  public byte[]  utf8;
  public int     count;

  public int     unicode;
  public int     continuationCount;

  public boolean isValid;

  public UTF8Writer()
  {
    this( 128 );
  }

  public UTF8Writer( int minimumCapacity )
  {
    utf8 = new byte[ minimumCapacity ];
  }

  public UTF8Writer clear()
  {
    count = 0;
    isValid = true;
    return this;
  }

  public UTF8Writer reserve( int additional )
  {
    int requiredCapacity = count + additional;
    if (requiredCapacity >= utf8.length)
    {
      int newCapacity = utf8.length * 2;
      if (requiredCapacity > newCapacity) newCapacity = requiredCapacity;
      byte[] newUTF8 = new byte[ newCapacity ];
      for (int i=count; --i>=0; )
      {
        newUTF8[i] = utf8[i];
      }
      utf8 = newUTF8;
    }
    return this;
  }

  public void write( String value )
  {
    int n = value.length();
    for (int i=0; i<n; ++i)
    {
      writeUTF16Char( value.charAt(i) );
    }
  }

  public void writeUTF16Char( int value )
  {
    if (continuationCount == 0)
    {
      if ((value & 0xfC00) == 0xD800)  // first of surrogate pair D800..DBFF
      {
        unicode = (value - 0xD800) << 10;
        continuationCount = 1;
      }
      else if ((value & 0xfC00) == 0xDC00)  // illegal second surrogate
      {
        isValid = false;
      }
      else
      {
        // regular Unicode <= 0xFFFF
        writeUnicode( value );
      }
    }
    else
    {
      if ((value & 0xFC00) != 0xDC00)
      {
        isValid = false;
      }
      else
      {
        // Second half of surrogate pair
        unicode |= (value - 0xDC00);
        writeUnicode( unicode + 0x10000 );
      }
      continuationCount = 0;
    }
  }

  public void writeUnicode( int value )
  {
    if (value <= 0x7F)
    {
      reserve( 1 );
      utf8[ count++ ] = (byte) value;
    }
    else if (value <= 0x7FF)
    {
      reserve( 2 );
      utf8[ count++ ] = (byte) (0xC0 | ((value >> 6) & 0x1F));
      utf8[ count++ ] = (byte) (0x80 | (value & 0x3F));
    }
    else if (value <= 0xFFFF)
    {
      reserve( 3 );
      utf8[ count++ ] = (byte) (0xE0 | ((value >> 12) & 0xF));
      utf8[ count++ ] = (byte) (0x80 | ((value >> 6) & 0x3F));
      utf8[ count++ ] = (byte) (0x80 | (value & 0x3F));
    }
    else
    {
      // 0x1_0000..0x10_FFFF
      reserve( 4 );
      utf8[ count++ ] = (byte) (0xF0 | ((value >> 18) & 0x7));
      utf8[ count++ ] = (byte) (0x80 | ((value >> 12) & 0x3F));
      utf8[ count++ ] = (byte) (0x80 | ((value >> 6)  & 0x3F));
      utf8[ count++ ] = (byte) (0x80 | (value & 0x3F));
    }
  }

}
