package com.example.itdictionary;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.itdictionary.adapter.FavoritesAdapter;
import com.example.itdictionary.database.DatabaseHelper;
import com.example.itdictionary.models.HistoryItem;

import java.util.ArrayList;
import java.util.List;


public class FavoritesFragment extends Fragment implements FavoritesAdapter.OnFavoriteItemClickListener {

    private RecyclerView recyclerViewFavorites;
    private TextView textViewEmpty;
    private DatabaseHelper dbHelper;
    private FavoritesAdapter adapter;
    private List<HistoryItem> favoriteList;

    private boolean isSearching = false;

    public FavoritesFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_favorites, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerViewFavorites = view.findViewById(R.id.recycler_view_favorites);
        textViewEmpty = view.findViewById(R.id.text_view_empty);

        dbHelper = new DatabaseHelper(getContext());
        favoriteList = new ArrayList<>();

        adapter = new FavoritesAdapter(favoriteList, this);

        recyclerViewFavorites.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewFavorites.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        isSearching = false;
        loadFavorites();
    }


    private void loadFavorites() {
        if (dbHelper == null || adapter == null) return;

        List<HistoryItem> newList = dbHelper.getAllFavorites();

        if (newList.isEmpty()) {
            textViewEmpty.setVisibility(View.VISIBLE);
            recyclerViewFavorites.setVisibility(View.GONE);
        } else {
            textViewEmpty.setVisibility(View.GONE);
            recyclerViewFavorites.setVisibility(View.VISIBLE);
        }

        adapter.updateList(newList);
    }


    @Override
    public void onItemClick(HistoryItem item) {
        if (isSearching) return;
        isSearching = true;

        Context context = getActivity();
        if (context == null) return;

        Intent intent = new Intent(context, DefinitionActivity.class);
        intent.putExtra(DefinitionActivity.EXTRA_WORD, item.getWord());
        intent.putExtra(DefinitionActivity.EXTRA_PHONETIC, item.getPhonetic());
        intent.putExtra(DefinitionActivity.EXTRA_MEANING, item.getMeaning());
        intent.putExtra(DefinitionActivity.EXTRA_RAW_JSON, item.getRawJson());
        startActivity(intent);
    }

    @Override
    public void onDeleteClick(HistoryItem item, int position) {
        if (getContext() == null) return;

        new AlertDialog.Builder(getContext())
                .setTitle("Xóa Yêu thích")
                .setMessage("Bạn có chắc muốn xóa từ '" + item.getWord() + "' khỏi danh sách yêu thích?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    dbHelper.removeFavorite(item.getWord());
                    adapter.removeItem(position);

                    if (adapter.getItemCount() == 0) {
                        textViewEmpty.setVisibility(View.VISIBLE);
                        recyclerViewFavorites.setVisibility(View.GONE);
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}