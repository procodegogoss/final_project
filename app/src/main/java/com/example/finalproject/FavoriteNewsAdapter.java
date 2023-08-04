package com.example.finalproject;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class FavoriteNewsAdapter extends RecyclerView.Adapter<FavoriteNewsAdapter.ViewHolder> {

    private  ArrayList<NewsItem> favoriteNewsItems;
    private  Context context;

    public FavoriteNewsAdapter(Context context, ArrayList<NewsItem> favoriteNewsItems) {
        this.context = context;
        this.favoriteNewsItems = favoriteNewsItems;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_favorite_news_item, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NewsItem newsItem = favoriteNewsItems.get(position);
        holder.titleTextView.setText(newsItem.getTitle());
        holder.descriptionTextView.setText(newsItem.getDescription());
        holder.dateTextView.setText(newsItem.getDate());

        // Handle other views or click events as needed
    }


    @Override
    public int getItemCount() {
        return favoriteNewsItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
       TextView titleTextView;
        TextView descriptionTextView;
       TextView dateTextView;

        public ViewHolder(View view) {
            super(view);
            titleTextView = view.findViewById(R.id.titleTextView);
            descriptionTextView = view.findViewById(R.id.descriptionTextView);
            dateTextView = view.findViewById(R.id.dateTextView);
        }
    }
}