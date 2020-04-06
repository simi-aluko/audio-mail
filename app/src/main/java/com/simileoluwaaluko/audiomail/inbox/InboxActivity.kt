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

class InboxActivity : AppCompatActivity() {

    lateinit var inboxAdapter : InboxRecyclerAdapter
    var inboxRecyclerResource = arrayListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_inbox)

        val credentialsSharedPrefs = getSharedPreferences(getString(R.string.gmail_credentials_shared_prefs), Context.MODE_PRIVATE)
        val userName = credentialsSharedPrefs.getString(getString(R.string.gmailaddress), "")
        val password = credentialsSharedPrefs.getString(getString(R.string.gmailpassword), "")
        Log.d("simi-oc", "here")
        if(userName != null && password != null){
            Log.d("simi-check", "$userName $password")
            GlobalScope.launch {
                recieveMail(userName, password)
            }
        }else{
            toast("Unable to fetch inboxes, update credentials").show()
        }

        inboxAdapter = InboxRecyclerAdapter(inboxRecyclerResource)
        val inboxLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        inboxes.apply {
            adapter = inboxAdapter
            layoutManager = inboxLayoutManager
        }
    }

    private suspend fun recieveMail(userName : String, password : String){

        Log.d("simi-rm", "here")
        val props2 = System.getProperties()
        props2.setProperty("mail.store.protocol", "imaps")
        val session2 = Session.getDefaultInstance(props2, null)

        try {
            Log.d("simi-rm-try", "here")
            val store = session2.getStore("imaps")
            store.connect(receivingHost, userName, password)
            val folder = store.getFolder("INBOX")
            folder.open(Folder.READ_ONLY)

            val messages = folder.messages
            Log.d("simi-rm-mess", messages[0].subject)
            for (message in messages){
                inboxRecyclerResource.add(message.subject)
                runOnUiThread {
                    inboxAdapter.updateResource(inboxRecyclerResource)
                    Log.d("simi-inrouit", inboxRecyclerResource.toString())
                }
            }
            Log.d("simi-rm-try2", "here")
            folder.close(true)
            Log.d("simi-rm-try3", "here")
            store.close()
        }catch (e : Exception){
            e.printStackTrace()
            runOnUiThread {
                toast("Unable to fetch mail, update credentials/check internet connection").show()
            }
        }
    }
}
