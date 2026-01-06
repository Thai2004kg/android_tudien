package com.example.itdictionary;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatDelegate;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE);
        int themeMode = prefs.getInt(MainActivity.KEY_THEME, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        AppCompatDelegate.setDefaultNightMode(themeMode);

        setContentView(R.layout.activity_home);


        CardView cardSearch = findViewById(R.id.card_search);
        CardView cardFavorites = findViewById(R.id.card_favorites);
        CardView cardTranslate = findViewById(R.id.card_translate);
        CardView cardHistory = findViewById(R.id.card_history);

        cardSearch.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, MainActivity.class);
            intent.putExtra("FRAGMENT_TO_LOAD", "SEARCH");
            startActivity(intent);
        });


        cardFavorites.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, MainActivity.class);
            intent.putExtra("FRAGMENT_TO_LOAD", "FAVORITES");
            startActivity(intent);
        });

        cardTranslate.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, TranslateActivity.class);
            startActivity(intent);
        });

        cardHistory.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, HistoryActivity.class);
            startActivity(intent);
        });
    }
}