package com.simileoluwaaluko.audiomail


import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import java.util.*

/**
 * A simple [Fragment] subclass.
 */
class HomeFragment : Fragment() {
    lateinit var textToSpeech: TextToSpeech

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        textToSpeech = TextToSpeech(context, TextToSpeech.OnInitListener { status ->
                if(status != TextToSpeech.ERROR){
                    val result = textToSpeech.setLanguage(Locale.US)
                    if(result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED){
                        Log.e("simi_TTS", "$result language not supported")
                    }
                    textToSpeech.speak("Hello Stephanie", TextToSpeech.QUEUE_FLUSH, null)
                }else{
                    Log.e("Simi_TTS", "$status initialisation failed")
                }
            })
    }

    override fun onDestroy() {
        if(textToSpeech != null){
            textToSpeech.stop()
            textToSpeech.shutdown()
        }
        super.onDestroy()
    }
}
