package com.example.itdictionary.models;

import com.google.gson.annotations.SerializedName;

public class TranslationData {

    @SerializedName("translatedText")
    private String translatedText;

    public String getTranslatedText() {
        return translatedText;
    }
}