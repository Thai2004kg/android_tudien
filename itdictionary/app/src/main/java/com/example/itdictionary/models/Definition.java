package com.example.itdictionary.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Definition {

    @SerializedName("definition")
    private String definition;

    @SerializedName("example")
    private String example;

    @SerializedName("synonyms")
    private List<String> synonyms;

    @SerializedName("antonyms")
    private List<String> antonyms;


    public String getDefinition() {
        return definition;
    }
    public String getExample() {
        return example;
    }

    public List<String> getSynonyms() {
        return synonyms;
    }
    public List<String> getAntonyms() {
        return antonyms;
    }
}