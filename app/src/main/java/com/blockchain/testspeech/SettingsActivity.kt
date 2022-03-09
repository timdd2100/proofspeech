package com.blockchain.testspeech

import android.Manifest
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceFragmentCompat
import com.blockchain.testspeech.databinding.SettingsActivityBinding
import com.blockchain.testspeech.viewmodel.MainViewModel
import com.markodevcic.peko.PermissionResult
import com.markodevcic.peko.requestPermissionsAsync
import kotlinx.coroutines.launch
import net.gotev.speech.GoogleVoiceTypingDisabledException
import net.gotev.speech.Speech
import net.gotev.speech.SpeechDelegate
import net.gotev.speech.SpeechRecognitionNotAvailable


class SettingsActivity : AppCompatActivity() {
    private lateinit var viewBinding: SettingsActivityBinding

    private val viewModel by lazy { ViewModelProvider(this)[MainViewModel::class.java] }
    private val speechDelegate = object : SpeechDelegate {
        override fun onStartOfSpeech() {
            viewModel.updateMessage("speech recognition is now active")
        }

        override fun onSpeechRmsChanged(value: Float) {
            viewModel.updateMessage("rms is now: $value")
        }

        override fun onSpeechPartialResults(results: List<String>) {
            val str = StringBuilder()
            for (res in results) {
                str.append(res).append(" ")
            }
            viewModel.updateMessage(
                "partial result: " + str.toString().trim { it <= ' ' })
        }

        override fun onSpeechResult(result: String) {
            viewModel.updateMessage("result: $result")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = SettingsActivityBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
        buildUI()
        init()
        initViewModel()
    }

    private fun buildUI() {
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }

    private fun init() {
        lifecycleScope.launchWhenResumed {
            launch {
                val result = requestPermissionsAsync(Manifest.permission.RECORD_AUDIO)
                if (result is PermissionResult.Granted) {
                    // we have contacts permission
                    viewBinding.testButton.setOnClickListener {
                        Speech.init(this@SettingsActivity, packageName)
                        speechSetting()
                    }
                    viewBinding.testButton2.setOnClickListener {
                        Speech.getInstance().shutdown()
                        viewModel.clearMsg()
                    }

                } else {
                    // permission denied
                    viewModel.updateMessage("RECORD_AUDIO Permission Denied")
                }
            }
        }
    }

    private fun initViewModel() {
        viewModel.messageLiveData.observe(this) {
            viewBinding.txtLog.text = it.orEmpty()
        }
    }

    private fun speechSetting() {
        try {
            // you must have android.permission.RECORD_AUDIO granted at this point
            Speech.getInstance().startListening(viewBinding.progress, speechDelegate)
        } catch (exc: SpeechRecognitionNotAvailable) {
            viewModel.updateMessage("Speech recognition is not available on this device!")
            // You can prompt the user if he wants to install Google App to have
            // speech recognition, and then you can simply call:
            //
            // SpeechUtil.redirectUserToGoogleAppOnPlayStore(this);
            //
            // to redirect the user to the Google App page on Play Store
        } catch (exc: GoogleVoiceTypingDisabledException) {
            viewModel.updateMessage("Google voice typing must be enabled!")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Speech.getInstance().shutdown()
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
        }
    }
}