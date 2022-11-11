package com.abhishek.mymusicplayer

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.abhishek.mymusicplayer.databinding.ActivityPlayListBinding
import com.abhishek.mymusicplayer.databinding.AddPlaylistDialogBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.SimpleDateFormat
import java.util.*

class PlayListActivity : AppCompatActivity(){

    private lateinit var binding: ActivityPlayListBinding
    private lateinit var adapter : PlayListAdapter

    companion object{
        var musicPlayList : MusicPlayList = MusicPlayList()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(MainActivity.currentThemeNav[MainActivity.themeIndex])
        binding = ActivityPlayListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.playListRV.setHasFixedSize(true)
        binding.playListRV.setItemViewCacheSize(13)
        binding.playListRV.layoutManager =  GridLayoutManager(this@PlayListActivity, 2)
        adapter = PlayListAdapter(this, playlistList = musicPlayList.ref)
        binding.playListRV.adapter = adapter
        binding.backBtnPL.setOnClickListener{finish()}
        binding.addPlayListBtn.setOnClickListener {
            customAlertDialog()
        }
        if(musicPlayList.ref.isNotEmpty()) binding.instructionPA.visibility = View.GONE
    }

    private fun customAlertDialog(){
        val customDialog = LayoutInflater.from(this@PlayListActivity).inflate(R.layout.add_playlist_dialog, binding.root, false)
        val binder = AddPlaylistDialogBinding.bind(customDialog)
        val builder = MaterialAlertDialogBuilder(this)
        val dialog = builder.setView(customDialog)
            .setTitle("PlayList Details")
            .setPositiveButton("ADD"){dialog, _ ->
                val playListName = binder.playListName.text
                val createdBy = binder.yourName.text
                if(playListName != null && createdBy != null){
                    if(playListName.isNotEmpty() && createdBy.isNotEmpty()){
                        addPlayList(playListName.toString(), createdBy.toString())
                    }
                }
                dialog.dismiss()
            }.create()
        dialog.show()
        setDialogBtnBackground(this, dialog)
    }

    private fun addPlayList(name : String, createdBy : String){
        var playListExists = false
        for(i in musicPlayList.ref){
            if(name == (i.name)){
                playListExists = true
                break
            }
        }
        if(playListExists){
            Toast.makeText(this, "PlayList Already Exists", Toast.LENGTH_SHORT).show()
        }
        else{
            val tempPlayList = PlayList()
            tempPlayList.name = name
            tempPlayList.playList = ArrayList()
            tempPlayList.createdBy = createdBy
            val calendar = Calendar.getInstance().time
            val sdf = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
            tempPlayList.createdOn = sdf.format(calendar)
            musicPlayList.ref.add(tempPlayList)
            adapter.refreshPlayList()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onResume() {
        super.onResume()
        adapter.notifyDataSetChanged()
    }
}