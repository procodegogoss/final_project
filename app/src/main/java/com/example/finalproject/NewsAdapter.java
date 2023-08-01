package com.example.finalproject;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;

public class NewsAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<NewsItem> newsItems;

    public NewsAdapter(Context context, ArrayList<NewsItem> newsItems) {
        this.context = context;
        this.newsItems = newsItems;
    }

    @Override
    public int getCount() {
        return newsItems.size();
    }

    @Override
    public Object getItem(int position) {
        return newsItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        NewsViewHolder viewHolder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_item_news, parent, false);
            viewHolder = new NewsViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (NewsViewHolder) convertView.getTag();
        }

        NewsItem newsItem = newsItems.get(position);
        viewHolder.bind(newsItem);

        return convertView;
    }
}
