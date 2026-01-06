package com.example.itdictionary;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.itdictionary.adapter.HistoryAdapter;
import com.example.itdictionary.database.DatabaseHelper;
import com.example.itdictionary.models.HistoryItem;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment implements HistoryAdapter.OnHistoryItemClickListener {

    private TextInputEditText editTextSearch;
    private ImageButton btnSearch;
    private RecyclerView recyclerViewSuggestions;
    private TextView labelSuggestions;

    private DatabaseHelper dbHelper;
    private HistoryAdapter historyAdapter;
    private List<HistoryItem> suggestionList;
    private boolean isSearching = false;

    public SearchFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        editTextSearch = view.findViewById(R.id.edit_text_search);
        btnSearch = view.findViewById(R.id.btn_search);
        recyclerViewSuggestions = view.findViewById(R.id.recycler_view_suggestions);
        labelSuggestions = view.findViewById(R.id.label_suggestions);

        dbHelper = new DatabaseHelper(getContext());
        suggestionList = new ArrayList<>();
        historyAdapter = new HistoryAdapter(suggestionList, this);

        recyclerViewSuggestions.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewSuggestions.setAdapter(historyAdapter);

        setupListeners();
    }


    private void setupListeners() {
        btnSearch.setOnClickListener(v -> {
            performSearch();
            hideKeyboard(v);
        });

        editTextSearch.setOnEditorActionListener((v, actionId, event) -> {
            boolean isActionVirtual = (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE);
            boolean isActionPhysical = (event != null && actionId == EditorInfo.IME_ACTION_UNSPECIFIED && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN);

            if (isActionVirtual || isActionPhysical) {
                performSearch();
                hideKeyboard(v);
                return true;
            }
            return false;
        });

        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                loadHistorySuggestions(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        isSearching = false;
        loadHistorySuggestions(editTextSearch.getText().toString());
    }

    /**
     * Tải gợi ý từ Database và cập nhật Adapter
     */
    private void loadHistorySuggestions(String query) {
        if (dbHelper == null) return;
        List<HistoryItem> newList = dbHelper.getHistorySuggestions(query);

        if (historyAdapter != null) {
            historyAdapter.updateList(newList);
        }

        if (newList.isEmpty()) {
            labelSuggestions.setVisibility(View.GONE);
        } else {
            labelSuggestions.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Hàm tra cứu chính
     */
    private void performSearch() {
        if (isSearching) return;
        if (editTextSearch == null) return;
        String query = editTextSearch.getText().toString().trim();
        if (query.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng nhập từ", Toast.LENGTH_SHORT).show();
            return;
        }

        isSearching = true;

        if (containsVietnamese(query)) {
            launchTranslateActivity(query);
        } else {
            launchDefinitionActivityOnline(query);
        }
    }

    /**
     * Helper: Kiểm tra ký tự tiếng Việt
     */
    private boolean containsVietnamese(String text) {
        return text.matches(".*[áàảãạăắằẳẵặâấầẩẫậđéèẻẽẹêếềểễệíìỉĩịóòỏõọôốồổỗộơớờởỡợúùủũụưứừửữựýỳỷỹỵ].*");
    }

    /**
     * Helper: Mở trang Dịch câu
     */
    private void launchTranslateActivity(String text) {
        Context context = getActivity();
        if (context == null) {
            isSearching = false;
            return;
        }
        Intent intent = new Intent(context, TranslateActivity.class);
        intent.putExtra(TranslateActivity.KEY_INCOMING_TEXT, text);
        intent.putExtra(TranslateActivity.EXTRA_IS_FROM_SEARCH, true);
        startActivity(intent);
    }

    /**
     * Mở trang chi tiết (Chế độ ONLINE)
     */
    private void launchDefinitionActivityOnline(String word) {
        Context context = getActivity();
        if (context == null) {
            isSearching = false;
            return;
        }
        Intent intent = new Intent(context, DefinitionActivity.class);
        intent.putExtra(DefinitionActivity.EXTRA_WORD, word);
        startActivity(intent);
    }

    /**
     * Mở trang chi tiết (Chế độ OFFLINE)
     */
    private void launchDefinitionActivityOffline(HistoryItem item) {
        Context context = getActivity();
        if (context == null) {
            isSearching = false;
            return;
        }
        Intent intent = new Intent(context, DefinitionActivity.class);
        intent.putExtra(DefinitionActivity.EXTRA_WORD, item.getWord());
        intent.putExtra(DefinitionActivity.EXTRA_PHONETIC, item.getPhonetic());
        intent.putExtra(DefinitionActivity.EXTRA_MEANING, item.getMeaning());
        intent.putExtra(DefinitionActivity.EXTRA_RAW_JSON, item.getRawJson());
        startActivity(intent);
    }

    /**
     * Kiểm tra Internet
     */
    private boolean isNetworkAvailable() {
        Context context = getContext();
        if (context == null) return false;
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) return false;
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void hideKeyboard(View view) {
        if (view != null && getContext() != null) {
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    // ==========================================================
    // XỬ LÝ SỰ KIỆN TỪ ADAPTER (Đã xóa Toast)
    // ==========================================================
    @Override
    public void onItemClick(HistoryItem item) {
        if (isSearching) return;
        isSearching = true;

        if (isNetworkAvailable()) {
            // (Đã xóa Toast "Đang tra lại (Online)")
            launchDefinitionActivityOnline(item.getWord());
        } else {
            // (Đã xóa Toast "Đang mở (Offline)")
            launchDefinitionActivityOffline(item);
        }
    }

    @Override
    public void onDeleteClick(HistoryItem item, int position) {
        if (getContext() == null) return;

        new AlertDialog.Builder(getContext())
                .setTitle("Xóa lịch sử")
                .setMessage("Bạn có chắc muốn xóa từ '" + item.getWord() + "' khỏi lịch sử?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    dbHelper.deleteHistoryItem(item.getId());
                    historyAdapter.removeItem(position);
                    if (historyAdapter.getItemCount() == 0) {
                        labelSuggestions.setVisibility(View.GONE);
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}