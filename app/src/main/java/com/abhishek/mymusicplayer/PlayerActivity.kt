package com.abhishek.mymusicplayer

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.database.Cursor
import android.graphics.BitmapFactory
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.audiofx.AudioEffect
import android.media.audiofx.LoudnessEnhancer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.MediaStore
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.abhishek.mymusicplayer.databinding.ActivityPlayerBinding
import com.abhishek.mymusicplayer.databinding.AudioBoosterBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class PlayerActivity : AppCompatActivity(), ServiceConnection, MediaPlayer.OnCompletionListener{

    companion object{
       lateinit var musicListPA : ArrayList<Music>
       var songPosition : Int = 0
        var isPlaying : Boolean = false
        var musicService : MusicService? = null
        @SuppressLint("StaticFieldLeak")
        lateinit var binding: ActivityPlayerBinding
        var repeat : Boolean = false
        var min15 : Boolean = false
        var min30 : Boolean = false
        var min60 : Boolean = false
        var nowPlayingId : String = ""
        var isFavourite : Boolean = false
        var fIndex : Int = -1
        lateinit var loudnessEnhancer: LoudnessEnhancer
    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(MainActivity.currentThemeNav[MainActivity.themeIndex])
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if(intent.data?.scheme.contentEquals("content")){
            val intentService = Intent(this, MusicService::class.java)
            bindService(intentService, this, BIND_AUTO_CREATE)
            startService(intentService)
            musicListPA = ArrayList()
            musicListPA.add(getMusicDetails(intent.data!!))
            Glide.with(this)
                .load(getImgArt(musicListPA[songPosition].path))
                .apply(RequestOptions().placeholder(R.drawable.music_icon_splash_screen).centerCrop())
                .into(binding.songImagePA)
            binding.songNamePA.text = musicListPA[songPosition].title
        }
        else{
            initializeLayout()
        }

        //audio booster feature
        binding.boosterBtnPA.setOnClickListener {
            val customDialogB = LayoutInflater.from(this).inflate(R.layout.audio_booster, binding.root, false)
            val bindingB = AudioBoosterBinding.bind(customDialogB)
            val dialogB = MaterialAlertDialogBuilder(this).setView(customDialogB)
                .setOnCancelListener { playMusic() }
                .setPositiveButton("OK"){self, _ ->
                    loudnessEnhancer.setTargetGain(bindingB.verticalBar.progress * 100)
                    playMusic()
                    self.dismiss()
                }
                .setBackground(ColorDrawable(0x803700B3.toInt()))
                .create()
            dialogB.show()

            bindingB.verticalBar.progress = loudnessEnhancer.targetGain.toInt()/100
            bindingB.progressText.text = "Audio Boost\n\n${loudnessEnhancer.targetGain.toInt()/10} %"
            bindingB.verticalBar.setOnProgressChangeListener {
                bindingB.progressText.text = "Audio Boost\n\n${it*20} %"
            }
            setDialogBtnBackground(this, dialogB)
        }

        binding.backBtnPA.setOnClickListener{finish()}
        binding.playPausePA.setOnClickListener(){
            if(isPlaying){
                pauseMusic()
            }
            else{
                playMusic()
            }
        }
        binding.previousBtn.setOnClickListener{ preNextSong(increment = false)}
        binding.nextBtn.setOnClickListener { preNextSong(increment = true) }
        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if(fromUser){
                    musicService!!.mediaPlayer!!.seekTo(progress)
                    musicService!!.showNotification(if(isPlaying){
                        R.drawable.ic_pause
                    }
                    else{
                        R.drawable.ic_play
                    })
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit
            override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
        })
        binding.repeatBtn.setOnClickListener{
            if(!repeat){
                repeat = true
                binding.repeatBtn.setColorFilter(ContextCompat.getColor(this, R.color.purple_500))
            }
            else{
                repeat = false
                binding.repeatBtn.setColorFilter(ContextCompat.getColor(this, R.color.coolBlue))
            }
        }
        binding.equalizer.setOnClickListener {
           try {
               val eqIntent = Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL)
               eqIntent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, musicService!!.mediaPlayer!!.audioSessionId)
               eqIntent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, baseContext.packageName)
               eqIntent.putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC)
               startActivityForResult(eqIntent, 13)
           }catch (e : Exception){
               Toast.makeText(this, "Equalizer Feature Not Available For Your Device", Toast.LENGTH_SHORT).show()
           }
        }
        binding.timerBtn.setOnClickListener {
            val timer = min15 || min30 || min60
            if(!timer){
            showBottomSheetDialog()
            }
            else{
                val builder = MaterialAlertDialogBuilder(this)
                builder.setTitle("Stop Timer")
                    .setMessage("Do You Want To Stop Timer")
                    .setPositiveButton("Yes"){ _, _ ->
                        min15 = false
                        min30 = false
                        min60 = false
                        binding.timerBtn.setColorFilter(ContextCompat.getColor(this, R.color.black))
                    }
                    .setNegativeButton("No"){ dialog, _ ->
                        dialog.dismiss()
                    }
                val customDialog = builder.create()
                customDialog.show()
                setDialogBtnBackground(this, customDialog)
//                customDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED)
//                customDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.RED)
            }
        }
        binding.shareBtn.setOnClickListener {
            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.type = "audio/*"
            shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(musicListPA[songPosition].path))
            startActivity(Intent.createChooser(shareIntent, "Sharing Music File.."))
        }
        binding.favBtnPA.setOnClickListener {
            fIndex = favouriteChecker(musicListPA[songPosition].id)
            if(isFavourite){
                isFavourite = false
                binding.favBtnPA.setImageResource(R.drawable.ic_favourite_empty)
                FavouriteActivity.favouriteSongs.removeAt(fIndex)
            }
            else{
                isFavourite = true
                binding.favBtnPA.setImageResource(R.drawable.ic_favourites)
                FavouriteActivity.favouriteSongs.add(musicListPA[songPosition])
            }
            FavouriteActivity.favouritesChanged = true
        }
    }

    private fun initializeLayout(){
        songPosition = intent.getIntExtra("index", 0)
        when(intent.getStringExtra("class")){
            "NowPlaying" -> {
                setLayout()
                binding.startSeekText.text = formatDuration(musicService!!.mediaPlayer!!.currentPosition.toLong())
                binding.endSeekText.text = formatDuration(musicService!!.mediaPlayer!!.duration.toLong())
                binding.seekBar.progress = musicService!!.mediaPlayer!!.currentPosition
                binding.seekBar.max = musicService!!.mediaPlayer!!.duration
                if(isPlaying){
                    binding.playPausePA.setIconResource(R.drawable.ic_pause)
                }
                else{
                    binding.playPausePA.setIconResource(R.drawable.ic_play)
                }
            }
            "MusicAdapterSearch" -> initServiceAndPlaylist(MainActivity.musicListSearch, shuffle = false)
            "MusicAdapter" -> initServiceAndPlaylist(MainActivity.musicListMA, shuffle = false)
            "FavouriteAdapter" -> initServiceAndPlaylist(FavouriteActivity.favouriteSongs, shuffle = false)
            "MainActivity" -> initServiceAndPlaylist(MainActivity.musicListMA, shuffle = true)
            "FavouriteShuffle" -> initServiceAndPlaylist(FavouriteActivity.favouriteSongs, shuffle = true)
            "PlayListDetailsAdapter" -> {
                initServiceAndPlaylist(PlayListActivity.musicPlayList.ref[PlayListDetails.currentPlayListPos].playList, shuffle = false)
            }
            "PlayListDetailsShuffle" -> {
                initServiceAndPlaylist(PlayListActivity.musicPlayList.ref[PlayListDetails.currentPlayListPos].playList, shuffle = true)
            }
            "PlayNext"->initServiceAndPlaylist(PlayNext.playNextList, shuffle = false, playNext = true)
        }
        if (musicService!= null && !isPlaying) playMusic()
    }

    private fun setLayout(){
        fIndex = favouriteChecker(musicListPA[songPosition].id)
        Glide.with(applicationContext)
            .load(musicListPA[songPosition].artUri)
            .apply(RequestOptions().placeholder(R.drawable.music_icon_splash_screen).centerCrop())
            .into(binding.songImagePA)
        binding.songNamePA.text = musicListPA[songPosition].title
        if(repeat){
            binding.repeatBtn.setColorFilter(ContextCompat.getColor(applicationContext, R.color.purple_500))
        }
        if(min15 || min30 || min60){
            binding.timerBtn.setColorFilter(ContextCompat.getColor(applicationContext, R.color.teal_200))
        }
        if(isFavourite){
            binding.favBtnPA.setImageResource(R.drawable.ic_favourites)
        }
        else{
            binding.favBtnPA.setImageResource(R.drawable.ic_favourite_empty)
        }

        val img = getImgArt(musicListPA[songPosition].path)
        val image = if (img != null) {
            BitmapFactory.decodeByteArray(img, 0, img.size)
        } else {
            BitmapFactory.decodeResource(
                resources,
                R.drawable.music_icon_splash_screen
            )
        }
        val bgColor = getMainColor(image)
        val gradient = GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, intArrayOf(0xFFFFFF, bgColor))
        binding.root.background = gradient
        window?.statusBarColor = bgColor
    }

    private fun createMediaPlayer(){
        try {
            if(musicService!!.mediaPlayer == null) musicService!!.mediaPlayer = MediaPlayer()
            musicService!!.mediaPlayer!!.reset()
            musicService!!.mediaPlayer!!.setDataSource(musicListPA[songPosition].path)
            musicService!!.mediaPlayer!!.prepare()
//            musicService!!.mediaPlayer!!.start()
//            isPlaying = true
//            binding.playPausePA.setIconResource(R.drawable.ic_pause)
//            musicService!!.showNotification(R.drawable.ic_pause, 1F)
            binding.startSeekText.text = formatDuration(musicService!!.mediaPlayer!!.currentPosition.toLong())
            binding.endSeekText.text = formatDuration(musicService!!.mediaPlayer!!.duration.toLong())
            binding.seekBar.progress = 0
            binding.seekBar.max = musicService!!.mediaPlayer!!.duration
            musicService!!.mediaPlayer!!.setOnCompletionListener(this)
            nowPlayingId = musicListPA[songPosition].id
            playMusic()
            loudnessEnhancer = LoudnessEnhancer(musicService!!.mediaPlayer!!.audioSessionId)
            loudnessEnhancer.enabled = true
        }
        catch (e : Exception){
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show()
        }
    }

    private fun playMusic(){
        isPlaying = true
        musicService!!.mediaPlayer!!.start()
        binding.playPausePA.setIconResource(R.drawable.ic_pause)
        musicService!!.showNotification(R.drawable.ic_pause)
    }

    private fun pauseMusic(){
        isPlaying = false
        musicService!!.mediaPlayer!!.pause()
        binding.playPausePA.setIconResource(R.drawable.ic_play)
        musicService!!.showNotification(R.drawable.ic_play)
    }

    private fun preNextSong(increment : Boolean){
        if(increment){
            setSongPosition(increment = true)
            setLayout()
            createMediaPlayer()
        }
        else{
            setSongPosition(increment = false)
            setLayout()
            createMediaPlayer()
        }
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?){
        if(musicService == null){
            val binder = service as MusicService.MyBinder
            musicService = binder.currentService()
            musicService!!.audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
            musicService!!.audioManager.requestAudioFocus(musicService, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
        }
        createMediaPlayer()
        musicService!!.seekBarSetup()
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        musicService = null
    }

    override fun onCompletion(mp: MediaPlayer?) {
        setSongPosition(increment = true)
        createMediaPlayer()
        setLayout()

        //for refreshing now playing image & text on song completion
        NowPlaying.binding.songNameNp.isSelected = true
        Glide.with(applicationContext)
            .load(musicListPA[songPosition].artUri)
            .apply(RequestOptions().placeholder(R.drawable.music_icon_splash_screen).centerCrop())
            .into(NowPlaying.binding.songImageNp)
        NowPlaying.binding.songNameNp.text = musicListPA[songPosition].title
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 13 || resultCode == RESULT_OK){
            return
        }
    }

    private fun showBottomSheetDialog(){
        val dialog = BottomSheetDialog(this@PlayerActivity)
        dialog.setContentView(R.layout.bottom_sheet_dialog)
        dialog.show()
        dialog.findViewById<LinearLayout>(R.id.min_15)?.setOnClickListener {
            Toast.makeText(baseContext, "Music will stop after 15 Minutes", Toast.LENGTH_SHORT).show()
            binding.timerBtn.setColorFilter(ContextCompat.getColor(this, R.color.teal_200))
            min15 = true
            Thread{Thread.sleep((15 * 60000).toLong())
            if(min15){
                exitApplication()
            }}.start()
            dialog.dismiss()
        }
        dialog.findViewById<LinearLayout>(R.id.min_30)?.setOnClickListener {
            Toast.makeText(baseContext, "Music will stop after 30 Minutes", Toast.LENGTH_SHORT).show()
            binding.timerBtn.setColorFilter(ContextCompat.getColor(this, R.color.teal_200))
            min30 = true
            Thread{Thread.sleep((30 * 60000).toLong())
                if(min30){
                    exitApplication()
                }}.start()
            dialog.dismiss()
        }
        dialog.findViewById<LinearLayout>(R.id.min_60)?.setOnClickListener {
            Toast.makeText(baseContext, "Music will stop after 60 Minutes", Toast.LENGTH_SHORT).show()
            binding.timerBtn.setColorFilter(ContextCompat.getColor(this, R.color.teal_200))
            min60 = true
            Thread{Thread.sleep((60 * 60000).toLong())
                if(min60){
                    exitApplication()
                }}.start()
            dialog.dismiss()
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun getMusicDetails(contentUri : Uri): Music {
        var cursor : Cursor? = null
        try{
            val projection = arrayOf(MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.DURATION)
            cursor = this.contentResolver.query(contentUri, projection, null, null, null)
            val dataColumn = cursor?.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val durationColumn = cursor?.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            cursor!!.moveToFirst()
            val path = dataColumn?.let { cursor.getString(it) }
            val duration = durationColumn?.let { cursor.getLong(it) }!!
            return Music(id = "Unknown", title = path.toString(), album = "Unknown", artist = "Unknown", duration = duration,
                path = path.toString(), artUri = "Unknown")
        }finally {
            cursor?.close()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if(musicListPA[songPosition].id == "Unknown" && !isPlaying){
            exitApplication()
        }
    }

    private fun initServiceAndPlaylist(playlist: ArrayList<Music>, shuffle: Boolean, playNext: Boolean = false){
        val intent = Intent(this, MusicService::class.java)
        bindService(intent, this, BIND_AUTO_CREATE)
        startService(intent)
        musicListPA = ArrayList()
        musicListPA.addAll(playlist)
        if(shuffle) musicListPA.shuffle()
        setLayout()
        if(!playNext) PlayNext.playNextList = ArrayList()
    }
}