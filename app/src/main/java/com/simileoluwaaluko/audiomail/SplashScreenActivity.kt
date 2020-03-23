package com.simileoluwaaluko.audiomail

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import org.jetbrains.anko.startActivity

class SplashScreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.AppTheme)
        supportActionBar?.hide()
        setContentView(R.layout.activity_splash_screen)

        startActivity<MainActivity>()
        finish()
    }
}
