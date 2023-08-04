package com.example.finalproject;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class FavoriteNewsFragment extends Fragment {
    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    public static final String ARG_FAVORITE_NEWS_ITEMS = "favorite-news-items"; // Change to public

    // TODO: Customize parameters
    private int mColumnCount = 1;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public FavoriteNewsFragment() {
    }

    // TODO: Customize parameter initialization
    public static FavoriteNewsFragment newInstance(int columnCount, ArrayList<NewsItem> favoriteNewsItems) {
        FavoriteNewsFragment fragment = new FavoriteNewsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorite_news_item, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }

            // Retrieve the list of favorite news items from SharedPreferences
            ArrayList<NewsItem> favoriteNewsItems = getFavoriteNewsItems();

            // Pass the list of favorite news items to the adapter
            recyclerView.setAdapter(new FavoriteNewsAdapter(context, favoriteNewsItems));
        }
        return view;
    }
    private ArrayList<NewsItem> getFavoriteNewsItems() {
        // Ensure the fragment is attached to an activity and has a valid context
        Context context = requireContext();

        // Retrieve the favorites from SharedPreferences
        SharedPreferences favoritesPrefs = context.getSharedPreferences(NewsDetailActivity.FAVORITES_PREFS_KEY, Context.MODE_PRIVATE);
        Set<String> favoritesSet = favoritesPrefs.getStringSet("favorites_set", new HashSet<>());

        // Retrieve the list of all news items from the fragment arguments
        ArrayList<NewsItem> allNewsItems = getArguments().getParcelableArrayList(ARG_FAVORITE_NEWS_ITEMS);

        // Create a new list to store the favorite news items
        ArrayList<NewsItem> favoriteNewsItems = new ArrayList<>();

        // Check if allNewsItems and favoritesSet are not null
        if (allNewsItems != null && favoritesSet != null) {
            // Loop through the list of all news items
            for (NewsItem newsItem : allNewsItems) {
                if (newsItem != null) {
                    String newsItemId = Integer.toString(newsItem.getId());
                    // Check if the news item ID is present in the favorites set
                    if (favoritesSet.contains(newsItemId)) {
                        // Add the news item to the list of favorite news items
                        favoriteNewsItems.add(newsItem);
                    }
                }
            }
        }

        return favoriteNewsItems;
    }}
