package com.example.itdictionary.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class WordResponse {

    @SerializedName("word")
    private String word;

    @SerializedName("phonetic")
    private String phonetic;

    @SerializedName("phonetics")
    private List<Phonetic> phonetics;

    @SerializedName("meanings")
    private List<Meaning> meanings;


    public String getWord() {
        return word;
    }
    public String getPhonetic() {
        return phonetic;
    }
    public List<Phonetic> getPhonetics() {
        return phonetics;
    }
    public List<Meaning> getMeanings() {
        return meanings;
    }
}