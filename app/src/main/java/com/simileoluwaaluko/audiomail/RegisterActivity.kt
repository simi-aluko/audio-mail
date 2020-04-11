package com.simileoluwaaluko.audiomail

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.View
import com.simileoluwaaluko.audiomail.mainActivity.MainActivity
import kotlinx.android.synthetic.main.activity_register.*
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
import java.util.*

class RegisterActivity : AppCompatActivity(), View.OnClickListener, TextToSpeech.OnInitListener {
    val tag = "RegisterActivity"
    lateinit var textToSpeech: TextToSpeech

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_register)

        // set onclicklisteners to buttons to cause a code to run when you click on it
        save_btn.setOnClickListener(this)
        allow_less_secure_apps.setOnClickListener(this)

        // initialise Android textToSpeech
        textToSpeech = TextToSpeech(this, this)
    }

    // click actions for buttons
    override fun onClick(v: View?) {
        when(v?.id){
            save_btn.id -> {
                val gmailAddress = gmail_address.text.trim().toString()
                val gmailPassword = gmail_password.text.trim().toString()
                if (validateInputs(gmailAddress, gmailPassword)){
                    toast("Credentials saved successfully").show()
                    saveCredentials(gmailAddress, gmailPassword)
                    startActivity<MainActivity>()
                }else{
                    toast("No inputs should be empty").show()
                }
            }
            allow_less_secure_apps.id -> {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://myaccount.google.com/lesssecureapps?pli=1"))
                startActivity(browserIntent)
            }
        }
    }

    // check that gmail address and password fields arent empty
    private fun validateInputs(gmailAddress : String, gmailPassword : String) : Boolean{
        return gmailAddress.isNotEmpty() && gmailPassword.isNotEmpty()
    }

    // save gmail address and password to sharedpreferences (a database in android)
    private fun saveCredentials(gmailAddress : String, password : String){
        // get sharedpreferences
        val credentialsPreferences = getSharedPreferences(getString(R.string.gmail_credentials_shared_prefs), Context.MODE_PRIVATE)

        // write to the db
        with(credentialsPreferences.edit()){
            putString(getString(R.string.gmailaddress), gmailAddress)
            putString(getString(R.string.gmailpassword), password)
            apply()
        }

        val firstRunPrefs = getSharedPreferences(getString(R.string.first_run_prefs), Context.MODE_PRIVATE)
        with(firstRunPrefs.edit()){
            putBoolean(getString(R.string.firstrun), true)
            apply()
        }

        finish()
    }

    //this callback is called when textToSpeech is initialised. Text to speech works after it is initialised
    override fun onInit(status: Int) {
        if(status != TextToSpeech.ERROR){
            val result = textToSpeech.setLanguage(Locale.US)
            if(result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED){
                toast("Text to Speech: Language not supported").show()
                Log.e(tag, "$result language not supported")
            }else {
                val textToSpeak = "Welcome to the registration page. Follow the blue link below the save button to enable the app to " +
                        "access your gmail account. You are meant to enter your gmail address and gmail account password in this page." +
                        " Get assistance from someone you trust to complete this registration."

                // speak text if textToSpeech is initialised.9
                if (textToSpeak != null) textToSpeech.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null, null)
            }
        }else{
            toast("Text to Speech Initialisation failed").show()
            Log.e(tag, "$status initialisation failed")
        }
    }

    // stop and release textToSpeech when activity is destroyed/closed
    override fun onDestroy() {
        if(::textToSpeech.isInitialized){
            textToSpeech.stop()
            textToSpeech.shutdown()
        }
        super.onDestroy()
    }

    // stop and release textToSpeech when activity is in paused state.
    override fun onPause() {
        if(::textToSpeech.isInitialized){
            textToSpeech.stop()
            textToSpeech.shutdown()
        }
        super.onPause()
    }
}
