package com.example.itdictionary;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.text.Html;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

// Import các model
import com.example.itdictionary.models.Definition;
import com.example.itdictionary.models.Meaning;
import com.example.itdictionary.models.Phonetic;
import com.example.itdictionary.models.TranslationResponse;
import com.example.itdictionary.models.WordResponse;
// Import Database
import com.example.itdictionary.database.DatabaseHelper;
// Import Mạng (Retrofit)
import com.example.itdictionary.network.ApiService;
import com.example.itdictionary.network.RetrofitClient;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// Import Retrofit
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DefinitionActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    public static final String EXTRA_WORD = "EXTRA_WORD";
    public static final String EXTRA_PHONETIC = "EXTRA_PHONETIC";
    public static final String EXTRA_MEANING = "EXTRA_MEANING";
    public static final String EXTRA_RAW_JSON = "EXTRA_RAW_JSON";


    private Toolbar toolbar;
    private LinearLayout definitionsContainer;
    private ImageButton btnFavorite;


    private TextToSpeech tts;
    private DatabaseHelper dbHelper;
    private ApiService dictionaryApiService;
    private ApiService translateApiService;
    private SharedPreferences prefs;
    private Gson gson;
    private MediaPlayer mediaPlayer;


    private String wordToSearch;
    private boolean isTtsInitialized = false;
    private WordResponse currentWordData;
    private String currentMainTranslation = "";
    private String currentRawJson = "";
    private String currentPhonetic = "";
    private float baseContentSize = 14;
    private float currentScale = 1.0f;
    private boolean isFavorite = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_definition);

        toolbar = findViewById(R.id.toolbar_definition);
        definitionsContainer = findViewById(R.id.definitions_container);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        dbHelper = new DatabaseHelper(this);
        tts = new TextToSpeech(this, this);
        dictionaryApiService = RetrofitClient.getDictionaryApiService();
        translateApiService = RetrofitClient.getTranslateApiService();
        prefs = getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE);
        gson = new Gson();

        // --- kiểm tra ONLINE/OFFLINE ---
        wordToSearch = getIntent().getStringExtra(EXTRA_WORD);
        String phonetic = getIntent().getStringExtra(EXTRA_PHONETIC);
        String meaning = getIntent().getStringExtra(EXTRA_MEANING);
        String rawJson = getIntent().getStringExtra(EXTRA_RAW_JSON);

        if (wordToSearch == null) {
            finish();
            return;
        }

        getSupportActionBar().setTitle(wordToSearch);
        currentScale = prefs.getFloat(MainActivity.KEY_FONT_SIZE, 1.0f);

        if (rawJson != null) {

            currentPhonetic = phonetic;
            currentMainTranslation = meaning;
            currentRawJson = rawJson;

            Type listType = new TypeToken<ArrayList<WordResponse>>(){}.getType();
            List<WordResponse> responses = gson.fromJson(rawJson, listType);

            if (responses != null && !responses.isEmpty()) {
                currentWordData = responses.get(0);
                populateUi(currentWordData, currentMainTranslation);
            } else {
                displayError("Lỗi đọc dữ liệu offline.");
            }

        } else {
            performSearch(wordToSearch);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void performSearch(String word) {
        definitionsContainer.removeAllViews();
        addTextView(String.format(getString(R.string.searching_for), word), 16, "normal", definitionsContainer);
        searchDictionaryApi(word);
    }

    private void searchDictionaryApi(String word) {
        Call<List<WordResponse>> call = dictionaryApiService.getWordDefinition(word);
        call.enqueue(new Callback<List<WordResponse>>() {
            @Override
            public void onResponse(Call<List<WordResponse>> call, Response<List<WordResponse>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    currentWordData = response.body().get(0);
                    List<WordResponse> rawResponseList = response.body();
                    currentRawJson = gson.toJson(rawResponseList);
                    searchTranslateApi(word, rawResponseList);
                } else {
                    displayError(String.format(getString(R.string.word_not_found), word));
                }
            }
            @Override
            public void onFailure(Call<List<WordResponse>> call, Throwable t) {
                displayError(getString(R.string.network_error));
            }
        });
    }

    private void searchTranslateApi(String wordInEnglish, List<WordResponse> rawResponseList) {
        Call<TranslationResponse> call = translateApiService.getTranslation(wordInEnglish, "en|vi");
        call.enqueue(new Callback<TranslationResponse>() {
            @Override
            public void onResponse(Call<TranslationResponse> call, Response<TranslationResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentMainTranslation = response.body().getResponseData().getTranslatedText();
                } else {
                    currentMainTranslation = "";
                }
                populateUi(currentWordData, currentMainTranslation);
                saveToHistory(currentWordData, currentMainTranslation, rawResponseList);
            }
            @Override
            public void onFailure(Call<TranslationResponse> call, Throwable t) {
                currentMainTranslation = "";
                populateUi(currentWordData, currentMainTranslation);
                saveToHistory(currentWordData, currentMainTranslation, rawResponseList);
            }
        });
    }

    private void displayError(String message) {
        definitionsContainer.removeAllViews();
        addTextView(message, 16, "normal", definitionsContainer);
    }


    private void populateUi(WordResponse data, String mainTranslation) {
        definitionsContainer.removeAllViews();
        if (data == null) return;

        currentScale = prefs.getFloat(MainActivity.KEY_FONT_SIZE, 1.0f);


        addTextView(data.getWord(), 24, "bold", definitionsContainer);
        currentPhonetic = findBestPhonetic(data.getPhonetics());
        if (!currentPhonetic.isEmpty()) {
            addTextView(currentPhonetic, 16, "italic", definitionsContainer);
        }
        if (mainTranslation != null && !mainTranslation.isEmpty()) {
            addTextView(mainTranslation, 18, "bold", definitionsContainer);
        }

        addControlButtons(data.getPhonetics());
        setupFavoriteButtonListener();

        for (Meaning meaning : data.getMeanings()) {
            addTextView("■ " + meaning.getPartOfSpeech(), 18, "bold_html", definitionsContainer);

            int defCount = 1;
            for (Definition def : meaning.getDefinitions()) {
                CardView card = createCardView();
                LinearLayout cardContentLayout = new LinearLayout(this);
                cardContentLayout.setOrientation(LinearLayout.VERTICAL);
                cardContentLayout.setPadding(16, 16, 16, 16);


                String defText = defCount + ". " + def.getDefinition();
                addTextView(defText, baseContentSize, "italic_html", cardContentLayout);


                if (def.getExample() != null && !def.getExample().isEmpty()) {
                    String exText = "<i>Ví dụ: \"" + def.getExample() + "\"</i>";
                    addTextView(exText, baseContentSize, "html", cardContentLayout);
                }


                if (def.getSynonyms() != null && !def.getSynonyms().isEmpty()) {
                    addTextView("<b>      Đồng nghĩa:</b>", baseContentSize, "html", cardContentLayout);
                    ChipGroup chipGroup = new ChipGroup(this);
                    for (String synonym : def.getSynonyms()) {
                        Chip chip = createChip(synonym);
                        chipGroup.addView(chip);
                    }
                    cardContentLayout.addView(chipGroup);
                }

                if (def.getAntonyms() != null && !def.getAntonyms().isEmpty()) {
                    addTextView("<b>      Trái nghĩa:</b>", baseContentSize, "html", cardContentLayout);
                    ChipGroup chipGroup = new ChipGroup(this);
                    for (String antonym : def.getAntonyms()) {
                        Chip chip = createChip(antonym);
                        chipGroup.addView(chip);
                    }
                    cardContentLayout.addView(chipGroup);
                }

                final String plainTextForPopup = def.getDefinition();
                View.OnClickListener listener = v -> showCopyTranslateMenu(plainTextForPopup);
                card.setOnClickListener(listener);
                card.setOnLongClickListener(v -> {
                    listener.onClick(v);
                    return true;
                });

                card.addView(cardContentLayout);
                definitionsContainer.addView(card);

                defCount++;
            }
            addTextView("", 8, "normal", definitionsContainer);
        }
    }

    private CardView createCardView() {
        CardView cardView = new CardView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 8, 0, 8);
        cardView.setLayoutParams(params);
        cardView.setRadius(12f);
        cardView.setCardElevation(4f);
        return cardView;
    }

    private TextView addTextView(String text, float sizeSp, String style, ViewGroup container) {
        TextView textView = new TextView(this);

        if (style.equals("normal")) {
            textView.setText(text);
        } else if (style.equals("bold")) {
            textView.setText(text);
            textView.setTypeface(null, Typeface.BOLD);
        } else if (style.equals("italic")) {
            textView.setText(text);
            textView.setTypeface(null, Typeface.ITALIC);
        } else if (style.equals("html") || style.equals("bold_html") || style.equals("italic_html")) {
            if (style.equals("bold_html")) text = "<b>" + text + "</b>";
            if (style.equals("italic_html")) text = "<i>" + text + "</i>";
            textView.setText(Html.fromHtml(text, Html.FROM_HTML_MODE_COMPACT));
        }

        if (sizeSp == baseContentSize) {
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, baseContentSize * currentScale);
        } else {
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeSp);
        }

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 8, 0, 8);
        textView.setLayoutParams(params);

        container.addView(textView);
        return textView;
    }

    private Chip createChip(String text) {
        Chip chip = new Chip(this);
        chip.setText(text);
        chip.setClickable(true);
        chip.setCheckable(false);

        chip.setOnClickListener(v -> {
            Intent intent = new Intent(this, DefinitionActivity.class);
            intent.putExtra(DefinitionActivity.EXTRA_WORD, text);
            startActivity(intent);
        });
        return chip;
    }

    private void addControlButtons(List<Phonetic> phonetics) {
        LinearLayout buttonLayout = new LinearLayout(this);
        buttonLayout.setOrientation(LinearLayout.HORIZONTAL);
        buttonLayout.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);

        MaterialButton btnUs = new MaterialButton(this, null, com.google.android.material.R.attr.materialButtonOutlinedStyle);
        btnUs.setText("US");
        btnUs.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_speak));
        btnUs.setOnClickListener(v -> speakWord(wordToSearch, Locale.US));
        LinearLayout.LayoutParams paramsUs = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
        );
        paramsUs.setMarginEnd(16);
        btnUs.setLayoutParams(paramsUs);
        buttonLayout.addView(btnUs);

        MaterialButton btnUk = new MaterialButton(this, null, com.google.android.material.R.attr.materialButtonOutlinedStyle);
        btnUk.setText("UK");
        btnUk.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_speak));
        btnUk.setOnClickListener(v -> speakWord(wordToSearch, Locale.UK));
        buttonLayout.addView(btnUk);

        View spacer = new View(this);
        LinearLayout.LayoutParams spacerParams = new LinearLayout.LayoutParams(0, 0, 1.0f);
        spacer.setLayoutParams(spacerParams);
        buttonLayout.addView(spacer);

        btnFavorite = new ImageButton(this);
        int size = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, getResources().getDisplayMetrics());
        btnFavorite.setLayoutParams(new ViewGroup.LayoutParams(size, size));

        TypedValue outValue = new TypedValue();
        getTheme().resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, outValue, true);
        btnFavorite.setBackgroundResource(outValue.resourceId);

        btnFavorite.setContentDescription(getString(R.string.favorite_button_description));
        checkIfFavorite();

        buttonLayout.addView(btnFavorite);
        definitionsContainer.addView(buttonLayout);
    }

    private void checkIfFavorite() {
        if (btnFavorite == null) return;
        isFavorite = dbHelper.isFavorite(wordToSearch);
        if (isFavorite) {
            btnFavorite.setImageResource(R.drawable.ic_favorite);
        } else {
            btnFavorite.setImageResource(R.drawable.ic_favorite_border);
        }
    }


    private void setupFavoriteButtonListener() {
        if (btnFavorite == null) return;

        btnFavorite.setOnClickListener(v -> {
            if (isFavorite) {
                dbHelper.removeFavorite(wordToSearch);
                btnFavorite.setImageResource(R.drawable.ic_favorite_border);
            } else {
                dbHelper.addFavorite(wordToSearch, currentPhonetic, currentMainTranslation, currentRawJson);
                btnFavorite.setImageResource(R.drawable.ic_favorite);
            }
            isFavorite = !isFavorite;
        });
    }

    private void showCopyTranslateMenu(String textToHandle) {
        String previewText = textToHandle;
        if (previewText.length() > 120) {
            previewText = previewText.substring(0, 120) + "...";
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("\"" + previewText + "\"");
        builder.setPositiveButton("Dịch câu này", (dialog, which) -> {
            translateText(textToHandle);
        });
        builder.setNegativeButton("Sao chép", (dialog, which) -> {
            copyToClipboard(textToHandle);
        });
        builder.setNeutralButton("Hủy", (dialog, which) -> {
            dialog.dismiss();
        });
        builder.show();
    }

    private void copyToClipboard(String text) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("definition", text);
        clipboard.setPrimaryClip(clip);
    }

    private void translateText(String text) {
        Intent intent = new Intent(this, TranslateActivity.class);
        intent.putExtra(TranslateActivity.KEY_INCOMING_TEXT, text);
        startActivity(intent);
    }


    private void saveToHistory(WordResponse data, String translatedMeaning, List<WordResponse> rawResponseList) {
        if (data == null || rawResponseList == null) return;

        String word = data.getWord();
        String phonetic = findBestPhonetic(data.getPhonetics());
        String rawJson = gson.toJson(rawResponseList);

        try {
            dbHelper.addHistory(word, phonetic, translatedMeaning, rawJson);
        } catch (Exception e) {
            Log.e("Database", "Lỗi khi lưu lịch sử: " + e.getMessage());
        }
    }

    private String findBestPhonetic(List<Phonetic> phonetics) {
        if (phonetics == null || phonetics.isEmpty()) return "";
        for (Phonetic p : phonetics) {
            if (p.getText() != null && !p.getText().isEmpty()) return p.getText();
        }
        return "";
    }



    private void speakWord(String word, Locale locale) {
        if (word == null || word.isEmpty()) {
            return;
        }


        String audioUrl = findBestAudio(locale);

        if (audioUrl != null) {

            playAudioFromUrl(audioUrl, locale);
        } else {
            playTextToSpeech(word, locale);
        }
    }

    private String findBestAudio(Locale locale) {
        if (currentWordData == null || currentWordData.getPhonetics() == null) {
            return null;
        }
        String regionTag = locale.getCountry().toLowerCase(Locale.ROOT);

        for (Phonetic phonetic : currentWordData.getPhonetics()) {
            String audio = phonetic.getAudio();
            if (audio != null && !audio.isEmpty()) {
                if (audio.contains("-" + regionTag + ".mp3")) {
                    return audio;
                }
            }
        }

        for (Phonetic phonetic : currentWordData.getPhonetics()) {
            String audio = phonetic.getAudio();
            if (audio != null && !audio.isEmpty()) {
                return audio;
            }
        }
        return null;
    }

    private void playAudioFromUrl(String url, Locale fallbackLocale) {
        releaseMediaPlayer();
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioAttributes(
                    new AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
            );
            mediaPlayer.setDataSource(url);
            mediaPlayer.setOnCompletionListener(mp -> releaseMediaPlayer());
            mediaPlayer.setOnPreparedListener(mp -> mp.start());
            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                Log.e("MediaPlayer", "Lỗi phát audio, mã lỗi: " + what);
                releaseMediaPlayer();
                playTextToSpeech(wordToSearch, fallbackLocale);
                return true;
            });
            mediaPlayer.prepareAsync();

        } catch (Exception e) {
            Log.e("MediaPlayer", "Lỗi setDataSource: " + e.getMessage());
            releaseMediaPlayer();
            playTextToSpeech(wordToSearch, fallbackLocale);
        }
    }

    private void playTextToSpeech(String word, Locale locale) {
        if (!isTtsInitialized) {
            Log.e("TTS", "TTS chưa khởi tạo.");
            return;
        }

        int result = tts.setLanguage(locale);
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            Log.e("TTS", "Ngôn ngữ " + locale.getDisplayLanguage() + " không được hỗ trợ.");
            int usResult = tts.setLanguage(Locale.US);
            if (usResult == TextToSpeech.LANG_MISSING_DATA || usResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "Giọng US cũng không hỗ trợ.");
                return;
            }
        }
        tts.speak(word, TextToSpeech.QUEUE_FLUSH, null, null);
    }

    private void releaseMediaPlayer() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    // --- Các hàm Callback của TTS ---

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            isTtsInitialized = true;
            int result = tts.setLanguage(Locale.US);
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "Ngôn ngữ US mặc định không được hỗ trợ");
            }
        } else {
            isTtsInitialized = false;
            Log.e("TTS", "Khởi tạo TextToSpeech thất bại!");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        releaseMediaPlayer();
    }
}
