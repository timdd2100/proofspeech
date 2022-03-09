package com.blockchain.testspeech.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {

    var logStr = ""
    val messageLiveData = MutableLiveData<String>()

    fun updateMessage(msg: String) {
        logStr += msg
        logStr += "\n"
        messageLiveData.postValue(logStr)
    }

    fun clearMsg() {
        logStr = ""
        messageLiveData.postValue("")
    }
}