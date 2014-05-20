package org.maidroid.reminder2

import android.graphics.Bitmap
import android.content.Context

import android.media.MediaPlayer
import android.media.AudioManager

import android.net.Uri

object Message {
    val systemPlayer = new MediaPlayer

    systemPlayer.setAudioStreamType (AudioManager.STREAM_SYSTEM)

    def playerStop () 
    {
        systemPlayer.stop ()
        systemPlayer.reset ()
    }
}

case class Message(text: String, maidBitmap: Bitmap, voice: Option[String] = None)
{
    private def play (context: Context, uriString: String) {
        val settings = new Settings(context)

        if (settings.enableVoice) {
            val uri = Uri.parse (uriString)
            val player = Message.systemPlayer

            player.setDataSource (context, uri)
            player.prepare ()
            player.start ()
        }
    }

    def play (context: Context)
    {
        Message.playerStop ()
        voice.foreach(uri => play(context, uri))
    }
    
}

