package com.abhishek.mymusicplayer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.abhishek.mymusicplayer.databinding.ActivityAboutBinding

class AboutActivity : AppCompatActivity() {

    lateinit var binding : ActivityAboutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(MainActivity.currentThemeNav[MainActivity.themeIndex])
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.title = "About"

        binding.aboutText.text = aboutText()
    }

    private fun aboutText() : String{
        return "This App was Developed by:-\n\t\t\tAbhishek Shrivastava" +
                "\n\nIf you want to provide any feedback, I will love to hear that."
    }
}