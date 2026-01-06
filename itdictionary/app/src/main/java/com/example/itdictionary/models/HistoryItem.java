package com.example.itdictionary.models;

public class HistoryItem {
    private int id;
    private String word;
    private String meaning;
    private String phonetic;
    private String rawJson;


    public HistoryItem(int id, String word, String meaning, String phonetic, String rawJson) {
        this.id = id;
        this.word = word;
        this.meaning = meaning;
        this.phonetic = phonetic;
        this.rawJson = rawJson;
    }


    public int getId() {
        return id;
    }
    public String getWord() {
        return word;
    }
    public String getMeaning() {
        return meaning;
    }
    public String getPhonetic() {
        return phonetic;
    }
    public String getRawJson() { // Đã đổi tên
        return rawJson;
    }
}