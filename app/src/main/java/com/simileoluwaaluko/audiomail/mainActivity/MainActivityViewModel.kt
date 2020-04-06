package com.simileoluwaaluko.audiomail.mainActivity

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.simileoluwaaluko.audiomail.R

/**
 * Created by The Awesome Simileoluwa Aluko on 2020-03-28.
 */
class MainActivityViewModel(application: Application) : AndroidViewModel(application) {
    val homeFragmentTextToBeSpoken = MutableLiveData(application.getString(R.string.home_tts))
    val recipientFragmentTextToBeSpoken = MutableLiveData(application.getString(R.string.mail_recipient_tts))
    val mailBodyFragmentTextToBeSpoken = MutableLiveData(application.getString(R.string.mail_body_tts))
    val subjectFragmentTextToBeSpoken = MutableLiveData(application.getString(R.string.mail_subject_tts))
    val summarySendFragmentTextToBeSpoken = MutableLiveData(application.getString(R.string.mail_summarysend_tts))


    val mailRecipientAddress = MutableLiveData<String>()
    val mailSubject = MutableLiveData<String>()
    val mailBody = MutableLiveData("")

}