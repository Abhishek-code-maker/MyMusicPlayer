package com.abhishek.mymusicplayer

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.abhishek.mymusicplayer.databinding.PlaylistViewBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class PlayListAdapter(private val context: Context, private var playlistList: ArrayList<PlayList>) : RecyclerView.Adapter<PlayListAdapter.MyHolder>(){

    class MyHolder(binding: PlaylistViewBinding) : RecyclerView.ViewHolder(binding.root) {
        val image = binding.playListImg
        val name = binding.playListName
        val root = binding.root
        val delete = binding.playListDelete
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        return MyHolder(PlaylistViewBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        if(MainActivity.themeIndex == 4){
            holder.root.strokeColor = ContextCompat.getColor(context, R.color.white)
        }
        holder.name.text = playlistList[position].name
        holder.name.isSelected = true
        holder.delete.setOnClickListener {
            val builder = MaterialAlertDialogBuilder(context)
            builder.setTitle(playlistList[position].name)
                .setMessage("Do You Want To Delete PlayList")
                .setPositiveButton("Yes"){ dialog, _ ->
                    PlayListActivity.musicPlayList.ref.removeAt(position)
                    refreshPlayList()
                    dialog.dismiss()
                }
                .setNegativeButton("No"){ dialog, _ ->
                    dialog.dismiss()
                }
            val customDialog = builder.create()
            customDialog.show()
            setDialogBtnBackground(context, customDialog)
//            customDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED)
//            customDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.RED)
        }
        holder.root.setOnClickListener {
            val intent = Intent(context, PlayListDetails::class.java)
            intent.putExtra("index", position)
            ContextCompat.startActivity(context, intent, null)
        }
        if(PlayListActivity.musicPlayList.ref[position].playList.size > 0){
            Glide.with(context)
                .load(PlayListActivity.musicPlayList.ref[position].playList[0].artUri)
                .apply(RequestOptions().placeholder(R.drawable.music_icon_splash_screen).centerCrop())
                .into(holder.image)
        }
    }

    override fun getItemCount(): Int {
        return playlistList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun refreshPlayList(){
        playlistList = ArrayList()
        playlistList.addAll(PlayListActivity.musicPlayList.ref)
        notifyDataSetChanged()
    }

}
