package com.example.itdictionary.network;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    // --- 1. Cho DictionaryAPI.dev ---
    private static final String BASE_URL_DICTIONARY = "https://api.dictionaryapi.dev/";
    private static Retrofit retrofitDictionary = null;

    // --- 2. Cho MyMemory API (Dịch) ---
    private static final String BASE_URL_TRANSLATE = "https://api.mymemory.translated.net/";
    private static Retrofit retrofitTranslate = null;


    public static ApiService getDictionaryApiService() {
        if (retrofitDictionary == null) {
            retrofitDictionary = new Retrofit.Builder()
                    .baseUrl(BASE_URL_DICTIONARY)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofitDictionary.create(ApiService.class);
    }


    public static ApiService getTranslateApiService() {
        if (retrofitTranslate == null) {
            retrofitTranslate = new Retrofit.Builder()
                    .baseUrl(BASE_URL_TRANSLATE)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofitTranslate.create(ApiService.class);
    }
}