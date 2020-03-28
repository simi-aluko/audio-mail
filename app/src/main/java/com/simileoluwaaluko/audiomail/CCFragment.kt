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
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_cc.*
import org.jetbrains.anko.support.v4.toast
import java.util.*

/**
 * A simple [Fragment] subclass.
 */
class CCFragment : Fragment(), View.OnClickListener, TextToSpeech.OnInitListener {

    private val ccFragmentViewModel: MainActivityViewModel by activityViewModels()
    lateinit var textToSpeech: TextToSpeech
    private val instructionRequest = 1
    private val readOutRequest = 2


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_cc, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        cc_read_out_button.setOnClickListener(this)
        cc_instruction_button.setOnClickListener(this)
        setUpLiveData()
    }

    private fun setUpLiveData(){
        ccFragmentViewModel.ccFragmentTextToBeSpoken.observe(viewLifecycleOwner, Observer {
            textToSpeech = TextToSpeech(context, this)
        })

        ccFragmentViewModel.mailCCAddress.observe(viewLifecycleOwner, Observer {
            mail_cc_edittext.setText(it)
        })
    }

    override fun onClick(v: View?) {
        when(v?.id){
            cc_read_out_button.id -> {
                callSpeechToText(readOutRequest)
            }

            cc_instruction_button.id -> {
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
                val textToSpeak = ccFragmentViewModel.ccFragmentTextToBeSpoken.value
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
                    if(results!=null) {
                        handleReadOutRequestResult(results[0])
                    }
                }
            }
        }
    }

    private fun handleInstructionRequestResult(spokenText : String){
        when(spokenText){
            "1","one" -> {
                val textToSpeak = mail_cc_edittext.text.toString()
                if(textToSpeak.isNotEmpty()) ccFragmentViewModel.ccFragmentTextToBeSpoken.value = textToSpeak
                else ccFragmentViewModel.ccFragmentTextToBeSpoken.value = "Mail CC's address is empty."
            }
            "2","to","two","too" -> {
                ccFragmentViewModel.mailCCAddress.value = ""
                ccFragmentViewModel.ccFragmentTextToBeSpoken.value = "Mail CC's address has been cleared."
            }
            "3","three","tree" -> {
                val action = CCFragmentDirections.backNavigation()
                findNavController().navigate(action)
            }
            "4","four","for" -> {
                ccFragmentViewModel.ccFragmentTextToBeSpoken.value = getString(R.string.mail_cc_tts)
            }
            else -> ccFragmentViewModel.ccFragmentTextToBeSpoken.value = getString(R.string.unknown_command)
        }
    }

    private fun handleReadOutRequestResult(spokenText: String) {
        val splittedText = spokenText.split("at")
        val address = splittedText[0].trim().toLowerCase(Locale.US)
        val mailProvider = splittedText[1].trim().toLowerCase(Locale.US)
        val ccAddress = "$address@$mailProvider"
        ccFragmentViewModel.mailCCAddress.value = ccAddress
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
