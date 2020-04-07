package com.simileoluwaaluko.audiomail.inbox

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.simileoluwaaluko.audiomail.Constants.receivingHost
import com.simileoluwaaluko.audiomail.R
import kotlinx.android.synthetic.main.activity_inbox.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.anko.toast
import javax.mail.Folder
import javax.mail.Session
import javax.mail.Store

class InboxActivity : AppCompatActivity() {

    lateinit var folder : Folder
    lateinit var store : Store

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_inbox)

        val credentialsSharedPrefs = getSharedPreferences(getString(R.string.gmail_credentials_shared_prefs), Context.MODE_PRIVATE)
        val userName = credentialsSharedPrefs.getString(getString(R.string.gmailaddress), "")
        val password = credentialsSharedPrefs.getString(getString(R.string.gmailpassword), "")
        if(userName != null && password != null){
            recieveMail(userName, password)
        }else{
            toast("Unable to fetch inboxes, update credentials").show()
        }
    }

    private fun recieveMail(userName : String, password : String){

        val props2 = System.getProperties()
        props2.setProperty("mail.store.protocol", "imaps")
        val session2 = Session.getDefaultInstance(props2, null)

        try {
            store = session2.getStore("imaps")

            GlobalScope.launch {
                store.connect(receivingHost, userName, password)
                folder = store.getFolder("INBOX")
                folder.open(Folder.READ_ONLY)
                val messages = folder.messages

                runOnUiThread {
                    val inboxAdapter = InboxRecyclerAdapter(messages)
                    val inboxLayoutManager = LinearLayoutManager(this@InboxActivity, LinearLayoutManager.VERTICAL, false)
                    inboxes.apply {
                        adapter = inboxAdapter
                        layoutManager = inboxLayoutManager
                    }
                }

            }

        }catch (e : Exception){
            e.printStackTrace()
            toast("Unable to fetch mail, update credentials/check internet connection").show()
        }
    }

    override fun onDestroy() {
        GlobalScope.launch {
            folder.close(true)
            store.close()
        }
        super.onDestroy()
    }
}
