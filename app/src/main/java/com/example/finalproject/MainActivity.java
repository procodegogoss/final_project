package com.example.finalproject;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private ListView newsListView;
    private NewsAdapter newsAdapter;
    private ArrayList<NewsItem> newsItems;
    private ArrayList<NewsItem> favoriteNewsItems;
    private SharedPreferences favoritesPrefs;
    private static final String FAVORITES_PREFS_KEY = "favorites_prefs_key";
    private Set<String> favoritesSet;
    private int newsItemIdCounter = 0; // Incremental counter for NewsItem IDs

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        newsListView = findViewById(R.id.newsListView);
        newsItems = new ArrayList<>();
        newsAdapter = new NewsAdapter(this, newsItems);
        newsListView.setAdapter(newsAdapter);
        favoriteNewsItems = new ArrayList<>();

        // Fetch news headlines from the RSS feed
        FetchNewsTask fetchNewsTask = new FetchNewsTask();
        fetchNewsTask.execute();

        // Set a click listener for the ListView items
        newsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Get the selected NewsItem
                NewsItem selectedNewsItem = newsItems.get(position);

                // Create an Intent to open the NewsDetailActivity
                Intent intent = new Intent(MainActivity.this, NewsDetailActivity.class);
                // Pass the selected NewsItem ID and the list of news items to the NewsDetailActivity
                intent.putExtra("news_item_id", selectedNewsItem.getId());
                intent.putParcelableArrayListExtra("news_items", newsItems);
                startActivity(intent);
                System.out.println(selectedNewsItem.getId());
            }
        });

        favoritesPrefs = getSharedPreferences(FAVORITES_PREFS_KEY, MODE_PRIVATE);
        favoritesSet = favoritesPrefs.getStringSet("favorites_set", new HashSet<>());
    }

    // Method to add/remove items to/from the favorites list
    private void saveFavoritesToPrefs() {
        SharedPreferences.Editor editor = favoritesPrefs.edit();
        editor.putStringSet("favorites_set", favoritesSet);
        editor.apply();
    }

    // Method to add/remove items to/from the favorites list
    public void toggleFavorite(NewsItem newsItem) {
        String newsItemId = Integer.toString(newsItem.getId());
        if (favoritesSet.contains(newsItemId)) {
            // Remove from favorites
            favoritesSet.remove(newsItemId);
            newsItem.setFavorite(false);
            Toast.makeText(this, "Removed from favorites", Toast.LENGTH_SHORT).show();
        } else {
            // Add to favorites
            favoritesSet.add(newsItemId);
            newsItem.setFavorite(true);
            Toast.makeText(this, "Added to favorites", Toast.LENGTH_SHORT).show();
        }
        saveFavoritesToPrefs();
    }


    private class FetchNewsTask extends AsyncTask<Void, Void, ArrayList<NewsItem>> {

        @Override
        protected ArrayList<NewsItem> doInBackground(Void... voids) {
            ArrayList<NewsItem> result = new ArrayList<>();

            try {
                URL url = new URL("https://feeds.bbci.co.uk/news/world/us_and_canada/rss.xml");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream inputStream = connection.getInputStream();
                    result = parseXml(inputStream);
                    inputStream.close();
                } else {
                    // Handle HTTP error (e.g., show error message or retry)
                }

                connection.disconnect();
            } catch (IOException | XmlPullParserException e) {
                e.printStackTrace();
                // Handle network or parsing errors
            }
            for (NewsItem newsItem : result) {
                newsItem.setId(newsItemIdCounter++); // Assign a unique ID and increment the counter
            }

            return result;
        }

        @Override
        protected void onPostExecute(ArrayList<NewsItem> result) {
            // Update the UI with the fetched news items
            if (result != null && !result.isEmpty()) {
                newsItems.clear();
                newsItems.addAll(result);
                newsAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(MainActivity.this, "Failed to fetch news data", Toast.LENGTH_SHORT).show();
            }

            // Hide progress bar or loading indicator if applicable
        }

        private ArrayList<NewsItem> parseXml(InputStream inputStream) throws XmlPullParserException, IOException {
            ArrayList<NewsItem> newsList = new ArrayList<>();
            XmlPullParserFactory parserFactory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = parserFactory.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(inputStream, null);

            int eventType = parser.getEventType();
            NewsItem currentNewsItem = null;

            while (eventType != XmlPullParser.END_DOCUMENT) {
                String tagName = parser.getName();

                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        if ("item".equals(tagName)) {
                            currentNewsItem = new NewsItem("", "", "", "");
                        } else if (currentNewsItem != null) {
                            if ("title".equals(tagName)) {
                                currentNewsItem.setTitle(parser.nextText());
                            } else if ("description".equals(tagName)) {
                                currentNewsItem.setDescription(parser.nextText());
                            } else if ("pubDate".equals(tagName)) {
                                currentNewsItem.setDate(parser.nextText());
                            } else if ("link".equals(tagName)) {
                                currentNewsItem.setLink(parser.nextText());
                            }
                        }
                        break;

                    case XmlPullParser.END_TAG:
                        if ("item".equals(tagName) && currentNewsItem != null) {
                            newsList.add(currentNewsItem);
                            currentNewsItem = null;
                        }
                        break;
                }

                eventType = parser.next();
            }

            return newsList;
        }
    }
}