package com.example.finalproject;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
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
    private EditText newsEditText;
    private Button addButton;
    private ListView newsListView;
    private NewsAdapter newsAdapter;
    private ArrayList<NewsItem> newsItems;
    private ArrayList<NewsItem> favoriteNewsItems;
    private SharedPreferences favoritesPrefs;
    private static final String FAVORITES_PREFS_KEY = "favorites_prefs_key";
    private Set<String> favoritesSet;
    private int newsItemIdCounter = 0; // Incremental counter for NewsItem IDs
    private ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Find the ProgressBar by its ID
        progressBar = findViewById(R.id.progressBar);
        newsListView = findViewById(R.id.newsListView);
        newsItems = new ArrayList<>();
        newsAdapter = new NewsAdapter(this, newsItems);
        newsListView.setAdapter(newsAdapter);
        favoriteNewsItems = new ArrayList<>();
        // Find the EditText and Button by their IDs
        newsEditText = findViewById(R.id.newsEditText);
        addButton = findViewById(R.id.addButton);
        favoritesPrefs = getSharedPreferences(AppConstants.FAVORITES_PREFS_KEY, MODE_PRIVATE);
        favoritesSet = favoritesPrefs.getStringSet("favorites_set", new HashSet<>());

        // Set click listener for the Button
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addNewsHeadline(); // Call method to add the entered headline
            }
        });

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

    private void addNewsHeadline() {
        String headline = newsEditText.getText().toString().trim();
        if (!headline.isEmpty()) {
            // Call the method to add the headline as a NewsItem
            addNewsItem(headline);

            // Clear the EditText after adding
            newsEditText.setText("");

            // Show a Toast message to indicate that the headline is added
            Toast.makeText(this, "Headline added: " + headline, Toast.LENGTH_SHORT).show();
        }
    }

    private void addNewsItem(String headline) {
        // Create a new NewsItem with the entered headline (You need to implement this constructor)
        NewsItem newsItem = new NewsItem(newsItemIdCounter, headline, "", "", "");
        // Increment the newsItemIdCounter for the next NewsItem
        newsItemIdCounter++;
        // Add the NewsItem to the newsItems list
        newsItems.add(newsItem);
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
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_help) {
            showHelpAlertDialog();
            return true;
        } else {
            // Add other menu item cases if needed
            return super.onOptionsItemSelected(item);
        }
    }

    private void showHelpAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Help");
        builder.setMessage(getString(R.string.action_help));
        builder.setPositiveButton("OK", null); // You can add a positive button if needed

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private class FetchNewsTask extends AsyncTask<Void, Void, ArrayList<NewsItem>> {
        @Override
        protected void onPreExecute() {
            // Show the progress bar before starting the task
            progressBar.setVisibility(View.VISIBLE);
        }

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

            // Hide the progress bar after fetching news items
            progressBar.setVisibility(View.GONE);
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