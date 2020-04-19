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
import com.simileoluwaaluko.audiomail.mainActivity.MainActivityViewModel
import com.simileoluwaaluko.audiomail.R
import com.simileoluwaaluko.audiomail.RegisterActivity
import com.simileoluwaaluko.audiomail.inbox.InboxActivity
import com.simileoluwaaluko.audiomail.mainActivity.MainActivity
import kotlinx.android.synthetic.main.fragment_home.*
import org.jetbrains.anko.support.v4.startActivity
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
        // set title of activity from fragment
        (activity as MainActivity).supportActionBar?.title = getString(R.string.welcome_)

        // initialise viewmodel
        homeFragmentViewModel = ViewModelProvider(this)[MainActivityViewModel::class.java]

        // set on clicklistener to btn
        commands_btn.setOnClickListener(this)

        // set up livedata
        setUpLiveData()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when(requestCode){
            // after the speak dialog goes away, the onActivity result is called. if the request code is this, the code in here runs
            // we are switching between what speech to text to direct what the app does based on the spoken number(command)
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
                            "2","to","two","too" -> startActivity<InboxActivity>()
                            "3","three","tree" -> startActivity<RegisterActivity>()
                            "4","four","for" -> homeFragmentViewModel.homeFragmentTextToBeSpoken.value = getString(R.string.home_tts)
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
        // create the android speech to text intent.
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())

        // making the call to start it
        if(intent.resolveActivity(activity!!.packageManager) != null){
            startActivityForResult(intent, speechToTextRequestCode)
        }else toast("Your device doesn't support speech input").show()
    }

    // this gets run after the TextToSpeech is just initialised. after textToSpeech is initialised,
    // that is when we can call it to convert text To Speech
    // the viewmodel pattern is used. when the value in the livedata is changed. the observers tied to
    // the livedata gets run. thence textToSpeech gets initialised  in the observer code in line 157,
    // following that onInit below gets run to speak the text
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

    // stop and release textToSpeech when activity is in destroyed
    override fun onDestroy() {
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

    // onclick function runs when views that onclicklisteners have been tied to is clicked.
    override fun onClick(v: View?) {
        when(v?.id){
            commands_btn.id -> {
                callSpeechToText()
            }
        }
    }

    // set up observers for livedata. text to speech is initialised when the value of homeFragmentTextToBeSpoken changes.
    private fun setUpLiveData(){
        homeFragmentViewModel.homeFragmentTextToBeSpoken.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            textToSpeech = TextToSpeech(context, this)
        })
    }

}
