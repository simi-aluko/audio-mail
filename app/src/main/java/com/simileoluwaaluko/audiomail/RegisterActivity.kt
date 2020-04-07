package com.simileoluwaaluko.audiomail

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.simileoluwaaluko.audiomail.mainActivity.MainActivity
import kotlinx.android.synthetic.main.activity_register.*
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast

class RegisterActivity : AppCompatActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_register)
        save_btn.setOnClickListener(this)
        allow_less_secure_apps.setOnClickListener(this)
    }

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

    private fun validateInputs(gmailAddress : String, gmailPassword : String) : Boolean{
        return gmailAddress.isNotEmpty() && gmailPassword.isNotEmpty()
    }

    private fun saveCredentials(gmailAddress : String, password : String){
        val credentialsPreferences = getSharedPreferences(getString(R.string.gmail_credentials_shared_prefs), Context.MODE_PRIVATE)
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
}
