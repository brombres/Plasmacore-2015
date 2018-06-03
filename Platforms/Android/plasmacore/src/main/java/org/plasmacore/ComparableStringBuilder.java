package org.plasmacore;

class ComparableStringBuilder implements CharSequence, Comparable<String>
{
  public char[]  characters;
  public int     count;
  public int     hash;

  public int     unicode;
  public int     continuationCount;

  public boolean isValid;

  public ComparableStringBuilder()
  {
    this( 128 );
  }

  public ComparableStringBuilder( int minimumCapacity )
  {
    characters = new char[ minimumCapacity ];
  }

  public ComparableStringBuilder clear()
  {
    count = 0;
    hash = 0;
    isValid = true;
    return this;
  }

  public char charAt( int index )
  {
    return characters[ index ];
  }

  public int compareTo( String other )
  {
    return compareTo( (CharSequence) other );
  }

  public int compareTo( CharSequence other )
  {
    int thisLength  = count;
    int otherLength = other.length();
    int minLength = Math.min( thisLength, otherLength );

    for (int i=0; i<minLength; ++i)
    {
      char thisCh = characters[ i ];
      char otherCh = other.charAt( i );
      if (thisCh != otherCh)
      {
        if (thisCh < otherCh) return -1;
        else                  return  1;
      }
    }

    if (thisLength == otherLength) return  0;  // equal
    if (thisLength < otherLength)  return -1;
    return 1;
  }

  public boolean equals( Object other )
  {
    if (other instanceof CharSequence) return equals( (CharSequence) other );
    return this == other;
  }

  public boolean equals( CharSequence other )
  {
    if (count != other.length()) return false;
    if (hash != other.hashCode()) return false;
    for (int i=count; --i>=0; )
    {
      if (characters[i] != other.charAt(i)) return false;
    }
    return true;
  }

  public int hashCode()
  {
    return hash;
  }

  public int length()
  {
    return count;
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

  public ComparableStringBuilder subSequence( int i1, int limit )
  {
    ComparableStringBuilder result = new ComparableStringBuilder( limit - i1 );
    int code = 0;
    for (int i=limit; --i>=i1; )
    {
      char ch = characters[i];
      result.characters[i-i1] = ch;
      code = ((code << 5) - code) + ch;
    }
    result.hash = code;
    return result;
  }

  public String toString()
  {
    return new String( characters, 0, count );
  }

  public void writeUTF8Byte( int value )
  {
    value &= 255;
    if (continuationCount == 0)
    {
      if ((value & 0xC0) == 0x80)  // 10xxxxxx - UTF-8 continuation byte
      {
        isValid = false;
      }
      else
      {
        if (value < 0x80)  // 0xxxxxxx
        {
          writeUnicode( value );
        }
        else if ((value & 0xE0) == 0xC0)  // 110xxxxx
        {
          unicode = value & 0x1F;
          continuationCount = 1;
        }
        else if ((value & 0xF0) == 0xE0)  // 1110xxxx
        {
          unicode = value & 0xF;
          continuationCount = 2;
        }
        else if ((value & 0xF8) == 0xF0)  // 11110xxx
        {
          unicode = value & 7;
          continuationCount = 3;
        }
        else
        {
          isValid = false;
        }
      }
    }
    else
    {
      if ((value & 0xC0) != 0x80)
      {
        isValid = false;
      }
      else
      {
        unicode = (unicode << 6) | (value & 0x3F);
      }
      if (--continuationCount == 0) writeUnicode( unicode );
    }
  }

  public void writeUnicode( int code )
  {
    if (code <= 0xD7FF || (code >= 0xE000 && code <= 0xFFFF))
    {
      // Standard UTF-16 encoding
      reserve( 1 );
      characters[ count++ ] = (char) code;
      hash = ((hash << 5) - hash) + code;
    }
    else if (code <= 0xDFFF || code > 0x10FFFF)
    {
      // Invalid code point - write as character 0
      reserve( 1 );
      characters[ count++ ] = (char) 0;
      hash = ((hash << 5) - hash);
    }
    else
    {
      // Write 10000..0x10FFFF as surrogate pair
      reserve( 2 );
      code -= 0x10000;
      characters[ count++ ] = (char)(0xD800 + (code >> 10));   // high surrogate comes first
      characters[ count++ ] = (char)(0xDC00 + (code & 0x3FF)); // low surrogate
      hash = ((hash << 5) - hash) + characters[count-2];
      hash = ((hash << 5) - hash) + characters[count-1];
    }
  }

}
