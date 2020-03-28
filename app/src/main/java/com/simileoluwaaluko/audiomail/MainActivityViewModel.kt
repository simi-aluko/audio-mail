package com.simileoluwaaluko.audiomail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * Created by The Awesome Simileoluwa Aluko on 2020-03-28.
 */
class MainActivityViewModel(application: Application) : AndroidViewModel(application) {
    val homeFragmentTextToBeSpoken = MutableLiveData(application.getString(R.string.home_tts))
    val recipientFragmentTextToBeSpoken = MutableLiveData(application.getString(R.string.mail_recipient_tts))
    val mailBodyFragmentTextToBeSpoken = MutableLiveData(application.getString(R.string.mail_body_tts))
    val ccFragmentTextToBeSpoken = MutableLiveData(application.getString(R.string.mail_cc_tts))
    val summarySendFragmentTextToBeSpoken = MutableLiveData(application.getString(R.string.mail_summarysend_tts))


    val mailRecipientAddress = MutableLiveData<String>()
    val mailCCAddress = MutableLiveData<String>()
    val mailBody = MutableLiveData<String>()

}