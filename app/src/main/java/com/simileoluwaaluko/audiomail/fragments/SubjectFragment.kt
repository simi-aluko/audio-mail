package com.simileoluwaaluko.audiomail.fragments


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
import com.simileoluwaaluko.audiomail.mainActivity.MainActivityViewModel
import com.simileoluwaaluko.audiomail.R
import com.simileoluwaaluko.audiomail.mainActivity.MainActivity
import kotlinx.android.synthetic.main.fragment_mail_subject.*
import org.jetbrains.anko.support.v4.toast
import java.util.*

/**
 * A simple [Fragment] subclass.
 */
class SubjectFragment : Fragment(), View.OnClickListener, TextToSpeech.OnInitListener {

    private val subjectFragmentViewModel: MainActivityViewModel by activityViewModels()
    lateinit var textToSpeech: TextToSpeech
    private val instructionRequest = 1
    private val readOutRequest = 2


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_mail_subject, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (activity as MainActivity).supportActionBar?.title = getString(R.string.mail_subject)
        subject_read_out_button.setOnClickListener(this)
        subject_instruction_button.setOnClickListener(this)
        setUpLiveData()
    }

    private fun setUpLiveData(){
        subjectFragmentViewModel.subjectFragmentTextToBeSpoken.observe(viewLifecycleOwner, Observer {
            textToSpeech = TextToSpeech(context, this)
        })

        subjectFragmentViewModel.mailSubject.observe(viewLifecycleOwner, Observer {
            mail_subject_edittext.setText(it)
        })
    }

    override fun onClick(v: View?) {
        when(v?.id){
            subject_read_out_button.id -> {
                callSpeechToText(readOutRequest)
            }

            subject_instruction_button.id -> {
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
                val textToSpeak = subjectFragmentViewModel.subjectFragmentTextToBeSpoken.value
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
                val textToSpeak = mail_subject_edittext.text.toString()
                if(textToSpeak.isNotEmpty()) subjectFragmentViewModel.subjectFragmentTextToBeSpoken.value = textToSpeak
                else subjectFragmentViewModel.subjectFragmentTextToBeSpoken.value = "Mail subject is empty."
            }
            "2","to","two","too" -> {
                subjectFragmentViewModel.mailSubject.value = ""
                subjectFragmentViewModel.subjectFragmentTextToBeSpoken.value = "Mail subject has been cleared."
            }
            "3","three","tree" -> {
                val action =
                    SubjectFragmentDirections.nextNavigation()
                findNavController().navigate(action)
            }
            "4","four","for" -> {
                subjectFragmentViewModel.subjectFragmentTextToBeSpoken.value = getString(
                    R.string.mail_subject_tts
                )
            }
            else -> subjectFragmentViewModel.subjectFragmentTextToBeSpoken.value = getString(
                R.string.unknown_command
            )
        }
    }

    private fun handleReadOutRequestResult(spokenText: String) {
        subjectFragmentViewModel.mailSubject.value = spokenText
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
