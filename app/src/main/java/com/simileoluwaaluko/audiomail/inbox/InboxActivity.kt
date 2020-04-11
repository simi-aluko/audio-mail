package com.simileoluwaaluko.audiomail.inbox

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.simileoluwaaluko.audiomail.Constants.receivingHost
import com.simileoluwaaluko.audiomail.R
import kotlinx.android.synthetic.main.activity_inbox.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main
import org.jetbrains.anko.toast
import java.io.IOException
import java.util.*
import javax.mail.*
import javax.mail.internet.ContentType
import javax.mail.internet.MimeMultipart
import javax.mail.search.ComparisonTerm
import javax.mail.search.ReceivedDateTerm


class InboxActivity : AppCompatActivity(), View.OnClickListener, TextToSpeech.OnInitListener {

    lateinit var folder : Folder
    lateinit var store : Store
    lateinit var inboxActivityViewModel: InboxActivityViewModel
    lateinit var textToSpeech: TextToSpeech
    lateinit var inboxMessages : Array<Message>
    var mailBody : String? = null
    private val speechToTextRequestCode = 1
    val tag = "InboxActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_inbox)

        // retrieve gmail username and password from shared preferences db
        val credentialsSharedPrefs = getSharedPreferences(getString(R.string.gmail_credentials_shared_prefs), Context.MODE_PRIVATE)
        val userName = credentialsSharedPrefs.getString(getString(R.string.gmailaddress), "")
        val password = credentialsSharedPrefs.getString(getString(R.string.gmailpassword), "")

        // if username and password is not null fetch mail otherwise show toast message.
        if(userName != null && password != null){
            receiveMail(userName, password)
        }else{
            inboxActivityViewModel.textToSpeak.value = "Unable to fetch mail inboxes, update gmail registration credentials."
            toast("Unable to fetch inboxes, update credentials").show()
        }

        // set onclicklisteners
        back_arrow.setOnClickListener(this)
        inbox_read_mail_btn.setOnClickListener(this)
        inbox_instruction_button.setOnClickListener(this)

        // initialise viewmodel
        inboxActivityViewModel = ViewModelProvider(this)[InboxActivityViewModel::class.java]

        // initialise livedata
        setUpLiveData()
    }

    // method to get emails
    private fun receiveMail(userName : String, password : String){

        val props2 = System.getProperties()
        props2.setProperty("mail.store.protocol", "imaps")
        val session2 = Session.getDefaultInstance(props2, null)

        store = session2.getStore("imaps")

        // fetching mail has to be done on a background thread
        GlobalScope.launch {
            try{
                store.connect(receivingHost, userName, password)
                folder = store.getFolder("INBOX")
                folder.open(Folder.READ_ONLY)

                // get mails for current day of the month.
                val cal = Calendar.getInstance()
                cal.add(Calendar.DAY_OF_MONTH, 0)
                // search term to filter emails that would be got from the server
                val term = ReceivedDateTerm(ComparisonTerm.EQ, Date(cal.timeInMillis))
                //get the mails
                val messages = folder.search(term)
                inboxMessages = messages

                // accessing the ui in android has to be done on the UIthread or mainthread
                runOnUiThread {
                    inboxActivityViewModel.textToSpeak.value =
                        "You have ${messages.size} emails for today!.\n" + getString(R.string.inbox_tts)
                    val inboxAdapter = InboxRecyclerAdapter(messages)
                    val inboxLayoutManager = LinearLayoutManager(this@InboxActivity, LinearLayoutManager.VERTICAL, false)
                    inboxes.apply {
                        adapter = inboxAdapter
                        layoutManager = inboxLayoutManager
                    }
                    inboxes_no.text = "Inbox for today(${messages.size})"
                }
            }catch (e : Exception){
                e.printStackTrace()
                runOnUiThread {
                    inboxActivityViewModel.textToSpeak.value = "Unable to fetch mail, check gmail credentials/internet connection or something went wrong"
                    toast("Unable to fetch mail, check mail credentials/check internet connection").show()
                }
            }
        }
    }

    // stop and release textToSpeech when activity is destroyed/closed
    override fun onDestroy() {
        GlobalScope.launch {
            if(::folder.isInitialized && ::store.isInitialized){
                folder.close(true)
                store.close()
            }
        }
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

    override fun onClick(v: View?) {
        when(v?.id){
            back_arrow.id -> onBackPressed()
            inbox_read_mail_btn.id -> {
                if(::textToSpeech.isInitialized){
                    textToSpeech.stop()
                    textToSpeech.shutdown()
                }
                if(::inboxMessages.isInitialized){
                    inbox_read_mail_btn.isEnabled = false
                    val message = inboxMessages[0]
                    CoroutineScope(Dispatchers.IO).launch{
                        val subject : Deferred<String> = async { message.subject }
                        val from : Deferred<String> = async { message.from[0].toString() }
                        withContext(Main){
                            val textToSpeak = "Mail from: ${from.await()}. The title is ${subject.await()}"
                            inboxActivityViewModel.textToSpeak.value = textToSpeak
                            if(inboxMessages.isNotEmpty()) inboxMessages = inboxMessages.drop(1).toTypedArray()
                        }
                        mailBody = getTextFromMessage(message)
                    }
                    inbox_read_mail_btn.isEnabled = true
                }
            }
            inbox_instruction_button.id -> {
                if(::textToSpeech.isInitialized){
                    textToSpeech.stop()
                    textToSpeech.shutdown()
                }
                if(::inboxMessages.isInitialized){
                    callSpeechToText()
                }
            }
        }
    }

    // this gets run when "" TextToSpeech(this@InboxActivity, this) "" is run. we are using a live data to initialise this.
    // anytime we set a value to "" inboxActivityViewModel.textToSpeak.value = "... " textToSpeech gets initialsed because
    // we are using a livedata to initialise it. Observers to liveData gets called when it the livedata value changes. And in
    // this case, we are initialising textTospeech, and after initialising textToSpeech, this onInit(status : Int) method
    // below gets called. because it gets called when TextToSpeech is initialised.
    override fun onInit(status: Int) {
        if(status != TextToSpeech.ERROR){
            val result = textToSpeech.setLanguage(Locale.US)
            if(result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED){
                toast("Text to Speech: Language not supported").show()
                Log.e(tag, "$result language not supported")
            }else {
                val textToSpeak = inboxActivityViewModel.textToSpeak.value
                if (textToSpeak != null) textToSpeech.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null, null)
            }
        }else{
            toast("Text to Speech Initialisation failed").show()
            Log.e(tag, "$status initialisation failed")
        }
    }

    private fun setUpLiveData(){
        inboxActivityViewModel.textToSpeak.observe(this, androidx.lifecycle.Observer {
            textToSpeech = TextToSpeech(this@InboxActivity, this)
        })
    }

    private fun callSpeechToText(){
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())

        if(intent.resolveActivity(packageManager) != null){
            startActivityForResult(intent, speechToTextRequestCode)
        }else toast("Your device doesn't support speech input").show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when(requestCode){
            speechToTextRequestCode -> {
                if(resultCode == Activity.RESULT_OK && data != null){
                    val results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    if(results!=null) {
                        when(results[0]){
                            "1","one" -> {
                                if(!mailBody.isNullOrBlank()){
                                    inboxActivityViewModel.textToSpeak.value = mailBody
                                }else{
                                    inboxActivityViewModel.textToSpeak.value = "Unable to read mail body"
                                }
                            }
                            "2","to","two","too" -> {
                                onBackPressed()
                            }
                            "3","three","tree" -> {
                                inboxActivityViewModel.textToSpeak.value = getString(R.string.inbox_tts)
                            }else -> {
                                inboxActivityViewModel.textToSpeak.value = getString(
                                    R.string.unknown_command
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    @Throws(IOException::class, MessagingException::class)
    private fun getTextFromMessage(message: Message): String? {
        var result: String? = ""
        if (message.isMimeType("text/plain")) {
            result = message.content.toString()
        } else if (message.isMimeType("multipart/*")) {
            val mimeMultipart: MimeMultipart = message.content as MimeMultipart
            result = getTextFromMimeMultipart(mimeMultipart)
        }
        return result
    }

    @Throws(IOException::class, MessagingException::class)
    private fun getTextFromMimeMultipart(
        mimeMultipart: MimeMultipart
    ): String? {
        val count: Int = mimeMultipart.getCount()
        if (count == 0) throw MessagingException("Multipart with no body parts not supported.")
        val multipartAlt: Boolean =
            ContentType(mimeMultipart.contentType).match("multipart/alternative")
        if (multipartAlt) // alternatives appear in an order of increasing
        // faithfulness to the original content. Customize as req'd.
            return getTextFromBodyPart(mimeMultipart.getBodyPart(count - 1))
        var result: String? = ""
        for (i in 0 until count) {
            val bodyPart: BodyPart = mimeMultipart.getBodyPart(i)
            result += getTextFromBodyPart(bodyPart)
        }
        return result
    }

    @Throws(IOException::class, MessagingException::class)
    private fun getTextFromBodyPart(
        bodyPart: BodyPart
    ): String? {
        var result: String? = ""
        if (bodyPart.isMimeType("text/plain")) {
            result = bodyPart.content as String
        } else if (bodyPart.isMimeType("text/html")) {
            val html = bodyPart.content as String
            result = org.jsoup.Jsoup.parse(html).text()
        } else if (bodyPart.content is MimeMultipart) {
            result = getTextFromMimeMultipart(bodyPart.content as MimeMultipart)
        }
        return result
    }
}
