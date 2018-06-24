package org.plasmacore;

import java.util.*;

class LookupList<ValueType>
{
  // - Similar to an ArrayList with a fast lookup table.
  // - Also similar to an indexed, ordered set.
  // - add() and get() items with the speed of a list.
  // - contains() and locate() with the speed of a table.
  // - id(value)->Int32 gives an integer ID that is fixed as long as the value is in the lookup.
  // - getByID(id)->ValueType returns the value associated with an integer ID.
  // - remove(value), removeAt(index), removeID(id), removeFirst(), removeLast().

  // PROPERTIES
  public ArrayList<ValueType>       values;
  public IntList                    indices;  // indices[id] -> index of value with given id
  public IntList                    unused = new IntList();
  public HashMap<ValueType,Integer> ids = new HashMap<ValueType,Integer>();

  // METHODS
  public LookupList()
  {
    this( 10 );
  }

  public LookupList( int initialCapacity )
  {
    values = new ArrayList<ValueType>( initialCapacity );
    indices = new IntList( initialCapacity );
    clear();
  }

  public LookupList<ValueType> clear()
  {
    values.clear();
    indices.clear().add( 0 ); // Dummy value to avoid using ID 0
    ids.clear();
    unused.clear();
    return this;
  }

  public LookupList<ValueType> add( ValueType value )
  {
    if (ids.containsKey(value)) return this;

    if (unused.count > 0)
    {
      int id = unused.removeFirst();
      ids.put( value, id );
      indices.set( id, values.size() );
    }
    else
    {
      int id = indices.count;
      ids.put( value, id );
      indices.add( values.size() );
    }

    values.add( value );
    return this;
  }

  public boolean contains( ValueType value )
  {
    return ids.containsKey( value );
  }

  public int count()
  {
    return values.size();
  }

  public ValueType get( int index )
  {
    return values.get( index );
  }

  public ValueType getByID( int id )
  {
    return values.get( indices.get(id) );
  }

  public int id( ValueType value )
  {
    Integer idValue = ids.get( value );
    if (idValue != null) return (int) idValue;

    int id = (unused.count>0) ? unused.last() : indices.count;
    add( value );
    return id;
  }

  public int index( ValueType value )
  {
    Integer id = ids.get( value );
    if (id != null) return indices.get( (int) id );

    int result = values.size();
    add( value );
    return result;
  }

  public int locate( ValueType value )
  {
    Integer index = ids.get( value );
    if (index == null) return -1;
    return indices.get( index );
  }

  public ValueType remove( ValueType value )
  {
    Integer existingID = ids.get( value );
    if (existingID == null) return value;

    int id = (int) existingID;
    int removeIndex = indices.get( id );

    ids.remove( value );
    values.remove( removeIndex );
    unused.add( id );
    indices.set( id, 0 );

    for (int i=0; i<indices.count; ++i)
    {
      int remainingIndex = indices.get( i );
      if (remainingIndex > removeIndex)
      {
        indices.set( i, remainingIndex - 1 );
      }
    }

    return value;
  }

  public ValueType removeAt( int index )
  {
    return remove( values.get(index) );
  }

  public ValueType removeFirst()
  {
    return remove( values.get(0) );
  }

  public ValueType removeID( int id )
  {
    return remove( values.get(indices.get(id)) );
  }

  public ValueType removeLast()
  {
    return remove( values.get(values.size()-1) );
  }

  public LookupList<ValueType> set( int index, ValueType newValue )
  {
    ValueType oldValue = values.get( index );
    if (oldValue == newValue) return this;

    int id = ids.remove( oldValue );
    ids.put( newValue, id );
    values.set( index, newValue );
    return this;
  }

  public String toString()
  {
    StringBuilder builder = new StringBuilder();
    builder.append( '[' );
    int count = values.size();
    for (int i=0; i<count; ++i)
    {
      if (i > 0) builder.append( ',' );
      builder.append( "" + values.get(i) );
    }
    builder.append( ']' );
    return builder.toString();
  }
}
