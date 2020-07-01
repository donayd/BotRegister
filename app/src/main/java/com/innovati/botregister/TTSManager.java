package com.innovati.botregister;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import java.util.Locale;

public class TTSManager {

    private TextToSpeech mTts = null;
    public MutableLiveData<Boolean> isLoaded = new MutableLiveData<>();

    public void init(Context context) {
        try {
            isLoaded.setValue(false);
            mTts = new TextToSpeech(context, onInitListener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private TextToSpeech.OnInitListener onInitListener = new TextToSpeech.OnInitListener() {
        @Override
        public void onInit(int status) {
            if (status == TextToSpeech.SUCCESS) {
                int result = mTts.setLanguage(Locale.getDefault());
                isLoaded.setValue(true);
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("error", "Este Lenguaje no esta permitido");
                }
            } else {
                Log.e("error", "Fallo al Inicilizar!");
            }
        }
    };

    public void shutDown() {
        mTts.shutdown();
    }

    public void addQueue(String text) {
        if (isLoaded.getValue())
            mTts.speak(text, TextToSpeech.QUEUE_ADD, null, null);
        else
            Log.e("error", "TTS Not Initialized");
    }

    public void initQueue(String text) {
        if (isLoaded.getValue())
            mTts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        else
            Log.e("error", "TTS Not Initialized");
    }



}
