package com.example.itdictionary;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.itdictionary.adapter.HistoryAdapter;
import com.example.itdictionary.database.DatabaseHelper;
import com.example.itdictionary.models.HistoryItem;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;


public class HistoryActivity extends AppCompatActivity implements HistoryAdapter.OnHistoryItemClickListener {

    private Toolbar toolbar;
    private TextInputEditText editTextSearchHistory;
    private RecyclerView recyclerViewHistory;

    private DatabaseHelper dbHelper;
    private HistoryAdapter historyAdapter;
    private List<HistoryItem> historyList;

    private boolean isSearching = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);


        toolbar = findViewById(R.id.toolbar_history);
        editTextSearchHistory = findViewById(R.id.edit_text_search_history);
        recyclerViewHistory = findViewById(R.id.recycler_view_history);


        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }


        dbHelper = new DatabaseHelper(this);
        historyList = new ArrayList<>();
        historyAdapter = new HistoryAdapter(historyList, this);


        recyclerViewHistory.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewHistory.setAdapter(historyAdapter);

        editTextSearchHistory.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                loadHistory(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        isSearching = false;

        loadHistory(editTextSearchHistory.getText().toString());
    }

    private void loadHistory(String query) {
        List<HistoryItem> newList = dbHelper.getHistorySuggestions(query);
        historyAdapter.updateList(newList);

    }

    @Override
    public void onItemClick(HistoryItem item) {
        if (isSearching) return;
        isSearching = true;

        Intent intent = new Intent(this, DefinitionActivity.class);
        intent.putExtra(DefinitionActivity.EXTRA_WORD, item.getWord());
        intent.putExtra(DefinitionActivity.EXTRA_PHONETIC, item.getPhonetic());
        intent.putExtra(DefinitionActivity.EXTRA_MEANING, item.getMeaning());
        intent.putExtra(DefinitionActivity.EXTRA_RAW_JSON, item.getRawJson());
        startActivity(intent);
    }

    @Override
    public void onDeleteClick(HistoryItem item, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa lịch sử")
                .setMessage("Bạn có chắc muốn xóa từ '" + item.getWord() + "' khỏi lịch sử?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    dbHelper.deleteHistoryItem(item.getId());
                    historyAdapter.removeItem(position);

                })
                .setNegativeButton("Hủy", null)
                .show();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}