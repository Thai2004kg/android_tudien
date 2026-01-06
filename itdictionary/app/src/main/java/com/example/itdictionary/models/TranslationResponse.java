package com.example.itdictionary.models;

import com.google.gson.annotations.SerializedName;

public class TranslationResponse {

    @SerializedName("responseData")
    private TranslationData responseData;


    public TranslationData getResponseData() {
        return responseData;
    }
}