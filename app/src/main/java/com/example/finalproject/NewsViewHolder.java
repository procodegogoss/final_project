package com.example.finalproject;

import android.view.View;
import android.widget.TextView;

public class NewsViewHolder {
    private TextView titleTextView;
    private TextView descriptionTextView;
    private TextView dateTextView;

    public NewsViewHolder(View itemView) {
        titleTextView = itemView.findViewById(R.id.titleTextView);
        descriptionTextView = itemView.findViewById(R.id.descriptionTextView);
        dateTextView = itemView.findViewById(R.id.dateTextView);
    }

    public void bind(NewsItem newsItem) {
        titleTextView.setText(newsItem.getTitle());
        descriptionTextView.setText(newsItem.getDescription());
        dateTextView.setText(newsItem.getDate());
    }
}
