package com.example.itdictionary.network;

import com.example.itdictionary.models.TranslationResponse;
import com.example.itdictionary.models.WordResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    // 1. Dùng cho DictionaryAPI.dev
    @GET("api/v2/entries/en/{word}")
    Call<List<WordResponse>> getWordDefinition(@Path("word") String word);


    // 2. Dùng cho MyMemory API (Dịch)
    @GET("get")
    Call<TranslationResponse> getTranslation(
            @Query("q") String query,
            @Query("langpair") String langPair
    );
}