package com.example.itdictionary.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.itdictionary.R;
import com.example.itdictionary.models.HistoryItem;

import java.util.List;


public class FavoritesAdapter extends RecyclerView.Adapter<FavoritesAdapter.FavoriteViewHolder> {

    private List<HistoryItem> favoriteList;
    private OnFavoriteItemClickListener listener;

    public interface OnFavoriteItemClickListener {
        void onItemClick(HistoryItem item);
        void onDeleteClick(HistoryItem item, int position);
    }

    public FavoritesAdapter(List<HistoryItem> favoriteList, OnFavoriteItemClickListener listener) {
        this.favoriteList = favoriteList;
        this.listener = listener;
    }

    /**
     * ViewHolder (Ánh xạ từ file item_favorite.xml)
     */
    public static class FavoriteViewHolder extends RecyclerView.ViewHolder {
        LinearLayout textLayout;
        TextView textWord;
        TextView textMeaning;
        ImageButton deleteButton;
        TextView textNumber;

        public FavoriteViewHolder(@NonNull View itemView) {
            super(itemView);
            textLayout = itemView.findViewById(R.id.layout_favorite_text);
            textWord = itemView.findViewById(R.id.text_favorite_word);
            textMeaning = itemView.findViewById(R.id.text_favorite_meaning);
            deleteButton = itemView.findViewById(R.id.btn_delete_favorite_item);
            textNumber = itemView.findViewById(R.id.text_favorite_number);
        }

        public void bind(final HistoryItem item, final OnFavoriteItemClickListener listener) {
            textWord.setText(item.getWord());

            // Gán số thứ tự (position + 1)
            int position = getAdapterPosition();
            textNumber.setText((position + 1) + ".");

            if (item.getMeaning() != null && !item.getMeaning().isEmpty()) {
                textMeaning.setText(item.getMeaning());
                textMeaning.setVisibility(View.VISIBLE);
            } else {
                textMeaning.setVisibility(View.GONE);
            }

            textLayout.setOnClickListener(v -> listener.onItemClick(item));
            deleteButton.setOnClickListener(v -> listener.onDeleteClick(item, position));
        }
    }

    @NonNull
    @Override
    public FavoriteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_favorite, parent, false);
        return new FavoriteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoriteViewHolder holder, int position) {
        HistoryItem item = favoriteList.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return favoriteList.size();
    }


    public void removeItem(int position) {
        favoriteList.remove(position);
        notifyDataSetChanged(); // Cập nhật lại toàn bộ list
    }

    public void updateList(List<HistoryItem> newList) {
        favoriteList.clear();
        favoriteList.addAll(newList);
        notifyDataSetChanged();
    }
}