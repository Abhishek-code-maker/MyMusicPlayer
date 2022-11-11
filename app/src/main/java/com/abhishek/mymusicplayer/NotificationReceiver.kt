package com.abhishek.mymusicplayer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            AppClass.PREVIOUS -> preNextSong(increment = false, context = context!!)
            AppClass.PLAY -> if (PlayerActivity.isPlaying) {
                pauseMusic()
            } else {
                playMusic()
            }
            AppClass.NEXT -> preNextSong(increment = true, context = context!!)
            AppClass.EXIT -> {
//                PlayerActivity.musicService!!.stopForeground(true)
//                PlayerActivity.musicService!!.mediaPlayer!!.release()
//                PlayerActivity.musicService = null
//                exitProcess(1)
                exitApplication()
            }
        }
    }

    private fun playMusic() {
        PlayerActivity.isPlaying = true
        PlayerActivity.musicService!!.mediaPlayer!!.start()
        PlayerActivity.musicService!!.showNotification(R.drawable.ic_pause)
        PlayerActivity.binding.playPausePA.setIconResource(R.drawable.ic_pause)
        NowPlaying.binding.playPauseBtnNP.setIconResource(R.drawable.ic_pause)
    }

    private fun pauseMusic() {
        PlayerActivity.isPlaying = false
        PlayerActivity.musicService!!.mediaPlayer!!.pause()
        PlayerActivity.musicService!!.showNotification(R.drawable.ic_play)
        PlayerActivity.binding.playPausePA.setIconResource(R.drawable.ic_play)
        NowPlaying.binding.playPauseBtnNP.setIconResource(R.drawable.ic_play)
    }

    private fun preNextSong(increment: Boolean, context: Context) {
        setSongPosition(increment = increment)
        PlayerActivity.musicService!!.createMediaPlayer()
        Glide.with(context)
            .load(PlayerActivity.musicListPA[PlayerActivity.songPosition].artUri)
            .apply(RequestOptions().placeholder(R.drawable.music_icon_splash_screen).centerCrop())
            .into(PlayerActivity.binding.songImagePA)
        PlayerActivity.binding.songNamePA.text =
            PlayerActivity.musicListPA[PlayerActivity.songPosition].title
        Glide.with(context)
            .load(PlayerActivity.musicListPA[PlayerActivity.songPosition].artUri)
            .apply(RequestOptions().placeholder(R.drawable.music_icon_splash_screen).centerCrop())
            .into(NowPlaying.binding.songImageNp)
        NowPlaying.binding.songNameNp.text =
            PlayerActivity.musicListPA[PlayerActivity.songPosition].title
        playMusic()
        PlayerActivity.fIndex =
            favouriteChecker(PlayerActivity.musicListPA[PlayerActivity.songPosition].id)
        if (PlayerActivity.isFavourite) {
            PlayerActivity.binding.favBtnPA.setImageResource(R.drawable.ic_favourites)
        } else {
            PlayerActivity.binding.favBtnPA.setImageResource(R.drawable.ic_favourite_empty)
        }
    }
}