package org.plasmacore;

class ComparableStringBuilder //implements Comparable<String>
{
  /*
  public char[] characters = new char[ 128 ];
  public int    count = 0;

  public ComparableStringBuilder clear()
  {
    count = 0;
    return this;
  }

  public void print( int ch )
  {
    if (ch <= 0xD7FF || (ch >= 0xE000 && ch <= 0xFFFF))
    {
      // Standard UTF-16 encoding
      reserve( 1 );
      characters[ count++ ] = (char) ch;
    }
    else if (ch <= 0xDFFF || ch > 0x10FFFF)
    {
      // Invalid code point - write as character 0
      reserve( 1 );
      characters[ count++ ] = (char) 0;
    }
    else
    {
      // Write 10000..0x10FFFF as surrogate pair
      reserve( 2 );
      ch -= 0x10000;
      characters[ count++ ] = (char)(0xD800 + (ch >> 10));   // high surrogate comes first
      characters[ count++ ] = (char)(0xDC00 + (ch & 0x3FF)); // low surrogate
    }
  }

  public ComparableStringBuilder reserve( int additional )
  {
    int requiredCapacity = count + additional;
    if (requiredCapacity >= characters.length)
    {
      int newCapacity = characters.length * 2;
      if (requiredCapacity > newCapacity) newCapacity = requiredCapacity;
      char[] newCharacters = new char[ newCapacity ];
      for (int i=count; --i>=0; )
      {
        newCharacters[i] = characters[i];
      }
      characters = newCharacters;
    }
    return this;
  }
  */
}
