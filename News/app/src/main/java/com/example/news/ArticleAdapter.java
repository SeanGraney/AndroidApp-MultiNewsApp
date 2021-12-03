package com.example.news;

import android.content.Intent;
import android.net.Uri;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Locale;

public class ArticleAdapter extends RecyclerView.Adapter<ArticleViewHolder> {

    private final MainActivity mainActivity;
    private final ArrayList<Article> articleList;
    private Picasso picasso;
    private final String TAG = "Article Adapter ";

    public ArticleAdapter(MainActivity mainActivity, ArrayList<Article> articleList) {
        this.mainActivity = mainActivity;
        this.articleList = articleList;
        picasso=Picasso.get();
    }

    @NonNull
    @Override
    public ArticleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ArticleViewHolder(
                LayoutInflater.from(parent.getContext()).
                        inflate(R.layout.news_entry, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ArticleViewHolder holder, int position) {
        Article a = articleList.get(position);

        holder.headline.setText(a.getTitle());
        holder.date.setText(a.getPublishedAt());
        holder.author.setText(a.getAuthor());
        loadRemoteImage(a.getUrlToImage(), holder.image);
        holder.articleText.setText(a.getDescription());
        holder.articleText.setMovementMethod(new ScrollingMovementMethod());
        holder.count.setText( String.valueOf(position+1)+" of "+articleList.size());

        //open internet
        holder.headline.setOnClickListener(v -> clickEl(a.getUrl()));
        holder.image.setOnClickListener(v -> clickEl(a.getUrl()));
        holder.articleText.setOnClickListener(v -> clickEl(a.getUrl()));
    }

    @Override
    public int getItemCount() {
        return articleList.size();
    }

    private void clickEl(String url) {
        Uri fullUrl = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, fullUrl);
        mainActivity.startActivity(intent);
    }

    private void loadRemoteImage(String imageURL, ImageView imageView) {
        // Needs gradle  implementation 'com.squareup.picasso:picasso:2.71828'

        long millisS = System.currentTimeMillis();

        picasso.load(imageURL)
                    .error(R.drawable.noimage)
                    .placeholder(R.drawable.loading)
                .into(imageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        long millisE = System.currentTimeMillis();
                        Log.d(TAG, "loadRemoteImage: Duration: " +
                                (millisE-millisS) + " ms");
                    }

                    @Override
                    public void onError(Exception e) {
                        long millisE = System.currentTimeMillis();
                        Log.d(TAG, "loadRemoteImage: Duration: " +
                                (millisE-millisS) + " ms");
                    }
                });

        picasso.load(imageURL)
                .error(R.drawable.noimage)
                .placeholder(R.drawable.loading)
                .into(imageView);


    }
}
