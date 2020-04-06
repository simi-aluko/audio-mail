package com.simileoluwaaluko.audiomail.fragments


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.simileoluwaaluko.audiomail.Constants.sendingHost
import com.simileoluwaaluko.audiomail.Constants.sendingPort
import com.simileoluwaaluko.audiomail.mainActivity.MainActivityViewModel
import com.simileoluwaaluko.audiomail.R
import com.simileoluwaaluko.audiomail.fragments.SummarySendFragmentDirections
import com.simileoluwaaluko.audiomail.mainActivity.MainActivity
import kotlinx.android.synthetic.main.fragment_summary_send.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.anko.support.v4.runOnUiThread
import org.jetbrains.anko.support.v4.toast
import org.jetbrains.anko.toast
import java.util.*
import javax.mail.Message
import javax.mail.MessagingException
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.AddressException
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage


/**
 * A simple [Fragment] subclass.
 */
class SummarySendFragment : Fragment(),View.OnClickListener, TextToSpeech.OnInitListener {

    private val summarySendFragmentViewModel: MainActivityViewModel by activityViewModels()
    lateinit var textToSpeech: TextToSpeech
    private val instructionRequest = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_summary_send, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (activity as MainActivity).supportActionBar?.title = getString(R.string.mail_summarysend)
        summarysend_instruction_button.setOnClickListener(this)
        setUpLiveData()
    }

    private fun setUpLiveData(){
        summarySendFragmentViewModel.summarySendFragmentTextToBeSpoken.observe(viewLifecycleOwner, Observer {
            textToSpeech = TextToSpeech(context, this)
        })

        summarySendFragmentViewModel.mailRecipientAddress.observe(viewLifecycleOwner, Observer {
            summary_recipient.setText(it)
        })

        summarySendFragmentViewModel.mailSubject.observe(viewLifecycleOwner, Observer {
            summary_subject.setText(it)
        })

        summarySendFragmentViewModel.mailBody.observe(viewLifecycleOwner, Observer {
            summary_body.setText(it)
        })
    }

    override fun onClick(v: View?) {
        when(v?.id){
            summarysend_instruction_button.id -> {
                callSpeechToText(instructionRequest)
            }
        }
    }

    private fun callSpeechToText(requestCode: Int){
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())

        if(intent.resolveActivity(activity!!.packageManager) != null){
            startActivityForResult(intent, requestCode)
        }else toast("Your device doesn't support speech input").show()
    }

    override fun onInit(status: Int) {
        if(status != TextToSpeech.ERROR){
            val result = textToSpeech.setLanguage(Locale.US)
            if(result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED){
                Log.e(tag, "$result language not supported")
            }else {
                val textToSpeak = summarySendFragmentViewModel.summarySendFragmentTextToBeSpoken.value
                if (textToSpeak != null) textToSpeech.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null, null)
            }
        }else{
            toast("TextToSpeech Initialisation failed").show()
            Log.e(tag, "$status initialisation failed")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when(requestCode){
            instructionRequest -> {
                if(resultCode == Activity.RESULT_OK && data != null){
                    val results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    if(results!=null) {
                        handleInstructionRequestResult(results[0])
                    }
                }
            }
        }
    }


    private fun handleInstructionRequestResult(spokenText : String){
        when(spokenText){
            "1","one" -> {
                var recipient = summary_recipient.text.toString()
                var cc = summary_subject.text.toString()
                var body = summary_body.text.toString()

                if(recipient.isEmpty()) recipient = "empty"
                if (cc.isEmpty()) cc = "empty"
                if(body.isEmpty()) body = "empty"

                val mailAddress = "Mail Address, is $recipient,,"
                val mailCC = "Mail CC, is $cc,,"
                val mailBody = "Mail Body, is $body"
                val textToSpeak = mailAddress + mailCC + mailBody
                if(textToSpeak.isNotEmpty()) summarySendFragmentViewModel.summarySendFragmentTextToBeSpoken.value = textToSpeak
                else summarySendFragmentViewModel.summarySendFragmentTextToBeSpoken.value = "Mail is empty."
            }
            "2","to","two","too" -> {
                summarySendFragmentViewModel.mailRecipientAddress.value = ""
                summarySendFragmentViewModel.mailSubject.value = ""
                summarySendFragmentViewModel.mailBody.value = ""
                summarySendFragmentViewModel.summarySendFragmentTextToBeSpoken.value = "Mail has been cleared."
            }
            "3","three","tree" -> {
                sendMail()
            }
            "4","four","for" -> {
                summarySendFragmentViewModel.summarySendFragmentTextToBeSpoken.value = getString(
                    R.string.mail_summarysend_tts
                )
            }
            "5", "five" -> {
                val action =
                    SummarySendFragmentDirections.backNavigation()
                findNavController().navigate(action)
            }
            else -> summarySendFragmentViewModel.summarySendFragmentTextToBeSpoken.value = getString(
                R.string.unknown_command
            )
        }
    }

    override fun onDestroy() {
        if(::textToSpeech.isInitialized){
            textToSpeech.stop()
            textToSpeech.shutdown()
        }
        super.onDestroy()
    }

    override fun onPause() {
        if(::textToSpeech.isInitialized){
            textToSpeech.stop()
            textToSpeech.shutdown()
        }
        super.onPause()
    }

    private fun sendMail(){
        val emailAddress = summary_recipient.text.toString().trim()
        val mailBody = summary_body.text.toString().trim()
        val mailSubject = summary_subject.text.toString().trim()

        if(emailAddress.isNotEmpty() && mailBody.isNotEmpty() && mailSubject.isNotEmpty()){
            context?.let {
                val credentialsSharedPrefs = it.getSharedPreferences(getString(R.string.gmail_credentials_shared_prefs), Context.MODE_PRIVATE)
                val userName = credentialsSharedPrefs.getString(getString(R.string.gmailaddress), "")
                val password = credentialsSharedPrefs.getString(getString(R.string.gmailpassword), "")

                if(userName != null && password != null){
                    GlobalScope.launch {
                        sendMail(userName, emailAddress, mailSubject, mailBody, password)
                    }
                }else{
                    summarySendFragmentViewModel.summarySendFragmentTextToBeSpoken.value = "Unable to Send mail, update credentials"
                }
            }
        }else{
            summarySendFragmentViewModel.summarySendFragmentTextToBeSpoken.value = "Unable to send mail, No field in mail should be empty."
        }
    }

    private suspend fun sendMail(userName : String, to : String, subject : String, text : String, password : String){
        val props = Properties()
        props.put("mail.smtp.host", sendingHost)
        props.put("mail.smtp.port", sendingPort.toShort())
        props.put("mail.smtp.user", userName)
        props.put("mail.smtp.password", password)
        props.put("mail.smtp.auth", "true")

        val session1 = Session.getDefaultInstance(props)
        val simpleMessage = MimeMessage(session1)

        var fromAddress : InternetAddress? = null
        var toAddress : InternetAddress? = null

        try {
            fromAddress = InternetAddress(userName)
            toAddress = InternetAddress(to)
        }catch (e : AddressException){
            e.printStackTrace()
            summarySendFragmentViewModel.summarySendFragmentTextToBeSpoken.value = "Sending mail failed!"
        }

        try {
            simpleMessage.setFrom(fromAddress)
            simpleMessage.setRecipient(Message.RecipientType.TO, toAddress)
            simpleMessage.subject = subject
            simpleMessage.setText(text)

            val transport: Transport = session1.getTransport("smtps")

            transport.connect(sendingHost, sendingPort, userName, password)
            transport.sendMessage(simpleMessage, simpleMessage.allRecipients)
            transport.close()
            runOnUiThread {
                summarySendFragmentViewModel.summarySendFragmentTextToBeSpoken.value = "Mail Sent successfully."
                toast("Mail Sent successfully.").show()
                summary_recipient.setText("")
                summary_body.setText("")
                summary_subject.setText("")
                summarySendFragmentViewModel.mailRecipientAddress.value = ""
                summarySendFragmentViewModel.mailSubject.value = ""
                summarySendFragmentViewModel.mailBody.value = ""
            }
        }catch (e : MessagingException){
            e.printStackTrace()
            runOnUiThread {
                summarySendFragmentViewModel.summarySendFragmentTextToBeSpoken.value = "Sending mail failed!"
                toast("Sending mail failed!").show()
            }
        }
    }
}
