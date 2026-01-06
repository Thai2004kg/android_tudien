package com.example.itdictionary.models;

import com.google.gson.annotations.SerializedName;

public class Phonetic {

    @SerializedName("text")
    private String text;

    @SerializedName("audio")
    private String audio;


    public String getText() {
        return text;
    }
    public String getAudio() {
        return audio;
    }
}