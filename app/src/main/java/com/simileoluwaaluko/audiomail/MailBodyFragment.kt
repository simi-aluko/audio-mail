package com.simileoluwaaluko.audiomail


import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_mail_body.*
import org.jetbrains.anko.support.v4.toast
import java.util.*


/**
 * A simple [Fragment] subclass.
 */
class MailBodyFragment : Fragment(), View.OnClickListener, TextToSpeech.OnInitListener{

    private val mailBodyFragmentViewModel: MainActivityViewModel by activityViewModels()
    lateinit var textToSpeech: TextToSpeech
    private val instructionRequest = 1
    private val readOutRequest = 2

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_mail_body, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        body_read_out_button.setOnClickListener(this)
        body_instruction_button.setOnClickListener(this)
        setUpLiveData()
    }

    private fun setUpLiveData(){
        mailBodyFragmentViewModel.mailBodyFragmentTextToBeSpoken.observe(viewLifecycleOwner, Observer {
            textToSpeech = TextToSpeech(context, this)
        })

        mailBodyFragmentViewModel.mailBody.observe(viewLifecycleOwner, Observer {
            mail_body_edittext.setText(it)
        })
    }

    override fun onClick(v: View?) {
        when(v?.id){
            body_read_out_button.id -> {
                callSpeechToText(readOutRequest)
            }

            body_instruction_button.id -> {
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
                val textToSpeak = mailBodyFragmentViewModel.mailBodyFragmentTextToBeSpoken.value
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

            readOutRequest -> {
                if(resultCode == Activity.RESULT_OK && data != null){
                    val results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    if(results!=null){
                        handleMessageBodyInput(results[0])
                    }
                }
            }
        }
    }

    private fun handleMessageBodyInput(spokenText: String){
        mailBodyFragmentViewModel.mailBody.value = mailBodyFragmentViewModel.mailBody.value + spokenText
    }

    private fun handleInstructionRequestResult(spokenText : String){
        when(spokenText){
            "1","one" -> {
                val textToSpeak = mail_body_edittext.text.toString()
                if(textToSpeak.isNotEmpty()) mailBodyFragmentViewModel.mailBodyFragmentTextToBeSpoken.value = textToSpeak
                else mailBodyFragmentViewModel.mailBodyFragmentTextToBeSpoken.value = "Mail's body is empty."
            }
            "2","to","two","too" -> {
                mail_body_edittext.setText("")
                mailBodyFragmentViewModel.mailBodyFragmentTextToBeSpoken.value = "Mail's body has been cleared."
            }
            "3","three","tree" -> {
                val action = MailBodyFragmentDirections.backNavigation()
                findNavController().navigate(action)
            }
            "4","four","for" -> {
                mailBodyFragmentViewModel.mailBodyFragmentTextToBeSpoken.value = getString(R.string.mail_body_tts)
            }
            else -> mailBodyFragmentViewModel.mailBodyFragmentTextToBeSpoken.value = getString(R.string.unknown_command)
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
