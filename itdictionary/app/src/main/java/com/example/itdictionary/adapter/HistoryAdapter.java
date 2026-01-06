package com.example.itdictionary.adapter;

import android.content.Context;
import android.text.Html;
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

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    private List<HistoryItem> historyList;
    private OnHistoryItemClickListener listener;

    public interface OnHistoryItemClickListener {
        void onItemClick(HistoryItem item);
        void onDeleteClick(HistoryItem item, int position);
    }


    public HistoryAdapter(List<HistoryItem> historyList, OnHistoryItemClickListener listener) {
        this.historyList = historyList;
        this.listener = listener;
    }


    public static class HistoryViewHolder extends RecyclerView.ViewHolder {
        LinearLayout textLayout;
        TextView textWord;
        TextView textMeaning;
        ImageButton deleteButton;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            textLayout = itemView.findViewById(R.id.layout_history_text);
            textWord = itemView.findViewById(R.id.text_history_word);
            textMeaning = itemView.findViewById(R.id.text_history_meaning);
            deleteButton = itemView.findViewById(R.id.btn_delete_history_item);
        }

        public void bind(final HistoryItem item, final OnHistoryItemClickListener listener) {
            textWord.setText(item.getWord());

            // Hiển thị nghĩa, nếu rỗng thì ẩn đi
            if (item.getMeaning() != null && !item.getMeaning().isEmpty()) {
                textMeaning.setText(item.getMeaning());
                textMeaning.setVisibility(View.VISIBLE);
            } else {
                textMeaning.setVisibility(View.GONE);
            }




            textLayout.setOnClickListener(v -> listener.onItemClick(item));


            deleteButton.setOnClickListener(v -> listener.onDeleteClick(item, getAdapterPosition()));
        }
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history, parent, false);
        return new HistoryViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        HistoryItem item = historyList.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    public void removeItem(int position) {
        historyList.remove(position);
        notifyItemRemoved(position);
    }

    public void updateList(List<HistoryItem> newList) {
        historyList.clear();
        historyList.addAll(newList);
        notifyDataSetChanged();
    }
}