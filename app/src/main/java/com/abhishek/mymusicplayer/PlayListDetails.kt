package com.abhishek.mymusicplayer

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.abhishek.mymusicplayer.databinding.ActivityPlayListDetailsBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.GsonBuilder

class PlayListDetails : AppCompatActivity() {

    lateinit var binding : ActivityPlayListDetailsBinding
    lateinit var adapter : MusicAdapter

    companion object{
        var currentPlayListPos : Int = -1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(MainActivity.currentThemeNav[MainActivity.themeIndex])
        binding = ActivityPlayListDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        currentPlayListPos = intent.extras?.get("index") as Int
        try{
            PlayListActivity.musicPlayList.ref[currentPlayListPos].playList =
                checkPlayList(playList = PlayListActivity.musicPlayList.ref[currentPlayListPos].playList)
        }catch(e : Exception){}
        binding.playListDetailsRV.setItemViewCacheSize(10)
        binding.playListDetailsRV.setHasFixedSize(true)
        binding.playListDetailsRV.layoutManager = LinearLayoutManager(this)
        adapter = MusicAdapter(this, PlayListActivity.musicPlayList.ref[currentPlayListPos].playList, playListDetails = true)
        binding.playListDetailsRV.adapter = adapter
        binding.backBtnPLD.setOnClickListener { finish() }
        binding.shuffleBtnPD.setOnClickListener {
            val intent = Intent(this, PlayerActivity::class.java)
            intent.putExtra("index", 0)
            intent.putExtra("class", "PlayListDetailsShuffle")
            startActivity(intent)
        }
        binding.addBtnPD.setOnClickListener {
            startActivity(Intent(this, SelectionActivity::class.java))
        }
        binding.removeAllPD.setOnClickListener {
            val builder = MaterialAlertDialogBuilder(this)
            builder.setTitle("Remove")
                .setMessage("Do you want to remove all songs")
                .setPositiveButton("Yes"){dialog, _ ->
                    PlayListActivity.musicPlayList.ref[currentPlayListPos].playList.clear()
                    adapter.refreshPlayList()
                    dialog.dismiss()
                }
                .setNegativeButton("No"){ dialog, _ ->
                    dialog.dismiss()
                }
            val customDialog = builder.create()
            customDialog.show()
            setDialogBtnBackground(this, customDialog)
//            customDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED)
//            customDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.RED)
        }
    }

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    override fun onResume() {
        super.onResume()
        binding.playListNamePD.text = PlayListActivity.musicPlayList.ref[currentPlayListPos].name
        binding.moreInfoPD.text = "Total ${adapter.itemCount} Songs.\n\n" +
                "Created On:\n${PlayListActivity.musicPlayList.ref[currentPlayListPos].createdOn}\n\n" +
                " -- ${PlayListActivity.musicPlayList.ref[currentPlayListPos].createdBy}"

        if(adapter.itemCount > 0){
            Glide.with(this)
                .load(PlayListActivity.musicPlayList.ref[currentPlayListPos].playList[0].artUri)
                .apply(RequestOptions().placeholder(R.drawable.music_icon_splash_screen).centerCrop())
                .into(binding.playListImgPD)
            binding.shuffleBtnPD.visibility = View.VISIBLE
        }
        adapter.notifyDataSetChanged()
        //        storing data using sharedPreferences
        val editor = getSharedPreferences("FAVOURITES", MODE_PRIVATE).edit()
        val jsonStringPlayList = GsonBuilder().create().toJson(PlayListActivity.musicPlayList)
        editor.putString("MusicPlayList", jsonStringPlayList)
        editor.apply()
    }
}