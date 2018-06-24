package org.plasmacore;

import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;

class PlasmacoreSoundManager implements SoundPool.OnLoadCompleteListener
{
  public LookupList<PlasmacoreSound> sounds = new LookupList<PlasmacoreSound>();
  public SoundPool soundPool;
  public boolean   allSoundsPaused;

  public PlasmacoreSoundManager()
  {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
    {
      AudioAttributes attributes = new AudioAttributes.Builder()
        .setUsage( AudioAttributes.USAGE_GAME )
        .setContentType( AudioAttributes.CONTENT_TYPE_SONIFICATION )
        .build();
      soundPool = new SoundPool.Builder().setAudioAttributes(attributes).setMaxStreams(8).build();
    }
    else
    {
      soundPool = new SoundPool( 8, AudioManager.STREAM_MUSIC, 0 );
    }

    soundPool.setOnLoadCompleteListener( this );

    final PlasmacoreSoundManager THIS = this;

    Plasmacore.setMessageListener(
        "SoundManager.is_loading",
        new PlasmacoreMessageListener()
        {
          public void on( PlasmacoreMessage m )
          {
            for (int i=0; i<sounds.count(); ++i)
            {
              if (sounds.get(i).isLoading)
              {
                m.reply().set( "is_loading", true );
                return;
              }
            }
            m.reply().set( "is_loading", false );
          }
        }
    );

    Plasmacore.setMessageListener(
        "Sound.create",
        new PlasmacoreMessageListener()
        {
          public void on( PlasmacoreMessage m )
          {
            String filepath = m.getString( "filepath" );
            boolean isMusic = m.getBoolean( "is_music" );
            if (isMusic)
            {
              m.reply().set( "id", sounds.id(new PlasmacoreSound.Music(THIS,filepath)) );
            }
            else
            {
              m.reply().set( "id", sounds.id(new PlasmacoreSound.Effect(THIS,filepath)) );
            }
          }
        }
    );

    Plasmacore.setMessageListener(
        "Sound.duration",
        new PlasmacoreMessageListener()
        {
          public void on( PlasmacoreMessage m )
          {
            int id = m.getInt( "id" );
            PlasmacoreSound sound = sounds.getByID( id );
            if (sound != null) m.reply().set( "duration", sound.duration() );
          }
        }
    );

    Plasmacore.setMessageListener(
        "Sound.is_playing",
        new PlasmacoreMessageListener()
        {
          public void on( PlasmacoreMessage m )
          {
            int id = m.getInt( "id" );
            PlasmacoreSound sound = sounds.getByID( id );
            if (sound != null) m.reply().set( "is_playing", sound.isPlaying() );
          }
        }
    );

    Plasmacore.setMessageListener(
        "Sound.pause",
        new PlasmacoreMessageListener()
        {
          public void on( PlasmacoreMessage m )
          {
            int id = m.getInt( "id" );
            PlasmacoreSound sound = sounds.getByID( id );
            if (sound != null) sound.pause();
          }
        }
    );

    Plasmacore.setMessageListener(
        "Sound.play",
        new PlasmacoreMessageListener()
        {
          public void on( PlasmacoreMessage m )
          {
            int id = m.getInt( "id" );
            boolean is_repeating = m.getBoolean( "is_repeating" );
            PlasmacoreSound sound = sounds.getByID( id );
            if (sound != null) sound.play( is_repeating );
          }
        }
    );

    Plasmacore.setMessageListener(
        "Sound.position",
        new PlasmacoreMessageListener()
        {
          public void on( PlasmacoreMessage m )
          {
            int id = m.getInt( "id" );
            PlasmacoreSound sound = sounds.getByID( id );
            if (sound != null) m.reply().set( "position", sound.position() );
          }
        }
    );

    Plasmacore.setMessageListener(
        "Sound.set_position",
        new PlasmacoreMessageListener()
        {
          public void on( PlasmacoreMessage m )
          {
            int id = m.getInt( "id" );
            double position = m.getDouble( "position" );
            PlasmacoreSound sound = sounds.getByID( id );
            if (sound != null) sound.setPosition( position );
          }
        }
    );

    Plasmacore.setMessageListener(
        "Sound.set_volume",
        new PlasmacoreMessageListener()
        {
          public void on( PlasmacoreMessage m )
          {
            int id = m.getInt( "id" );
            double volume = m.getDouble( "volume" );
            PlasmacoreSound sound = sounds.getByID( id );
            if (sound != null) sound.setVolume( volume );
          }
        }
    );

    Plasmacore.setMessageListener(
        "Sound.unload",
        new PlasmacoreMessageListener()
        {
          public void on( PlasmacoreMessage m )
          {
            int id = m.getInt( "id" );
            PlasmacoreSound sound = sounds.removeID( id );
            if (sound != null)
            {
              sound.unload();
            }
          }
        }
    );

  }

  public void onLoadComplete( SoundPool soundPool, int soundID, int status )
  {
    for (int i=0; i<sounds.count(); ++i)
    {
      PlasmacoreSound sound = sounds.get( i );
      if (sound.hasSoundID(soundID))
      {
        sound.onLoadFinished( 0 == status );
        break;
      }
    }
  }

  public void pauseAll()
  {
    if (allSoundsPaused) return;
    allSoundsPaused = true;

    soundPool.autoPause();
    for (int i=0; i<sounds.count(); ++i)
    {
      sounds.get( i ).systemPause();
    }
  }

  public void resumeAll()
  {
    if ( !allSoundsPaused ) return;
    allSoundsPaused = false;

    soundPool.autoResume();
    for (int i=0; i<sounds.count(); ++i)
    {
      sounds.get( i ).systemResume();
    }
  }
}


