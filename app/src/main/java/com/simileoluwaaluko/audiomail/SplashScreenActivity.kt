package com.simileoluwaaluko.audiomail

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.simileoluwaaluko.audiomail.mainActivity.MainActivity
import org.jetbrains.anko.startActivity

class SplashScreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.AppTheme)
        supportActionBar?.hide()
        setContentView(R.layout.activity_splash_screen)

        // get firstrun data from sharedPreferences db
        val firstRunPrefs = getSharedPreferences(getString(R.string.first_run_prefs), Context.MODE_PRIVATE)
        // if app has been run before go straight to mainActivity and skip register activity
        if(firstRunPrefs.getBoolean(getString(R.string.firstrun), false)) startActivity<MainActivity>()
        // if app first run, go to register activity
        else startActivity<RegisterActivity>()
        // finish the activity after navigating to another activity.
        finish()
    }
}
