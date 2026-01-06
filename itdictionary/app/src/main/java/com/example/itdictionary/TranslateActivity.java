package com.example.itdictionary;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.itdictionary.models.TranslationResponse;
import com.example.itdictionary.network.ApiService;
import com.example.itdictionary.network.RetrofitClient;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TranslateActivity extends AppCompatActivity {

    public static final String KEY_INCOMING_TEXT = "INCOMING_TEXT";
    public static final String EXTRA_IS_FROM_SEARCH = "EXTRA_IS_FROM_SEARCH";

    private Toolbar toolbar;
    private TextInputEditText editTextInput;
    private Button btnTranslate;
    private TextView textViewResult;
    private Button btnGoToDefinition;

    private ApiService translateApiService;

    private boolean isFromSearch = false;
    private String lastTranslatedWord = "";
    private String lastSourceLang = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_translate);

        // --- 1. Ánh xạ Views ---
        toolbar = findViewById(R.id.toolbar_translate);
        editTextInput = findViewById(R.id.edit_text_translate_input);
        btnTranslate = findViewById(R.id.btn_translate);
        textViewResult = findViewById(R.id.text_view_translate_result);
        btnGoToDefinition = findViewById(R.id.btn_go_to_definition);

        // --- 2. Cài đặt Toolbar (Có nút "Back" <-) ---
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // --- 3. Khởi tạo API Service ---
        translateApiService = RetrofitClient.getTranslateApiService();

        // --- 4. Nhận Dữ liệu (Text và "Cờ") ---
        String incomingText = getIntent().getStringExtra(KEY_INCOMING_TEXT);
        isFromSearch = getIntent().getBooleanExtra(EXTRA_IS_FROM_SEARCH, false);

        if (incomingText != null && !incomingText.isEmpty()) {
            editTextInput.setText(incomingText);
            performTranslation(incomingText);
        }

        // --- 5. Gắn Listener cho Nút Dịch ---
        btnTranslate.setOnClickListener(v -> {
            String textToTranslate = editTextInput.getText().toString();
            // (Chúng ta sẽ trim() ở trong hàm performTranslation)
            performTranslation(textToTranslate);
        });

        // --- 6. Gắn Listener cho Nút "Xem chi tiết" ---
        btnGoToDefinition.setOnClickListener(v -> {
            Intent intent = new Intent(this, DefinitionActivity.class);
            intent.putExtra(DefinitionActivity.EXTRA_WORD, lastTranslatedWord);
            startActivity(intent);
        });
    }

    /**
     * Hàm chính: Gọi API và Kiểm tra mạng
     */
    private void performTranslation(String text) {

        // ==========================================================
        // SỬA LỖI Ở ĐÂY: Cắt bỏ khoảng trắng thừa
        // ==========================================================
        text = text.trim();

        // Kiểm tra xem sau khi cắt có còn rỗng không
        if (text.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập văn bản", Toast.LENGTH_SHORT).show();
            return;
        }

        btnGoToDefinition.setVisibility(View.GONE);

        if (!isNetworkAvailable()) {
            textViewResult.setText("Lỗi: Không có kết nối mạng. (Tra cứu offline không hoạt động)");
            btnTranslate.setEnabled(false);
            return;
        }

        btnTranslate.setEnabled(true);
        textViewResult.setText("Đang dịch...");

        lastSourceLang = "en";
        if (containsVietnamese(text)) {
            lastSourceLang = "vi";
        }
        String langPair = lastSourceLang + "|" + (lastSourceLang.equals("vi") ? "en" : "vi");

        // Gửi text đã được "trim()" đi
        Call<TranslationResponse> call = translateApiService.getTranslation(text, langPair);

        call.enqueue(new Callback<TranslationResponse>() {
            @Override
            public void onResponse(Call<TranslationResponse> call, Response<TranslationResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String translatedText = response.body().getResponseData().getTranslatedText();
                    textViewResult.setText(translatedText);

                    lastTranslatedWord = translatedText;

                    checkAndShowGoToDefinition(translatedText);

                } else {
                    textViewResult.setText("Lỗi: Không thể dịch được.");
                }
            }
            @Override
            public void onFailure(Call<TranslationResponse> call, Throwable t) {
                textViewResult.setText("Lỗi: " + t.getMessage());
            }
        });
    }

    /**
     * HÀM MỚI: Kiểm tra và hiển thị nút "Xem chi tiết"
     */
    private void checkAndShowGoToDefinition(String translatedText) {
        if (isFromSearch && lastSourceLang.equals("vi") && isSingleWord(translatedText)) {
            btnGoToDefinition.setText("Xem chi tiết từ '" + translatedText + "' ➔");
            btnGoToDefinition.setVisibility(View.VISIBLE);
        } else {
            btnGoToDefinition.setVisibility(View.GONE);
        }
    }

    /**
     * Helper: Kiểm tra 1 từ đơn
     */
    private boolean isSingleWord(String text) {
        return text != null && !text.trim().contains(" ");
    }

    /**
     * Helper: Kiểm tra ký tự tiếng Việt
     */
    private boolean containsVietnamese(String text) {
        return text.matches(".*[áàảãạăắằẳẵặâấầẩẫậđéèẻẽẹêếềểễệíìỉĩịóòỏõọôốồổỗộơớờởỡợúùủũụưứừửữựýỳỷỹỵ].*");
    }

    /**
     * Helper: Kiểm tra Internet
     */
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) return false;
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /**
     * Xử lý khi người dùng bấm nút Back (<-) trên Toolbar
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}