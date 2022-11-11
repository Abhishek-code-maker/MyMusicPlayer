package com.abhishek.mymusicplayer

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.abhishek.mymusicplayer.databinding.ActivitySettingsBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class SettingsActivity : AppCompatActivity() {

    lateinit var binding : ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(MainActivity.currentThemeNav[MainActivity.themeIndex])
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.title = "Settings"
        when(MainActivity.themeIndex){
            0 -> binding.coolBlue.setBackgroundColor(Color.DKGRAY)
            1 -> binding.coolPurple.setBackgroundColor(Color.DKGRAY)
            2 -> binding.coolOrange.setBackgroundColor(Color.DKGRAY)
            3 -> binding.coolRed.setBackgroundColor(Color.DKGRAY)
            4 -> binding.blackTheme.setBackgroundColor(Color.DKGRAY)
        }
        binding.coolBlue.setOnClickListener {
            saveTheme(0)
        }
        binding.coolPurple.setOnClickListener {
            saveTheme(1)
        }
        binding.coolOrange.setOnClickListener {
            saveTheme(2)
        }
        binding.coolRed.setOnClickListener {
            saveTheme(3)
        }
        binding.blackTheme.setOnClickListener {
            saveTheme(4)
        }
        binding.versionName.text = setVersionDetails()
        binding.sortBtn.setOnClickListener {
            val menuList = arrayOf("Recently Added", "Song Title", "File Size")
            var currentSort = MainActivity.sortOrder
            val builder = MaterialAlertDialogBuilder(this)
            builder.setTitle("Sorting")
                .setPositiveButton("OK"){_, _ ->
                    val editor = getSharedPreferences("SORTING", MODE_PRIVATE).edit()
                    editor.putInt("sortOrder", currentSort)
                    editor.apply()
                }
                .setSingleChoiceItems(menuList, currentSort){ _,which ->
                    currentSort = which
                }
            val customDialog = builder.create()
            customDialog.show()
            setDialogBtnBackground(this, customDialog)
//            customDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED)
        }
    }

    private fun saveTheme(index : Int){
        if(MainActivity.themeIndex != index){
            val editor = getSharedPreferences("THEMES", MODE_PRIVATE).edit()
            editor.putInt("themeIndex", index)
            editor.apply()
            val builder = MaterialAlertDialogBuilder(this)
            builder.setTitle("Apply Theme")
                .setMessage("Do You Want To Apply Theme")
                .setPositiveButton("Yes"){ _, _ ->
                    exitApplication()
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

    private fun setVersionDetails() : String{
        return "Version Name : ${BuildConfig.VERSION_NAME}"
    }
}