package org.plasmacore;

import android.media.AudioManager;
import android.media.MediaPlayer;

abstract class PlasmacoreSound
{
  public PlasmacoreSoundManager manager;
  public String  filepath;
  public boolean is_repeating;
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

  abstract public void play( boolean is_repeating );
  abstract public void unload();

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
      isLoading = true;
      soundID = manager.soundPool.load( filepath, 1 );
    }

    public boolean hasSoundID( int soundID )
    {
      return (soundID == this.soundID);
    }

    public void play( boolean is_repeating )
    {
      this.is_repeating = is_repeating;
      if ( !isReady ) return;

      if (is_repeating) streamID = manager.soundPool.play( soundID, 1.0f, 1.0f, 0, -1, 1.0f );
      else              streamID = manager.soundPool.play( soundID, 1.0f, 1.0f, 0, 0, 1.0f );
    }

    public void unload()
    {
      if (isReady)
      {
        manager.soundPool.unload( soundID );
      }
    }
  }

  //---------------------------------------------------------------------------
  // Music
  //---------------------------------------------------------------------------
  static public class Music extends PlasmacoreSound //implements MediaPlayer.OnErrorListener
  {
    public MediaPlayer mediaPlayer;

    public Music( PlasmacoreSoundManager manager, String filepath )
    {
      this.manager = manager;
      this.filepath = filepath;
      try
      {
        mediaPlayer = new MediaPlayer();
        //mediaPlayer.setOnErrorListener( this );

        mediaPlayer.setDataSource( filepath );
        mediaPlayer.prepare();
        mediaPlayer.setAudioStreamType( AudioManager.STREAM_MUSIC );
        isReady = true;
      }
      catch (Exception e)
      {
        Plasmacore.logError( "Error initializing MediaPlayer: " + e.toString() );
        mediaPlayer = null;
        error = true;
      }
    }

    public boolean hasSoundID( int soundID )
    {
      return false;
    }

    //public void onError( MediaPlayer player,int,int)

    public void play( boolean is_repeating )
    {
      this.is_repeating = is_repeating;
      if ( !isReady ) return;
      mediaPlayer.setLooping( is_repeating );
      mediaPlayer.start();
    }

    public void unload()
    {
      if (isReady && mediaPlayer != null) mediaPlayer.release();
    }
  }
}
