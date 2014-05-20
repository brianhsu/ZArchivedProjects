package org.maidroid.reminder

import android.content.Context

import android.media.MediaPlayer
import android.media.AudioManager

import android.net.Uri
import android.util.Log

object Message
{
    val musicPlayer = new MediaPlayer
    val systemPlayer = new MediaPlayer

    systemPlayer.setAudioStreamType (AudioManager.STREAM_SYSTEM)

    def playerStop () 
    {
        musicPlayer.stop ()
        musicPlayer.reset ()

        systemPlayer.stop ()
        systemPlayer.reset ()
    }

    def setupPlayer (context: Context) = 
    {
        val preference = new MaidroidPreference (context)

        val volume = if (preference.voiceOn == false) 0.0F else preference.volume / 100.F
        val player = if (preference.ignoreSilent) musicPlayer else systemPlayer

        player.setVolume (volume, volume)

        player
    }

    def apply (faceResID: Int, message: String, voice: String) = 
        new Message (faceResID, message, Some(voice))

    def apply (faceResID: Int, message: String) = 
        new Message (faceResID, message, None)
}

class Message private (val faceResID: Int, val message: String, val voice: Option[String])
{
    def play (context: Context)
    {
        Message.playerStop ()

        if (voice == None) return

        try {
            val uri = Uri.parse (voice.get)
            val player = Message.setupPlayer (context)

            player.setDataSource (context, uri)
            player.prepare ()
            player.start ()
        } catch {
            case _ =>
        }
    }
}

