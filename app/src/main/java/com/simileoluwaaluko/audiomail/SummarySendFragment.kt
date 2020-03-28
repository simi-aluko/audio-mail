package com.simileoluwaaluko.audiomail


import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_summary_send.*
import org.jetbrains.anko.support.v4.toast
import java.util.*

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

        summarySendFragmentViewModel.mailCCAddress.observe(viewLifecycleOwner, Observer {
            summary_cc.setText(it)
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
                var cc = summary_cc.text.toString()
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
                summarySendFragmentViewModel.mailCCAddress.value = ""
                summarySendFragmentViewModel.mailBody.value = ""
                summarySendFragmentViewModel.summarySendFragmentTextToBeSpoken.value = "Mail has been cleared."
            }
            "3","three","tree" -> {
                toast("sending mail").show()
            }
            "4","four","for" -> {
                summarySendFragmentViewModel.summarySendFragmentTextToBeSpoken.value = getString(R.string.mail_summarysend_tts)
            }
            "5", "five" -> {
                val action = SummarySendFragmentDirections.backNavigation()
                findNavController().navigate(action)
            }
            else -> summarySendFragmentViewModel.summarySendFragmentTextToBeSpoken.value = getString(R.string.unknown_command)
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
}
