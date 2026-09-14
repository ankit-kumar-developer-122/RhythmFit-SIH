package com.example.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

/**
 * TextToSpeech engine wrapper for hands-free audio guidance during micro-break routines.
 */
class TtsAudioGuide(context: Context) : TextToSpeech.OnInitListener {

    private val tag = "TtsAudioGuide"
    private var tts: TextToSpeech? = null
    private var isInitialized = false

    init {
        tts = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.US)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.w(tag, "TTS language not supported or missing data")
            } else {
                isInitialized = true
                tts?.setSpeechRate(0.95f) // Relaxed, clear cadence
                tts?.setPitch(1.0f)
            }
        } else {
            Log.e(tag, "TTS Initialization failed with status: $status")
        }
    }

    fun speak(text: String, queueMode: Int = TextToSpeech.QUEUE_FLUSH) {
        if (isInitialized && text.isNotBlank()) {
            tts?.speak(text, queueMode, null, "RhythmFit_Cue_${System.currentTimeMillis()}")
        }
    }

    fun stop() {
        if (isInitialized) {
            tts?.stop()
        }
    }

    fun release() {
        try {
            tts?.stop()
            tts?.shutdown()
            tts = null
            isInitialized = false
        } catch (e: Exception) {
            Log.e(tag, "Error shutting down TTS: ${e.message}")
        }
    }
}
