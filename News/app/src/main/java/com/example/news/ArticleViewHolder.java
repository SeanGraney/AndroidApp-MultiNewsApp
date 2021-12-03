package com.example.news;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ArticleViewHolder extends RecyclerView.ViewHolder {

    TextView headline;
    TextView date;
    TextView author;
    ImageView image;
    TextView articleText;
    TextView count;

    public ArticleViewHolder(@NonNull View itemView) {
        super(itemView);
        headline =itemView.findViewById(R.id.headline);
        date = itemView.findViewById(R.id.date);
        author = itemView.findViewById(R.id.author);
        image = itemView.findViewById(R.id.image);
        articleText = itemView.findViewById(R.id.articleText);
        count = itemView.findViewById(R.id.articleCount);
    }
}
