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
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.simileoluwaaluko.audiomail.fragments.HomeFragmentDirections
import com.simileoluwaaluko.audiomail.mainActivity.MainActivityViewModel
import com.simileoluwaaluko.audiomail.R
import com.simileoluwaaluko.audiomail.mainActivity.MainActivity
import kotlinx.android.synthetic.main.fragment_home.*
import org.jetbrains.anko.support.v4.toast
import java.util.*

/**
 * A simple [Fragment] subclass.
 */
class HomeFragment : Fragment(), TextToSpeech.OnInitListener, View.OnClickListener {
    lateinit var homeFragmentViewModel : MainActivityViewModel
    lateinit var textToSpeech: TextToSpeech
    private val speechToTextRequestCode = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (activity as MainActivity).supportActionBar?.title = getString(R.string.welcome_)
        homeFragmentViewModel = ViewModelProvider(this)[MainActivityViewModel::class.java]
        commands_btn.setOnClickListener(this)
        setUpViewModel()
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
                                val action =
                                    HomeFragmentDirections.recipientNavigation()
                                findNavController().navigate(action)
                            }
                            "2","to","two","too" -> {
                                val action =
                                    HomeFragmentDirections.ccNavigation()
                                findNavController().navigate(action)
                            }
                            "3","three","tree" -> {
                                val action =
                                    HomeFragmentDirections.mailBodyNavigation()
                                findNavController().navigate(action)
                            }
                            "4","four","for" -> {
                                val action =
                                    HomeFragmentDirections.summarySendNavigation()
                                findNavController().navigate(action)
                            }
                            "5","five" -> homeFragmentViewModel.homeFragmentTextToBeSpoken.value = getString(
                                R.string.home_tts
                            )
                            else -> {
                                homeFragmentViewModel.homeFragmentTextToBeSpoken.value = getString(
                                    R.string.unknown_command
                                )
                            }
                        }
                    }
                }
            }
        }
    }


    private fun callSpeechToText(){
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())

        if(intent.resolveActivity(activity!!.packageManager) != null){
            startActivityForResult(intent, speechToTextRequestCode)
        }else toast("Your device doesn't support speech input").show()
    }

    override fun onInit(status: Int) {
        if(status != TextToSpeech.ERROR){
            val result = textToSpeech.setLanguage(Locale.US)
            if(result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED){
                Log.e(tag, "$result language not supported")
            }else {
                val textToSpeak = homeFragmentViewModel.homeFragmentTextToBeSpoken.value
                if (textToSpeak != null) textToSpeech.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null, null)
            }
        }else{
            Log.e(tag, "$status initialisation failed")
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

    override fun onClick(v: View?) {
        when(v?.id){
            commands_btn.id -> {
                callSpeechToText()
            }
        }
    }

    private fun setUpViewModel(){
        homeFragmentViewModel.homeFragmentTextToBeSpoken.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            textToSpeech = TextToSpeech(context, this)
        })
    }

}
