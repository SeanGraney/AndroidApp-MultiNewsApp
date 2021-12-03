package com.example.news;

import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.RequiresApi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

public class NewsLoader implements Runnable {

    private static final String TAG = "NewsLoadeRunnable";
    private final MainActivity mainActivity;

    private static final String SourcesURL = "https://newsapi.org/v2/sources?";
    private static final String ArticleURL = "https://newsapi.org/v2/top-headlines?";
    private static final String APIKey = "52a86202037646668dd424f42c3a95c5";

    private static String jsonCountry;
    private static String jsonLanguage;
    private static String urlSwitch = "source";
    private static Source[] sr;
    private static Article[] ar;

    NewsLoader(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }


    public void run() {
        setJsonLanguage();
        setJsonCountry();
        if(urlSwitch.equals("source")) {
            apiCall(buildUrl(SourcesURL, null));
        }else {
            apiCall(buildUrl(ArticleURL, mainActivity.getCurrentSource()));
        }
    }

    public String buildUrl(String s, String str){ ;
        String urlToUse="";

        if (s.equals(SourcesURL)){
            Uri.Builder buildURL = Uri.parse(SourcesURL).buildUpon();

            buildURL.appendQueryParameter("apiKey", APIKey);
            urlToUse = buildURL.build().toString();
            Log.d(TAG, "doInBackground: " + urlToUse);

        }
        else if (s.equals(ArticleURL)){
            Uri.Builder buildURL = Uri.parse(ArticleURL).buildUpon();
            String currentSource = str;


            buildURL.appendQueryParameter("sources", currentSource);
            buildURL.appendQueryParameter("apiKey", APIKey);
            urlToUse = buildURL.build().toString();
            try {
                urlToUse = URLDecoder.decode(urlToUse, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return null;
            }
            Log.d(TAG, "doInBackground: " + urlToUse);
        }
        return urlToUse;
    }

    public void apiCall(String urlToUse){
        StringBuilder sb = new StringBuilder();
        try {
            URL url = new URL(urlToUse);

            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.addRequestProperty("User-Agent", "");
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            connection.setRequestProperty("Accept", "application/json");
            connection.connect();

            if (connection.getResponseCode() != HttpsURLConnection.HTTP_OK) {
                InputStream is = connection.getErrorStream();
                BufferedReader reader = new BufferedReader((new InputStreamReader(is)));

                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append('\n');
                }
                handleError(sb.toString());
                return;
            }


            InputStream is = connection.getInputStream();
            BufferedReader reader = new BufferedReader((new InputStreamReader(is)));

            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }

            Log.d(TAG, "doInBackground: " + sb.toString());

        } catch (Exception e) {
            Log.e(TAG, "doInBackground: ", e);
            handleResults(null);
            return;
        }
        handleResults(sb.toString());
    }


    public void handleError(String s) {
        String msg = "Error: ";
        try {
            JSONObject jObjMain = new JSONObject(s);
            msg += jObjMain.getString("message");

        } catch (JSONException e) {
            msg += e.getMessage();
        }

        String finalMsg = String.format("%s (%s)", msg);
        mainActivity.runOnUiThread(() -> mainActivity.handleError(finalMsg));
    }


    public void handleResults(final String jsonString) {

        if (!urlSwitch.equals("articles")) {
            // sources section
            try {
                //Sources
                JSONObject jObjMain = new JSONObject(jsonString);
                JSONArray sources = jObjMain.getJSONArray("sources");

                sr = new Source[sources.length()];
                String[] articleURL = new String[sources.length()];
                String articleURLString;
                urlSwitch = "articles";

                for (int i = 0; i < sources.length(); i++) {
                    Source s = parseSource(sources.getJSONObject(i));
                    sr[i] = s;
                    articleURL[i] = s.getId();
                    Log.d(TAG, s.toString());
                }

//                //article url building
//                articleURLString = TextUtils.join(",", articleURL);
//                Log.d("Source Api Call String: ", articleURLString);
//
//
//                apiCall(buildUrl(ArticleURL ,articleURL));
//
//                //article pairing
//                JSONObject jObjMain2 = new JSONObject(urlSwitch);
//                JSONArray articles = jObjMain2.getJSONArray("articles");
//
//
//                ar = new Article[articles.length()];
//
//                for (int i = 0; i < articles.length(); i++) {
//                    JSONObject article = articles.getJSONObject(i);
//                    JSONObject asource = article.getJSONObject("source");
//                    Article a = parseArticle(articles.getJSONObject(i), asource);
//                    Log.d(TAG, a.toString());
//                    ar[i] = a;
//                }
//
//                pairArticleAndSources();
                mainActivity.runOnUiThread(() -> mainActivity.updateData(sr));

            } catch (Exception e) {
                e.printStackTrace();
            }
        }else {
            try {
                //article pairing
                JSONObject jObjMain2 = new JSONObject(jsonString);
                JSONArray articles = jObjMain2.getJSONArray("articles");


                ar = new Article[articles.length()];

                for (int i = 0; i < articles.length(); i++) {
                    JSONObject article = articles.getJSONObject(i);
                    JSONObject asource = article.getJSONObject("source");
                    Article a = parseArticle(articles.getJSONObject(i), asource);
                    Log.d(TAG, a.toString());
                    ar[i] = a;
                }

                sr[mainActivity.getCurrentSourceIdx()].addArticles(ar);
                mainActivity.runOnUiThread(() -> mainActivity.updateData(sr));


            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }


    private Source parseSource(JSONObject s) {

        try {

            String id;
            String name;
            String category;
            String language;
            String country;

            id = s.getString("id");
            name = s.getString("name");
            category = s.getString("category");
            language = s.getString("language");
            country = s.getString("country");

            Source source = new Source(id, name, category, language, country, jsonLanguage, jsonCountry);
            return source;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    private Article parseArticle(JSONObject s, JSONObject source) {

        try {
            String id;
            String author;
            String title;
            String description;
            String url;
            String urlToImage;
            String publishedAt;

            id= source.getString("id");
            author = s.getString("author");
            title = s.getString("title");
            description = s.getString("description");
            url = s.getString("url");
            urlToImage = s.getString("urlToImage");
            publishedAt = s.getString("publishedAt");

            Article article = new Article(id, author, title, description, url, urlToImage, publishedAt);
            return article;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


//    public static void pairArticleAndSources(){
//        ArrayList<Article> tempAr = new ArrayList<>();
//
//        try {
//            for (int i = 0; i < sr.length; i++) {
//                Log.d(TAG, "loopoing");
//                for (int j = 0; j < ar.length; j++) {
//                    Log.d(TAG, "Inner Looping");
//                    Log.d(TAG, sr[i].getId() + " " + ar[j].getId());
//
//                    if (sr[i].getId().equals(ar[j].getId())) {
//                        tempAr.add(ar[j]);
//                    }
//                    sr[i].addArticles(tempAr);
//                    Log.d(TAG, "Source: " + sr[i] + " " + "Articles: " + tempAr.toString());
//                    tempAr.clear();
//                }
//            }
//        } catch (Exception e){
//            e.printStackTrace();
//        }
//    }


    public void setJsonCountry() {
        Utils j = new Utils(mainActivity,"country_codes.json");
        NewsLoader.jsonCountry = j.getJsonFromAssets();
    }

    public void setJsonLanguage() {
        Utils ja = new Utils(mainActivity,"language_codes.json");
        NewsLoader.jsonLanguage = ja.getJsonFromAssets();
    }
}
