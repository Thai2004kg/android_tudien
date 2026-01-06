package com.example.itdictionary.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.Nullable;

import com.example.itdictionary.models.HistoryItem;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {


    public static final String DATABASE_NAME = "dictionary.db";
    public static final int DATABASE_VERSION = 2;

    public static final String TABLE_SAVED_WORDS = "saved_words";
    public static final String COL_ID_SAVED = "id";
    public static final String COL_WORD_SAVED = "word";
    public static final String COL_PHONETIC_SAVED = "phonetic";
    public static final String COL_MEANING_SAVED = "meaning";
    public static final String COL_RAW_JSON_SAVED = "raw_json";

    private static final String CREATE_TABLE_SAVED_WORDS = "CREATE TABLE " + TABLE_SAVED_WORDS + " (" +
            COL_ID_SAVED + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COL_WORD_SAVED + " TEXT UNIQUE, " + // Thêm UNIQUE để tránh trùng lặp
            COL_PHONETIC_SAVED + " TEXT, " +
            COL_MEANING_SAVED + " TEXT, " +
            COL_RAW_JSON_SAVED + " TEXT)";

    public static final String TABLE_HISTORY = "history";
    public static final String COL_ID_HISTORY = "id";
    public static final String COL_WORD_HISTORY = "word";
    public static final String COL_PHONETIC_HISTORY = "phonetic";
    public static final String COL_MEANING_HISTORY = "meaning";
    public static final String COL_RAW_JSON_HISTORY = "raw_json";
    public static final String COL_TIMESTAMP_HISTORY = "timestamp";

    private static final String CREATE_TABLE_HISTORY = "CREATE TABLE " + TABLE_HISTORY + " (" +
            COL_ID_HISTORY + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COL_WORD_HISTORY + " TEXT, " +
            COL_PHONETIC_HISTORY + " TEXT, " +
            COL_MEANING_HISTORY + " TEXT, " +
            COL_RAW_JSON_HISTORY + " TEXT, " +
            COL_TIMESTAMP_HISTORY + " TEXT)";


    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_SAVED_WORDS);
        db.execSQL(CREATE_TABLE_HISTORY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SAVED_WORDS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HISTORY);
        onCreate(db);
    }

    public void addHistory(String word, String phonetic, String meaning, String rawJson) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_HISTORY, COL_WORD_HISTORY + " = ?", new String[]{word});
        ContentValues values = new ContentValues();
        values.put(COL_WORD_HISTORY, word);
        values.put(COL_PHONETIC_HISTORY, phonetic);
        values.put(COL_MEANING_HISTORY, meaning);
        values.put(COL_RAW_JSON_HISTORY, rawJson);
        values.put(COL_TIMESTAMP_HISTORY, String.valueOf(System.currentTimeMillis()));
        db.insert(TABLE_HISTORY, null, values);
        db.close();
    }

    public List<HistoryItem> getHistorySuggestions(String query) {
        List<HistoryItem> historyList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = COL_WORD_HISTORY + " LIKE ?";
        String[] selectionArgs = { query + "%" };
        String orderBy = COL_TIMESTAMP_HISTORY + " DESC";
        String limit = "20";

        Cursor cursor = db.query(TABLE_HISTORY, null, selection, selectionArgs, null, null, orderBy, limit);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID_HISTORY));
                String word = cursor.getString(cursor.getColumnIndexOrThrow(COL_WORD_HISTORY));
                String meaning = cursor.getString(cursor.getColumnIndexOrThrow(COL_MEANING_HISTORY));
                String phonetic = cursor.getString(cursor.getColumnIndexOrThrow(COL_PHONETIC_HISTORY));
                String rawJson = cursor.getString(cursor.getColumnIndexOrThrow(COL_RAW_JSON_HISTORY));

                historyList.add(new HistoryItem(id, word, meaning, phonetic, rawJson));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return historyList;
    }

    public void deleteHistoryItem(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_HISTORY, COL_ID_HISTORY + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    public void addFavorite(String word, String phonetic, String meaning, String rawJson) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_WORD_SAVED, word);
        values.put(COL_PHONETIC_SAVED, phonetic);
        values.put(COL_MEANING_SAVED, meaning);
        values.put(COL_RAW_JSON_SAVED, rawJson);
        db.insert(TABLE_SAVED_WORDS, null, values);
        db.close();
    }


    public void removeFavorite(String word) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_SAVED_WORDS, COL_WORD_SAVED + " = ?", new String[]{word});
        db.close();
    }

    public boolean isFavorite(String word) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_SAVED_WORDS,
                null,
                COL_WORD_SAVED + " = ?",
                new String[]{word},
                null, null, null
        );
        boolean exists = (cursor.getCount() > 0);
        cursor.close();
        db.close();
        return exists;
    }

    public List<HistoryItem> getAllFavorites() {
        List<HistoryItem> favoriteList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String orderBy = COL_WORD_SAVED + " ASC";

        Cursor cursor = db.query(TABLE_SAVED_WORDS, null, null, null, null, null, orderBy);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID_SAVED));
                String word = cursor.getString(cursor.getColumnIndexOrThrow(COL_WORD_SAVED));
                String meaning = cursor.getString(cursor.getColumnIndexOrThrow(COL_MEANING_SAVED));
                String phonetic = cursor.getString(cursor.getColumnIndexOrThrow(COL_PHONETIC_SAVED));
                String rawJson = cursor.getString(cursor.getColumnIndexOrThrow(COL_RAW_JSON_SAVED));

                favoriteList.add(new HistoryItem(id, word, meaning, phonetic, rawJson));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return favoriteList;
    }
}