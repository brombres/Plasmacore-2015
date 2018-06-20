package org.plasmacore;

abstract class PlasmacoreSound
{
  public PlasmacoreSoundManager manager;
  public String  filepath;
  public boolean repeating;
  public boolean isLoading;
  public boolean isReady;
  public boolean error;

  public boolean hasSoundID( int soundID )
  {
    return false;
  }

  public void onLoadFinished( boolean success )
  {
    isLoading = false;
    if (success)
    {
      isReady = true;
      Plasmacore.log( "Sound prepared: " + filepath );
    }
    else
    {
      error = true;
      Plasmacore.logError( "Failed to load sound effect: " + filepath );
    }
  }

  abstract public void play( boolean repeating );

  //---------------------------------------------------------------------------
  // Effect
  //---------------------------------------------------------------------------
  static public class Effect extends PlasmacoreSound
  {
    public int soundID;
    public int streamID;

    public Effect( PlasmacoreSoundManager manager, String filepath )
    {
      this.manager = manager;
      this.filepath = filepath;
      soundID = manager.soundPool.load( filepath, 1 );
    }

    public boolean hasSoundID( int soundID )
    {
      return (soundID == this.soundID);
    }

    public void play( boolean repeating )
    {
      this.repeating = repeating;
      if ( !isReady ) return;

      if (repeating) streamID = manager.soundPool.play( soundID, 1.0f, 1.0f, 0, -1, 1.0f );
      else           streamID = manager.soundPool.play( soundID, 1.0f, 1.0f, 0, 0, 1.0f );
    }
  }
}
