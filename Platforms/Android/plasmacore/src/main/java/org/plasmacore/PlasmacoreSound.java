package org.plasmacore;

import android.media.AudioManager;
import android.media.MediaPlayer;

abstract class PlasmacoreSound
{
  public PlasmacoreSoundManager manager;
  public String  filepath;
  public boolean isRepeating;
  public boolean isLoading;
  public boolean isReady;
  public boolean error;
  public boolean isPaused;
  public boolean isSystemPaused;

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

  abstract public double  duration();
  abstract public boolean isPlaying();
  abstract public void    pause();
  abstract public void    play( boolean isRepeating );
  abstract public double  position();
  abstract public void    setPosition( double newPosition );
  abstract public void    setVolume( double newVolume );
  public void             systemPause() {}
  public void             systemResume() {}
  abstract public void    unload();

  //---------------------------------------------------------------------------
  // Effect
  //---------------------------------------------------------------------------
  static public class Effect extends PlasmacoreSound
  {
    public int    soundID;
    public int    streamID;
    public double volume = 1.0;

    public Effect( PlasmacoreSoundManager manager, String filepath )
    {
      this.manager = manager;
      this.filepath = filepath;
      isLoading = true;
      soundID = manager.soundPool.load( filepath, 1 );
    }

    public double duration()
    {
      return 1.0;
    }

    public boolean hasSoundID( int soundID )
    {
      return (soundID == this.soundID);
    }

    public boolean isPlaying()
    {
      return isRepeating;
    }

    public void pause()
    {
      manager.soundPool.pause( streamID );
      if (isRepeating)
      {
        // If it's not repeating then there's no way to actually tell that it's
        // paused - better in that case to not flag it as paused and just play
        // again from scratch
        isPaused = true;
      }
    }

    public void play( boolean isRepeating )
    {
      this.isRepeating = isRepeating;
      if ( !isReady ) return;

      if (isPaused)
      {
        isPaused = false;
        manager.soundPool.resume( streamID );
      }
      else
      {
        if (isRepeating) streamID = manager.soundPool.play( soundID, (float)volume, (float)volume, 0, -1, 1.0f );
        else             streamID = manager.soundPool.play( soundID, (float)volume, (float)volume, 0,  0, 1.0f );
      }
    }

    public double position()
    {
      return 0.0;
    }

    public void setPosition( double newPosition )
    {
      // no action
    }

    public void setVolume( double newVolume )
    {
      volume = newVolume;
      manager.soundPool.setVolume( streamID, (float)newVolume, (float)newVolume );
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

    public double duration()
    {
      if ( !isReady ) return 0.0;
      return mediaPlayer.getDuration() / 1000.0;
    }

    public boolean hasSoundID( int soundID )
    {
      return false;
    }

    public boolean isPlaying()
    {
      if ( !isReady ) return false;
      return mediaPlayer.isPlaying();
    }

    public void pause()
    {
      if (isPlaying())
      {
        isPaused = true;
        mediaPlayer.pause();
      }
    }

    public void play( boolean isRepeating )
    {
      this.isRepeating = isRepeating;
      if ( !isReady ) return;

      isPaused = false;
      mediaPlayer.setLooping( isRepeating );

      if (manager.allSoundsPaused)
      {
        isSystemPaused = true;
      }
      else
      {
        mediaPlayer.start();
      }
    }

    public void systemPause()
    {
      if (isPlaying())
      {
        isSystemPaused = true;
        pause();
      }
    }

    public void systemResume()
    {
      if ( !isSystemPaused ) return;
      isSystemPaused = false;

      play( isRepeating );
    }

    public double position()
    {
      if (isReady) return mediaPlayer.getCurrentPosition() / 1000.0;
      return 0.0;
    }

    public void setPosition( double newPosition )
    {
      if (isReady) mediaPlayer.seekTo( (long)(newPosition * 1000), MediaPlayer.SEEK_CLOSEST );
    }

    public void setVolume( double newVolume )
    {
      mediaPlayer.setVolume( (float) newVolume, (float) newVolume );
    }

    public void unload()
    {
      if (mediaPlayer != null) mediaPlayer.release();
    }
  }
}

