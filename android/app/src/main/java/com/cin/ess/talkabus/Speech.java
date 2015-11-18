package com.cin.ess.talkabus;

import java.util.HashMap;
import java.util.Locale;

import android.content.Context;
import android.content.Intent;
import android.speech.tts.TextToSpeech;
import android.widget.Toast;

public class Speech
{
    TextToSpeech t1;
    Locale myLocale;
    Context context;
    public Speech(Context context)
    {
        this.context = context;
        myLocale = new Locale("pt", "BR");
        t1 = new TextToSpeech(this.context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    t1.setLanguage(myLocale);
                }
            }
        });

    }


    public void speak(String text)
    {
        t1.speak(text, TextToSpeech.QUEUE_ADD, null);

    }



}