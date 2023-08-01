package com.example.finalproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class NewsDetailActivity extends AppCompatActivity {

    private TextView detailTitleTextView;
    private TextView detailDescriptionTextView;
    private TextView detailDateTextView;
    private Button openLinkButton;
    private ToggleButton favoriteToggleButton;
    private NewsItem newsItem;
    private SharedPreferences favoritesPrefs;
    private static final String FAVORITES_PREFS_KEY = "favorites_prefs_key";
    private Set<String> favoritesSet;
    private static final int OPEN_LINK_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_detail);

        // Initialize views
        detailTitleTextView = findViewById(R.id.titleTextView);
        detailDescriptionTextView = findViewById(R.id.descriptionTextView);
        detailDateTextView = findViewById(R.id.dateTextView);
        favoriteToggleButton = findViewById(R.id.favoriteToggleButton);

        // Retrieve NewsItems list and favorites from SharedPreferences
        favoritesPrefs = getSharedPreferences(FAVORITES_PREFS_KEY, MODE_PRIVATE);
        favoritesSet = favoritesPrefs.getStringSet("favorites_set", new HashSet<>());

        // Retrieve NewsItem ID and the list of news items from the intent
        Intent intent = getIntent();
        int newsItemId = intent.getIntExtra("news_item_id", -1);
        ArrayList<NewsItem> newsItems = intent.getParcelableArrayListExtra("news_items");

        if (newsItemId != -1 && newsItems != null) {
            // Find the corresponding NewsItem from the newsItems list
            NewsItem selectedNewsItem = findNewsItemById(newsItems, newsItemId);

            if (selectedNewsItem != null) {
                // Update UI with the selected NewsItem details
                updateUI(selectedNewsItem);
            }
        }


        // Set click listener for the open link button
        openLinkButton = findViewById(R.id.openLinkButton);
        openLinkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openLinkInBrowser();
            }
        });

        // Set click listener for the favorite toggle button
        favoriteToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                toggleFavorite();
            }
        });

        // Handle click event for the "Back to First Page" button
        Button backToFirstPageButton = findViewById(R.id.backToFirstPageButton);
        backToFirstPageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Go back to the MainActivity (first page)
                finish();
            }
        });
    }
    // Method to find the corresponding NewsItem based on its ID
    private NewsItem findNewsItemById(ArrayList<NewsItem> newsItems, int newsItemId) {
        for (NewsItem newsItem : newsItems) {
            if (newsItem.getId() == newsItemId) {
                return newsItem;
            }
        }
        return null;
    }
    private void updateUI(NewsItem newsItem) {
        // Update UI with the NewsItem details
        detailTitleTextView.setText(newsItem.getTitle());
        detailDescriptionTextView.setText(newsItem.getDescription());
        detailDateTextView.setText(newsItem.getDate());

        // Check if the NewsItem is in the favorites list and update the toggle button accordingly
        String newsItemId = Integer.toString(newsItem.getId());
        favoriteToggleButton.setChecked(favoritesSet.contains(newsItemId));

        // Set the NewsItem object for further use (e.g., when updating the favorite status)
        this.newsItem = newsItem;
    }

    private void openLinkInBrowser() {
        // Open the link in the browser
        String articleUrl = newsItem.getLink();
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(articleUrl));
        startActivityForResult(browserIntent, OPEN_LINK_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == OPEN_LINK_REQUEST_CODE) {
            // Check if the user returned from the web browser
            if (resultCode == RESULT_OK) {
                // The user has returned from the web browser
                // Add your desired action here, for example, show a toast message
                Toast.makeText(this, "Welcome back!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void toggleFavorite() {
        String newsItemId = Integer.toString(newsItem.getId());
        if (favoriteToggleButton.isChecked()) {
            // Add to favorites
            favoritesSet.add(newsItemId);
            Toast.makeText(this, "Added to favorites", Toast.LENGTH_SHORT).show();
        } else {
            // Remove from favorites
            favoritesSet.remove(newsItemId);
            Toast.makeText(this, "Removed from favorites", Toast.LENGTH_SHORT).show();
        }
        saveFavoritesToPrefs();
    }

    // Method to save favorites list to SharedPreferences
    private void saveFavoritesToPrefs() {
        SharedPreferences.Editor editor = favoritesPrefs.edit();
        editor.putStringSet("favorites_set", favoritesSet);
        editor.apply();
    }
}