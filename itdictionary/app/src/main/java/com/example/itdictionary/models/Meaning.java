package com.example.itdictionary.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Meaning {

    @SerializedName("partOfSpeech")
    private String partOfSpeech;

    @SerializedName("definitions")
    private List<Definition> definitions;

    public String getPartOfSpeech() {
        return partOfSpeech;
    }
    public List<Definition> getDefinitions() {
        return definitions;
    }
}