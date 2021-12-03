package com.example.news;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

public class Source implements Serializable {

    private String id;
    private String name;
    private String category;
    private String language;
    private String country;
    private final ArrayList<Article> articles = new ArrayList<>();

    public Source(String id, String name, String category, String language, String country, String languageJson, String countryJson) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.language = setLanguage(language.toUpperCase(Locale.ROOT), languageJson);
        this.country = setCountry(country.toUpperCase(Locale.ROOT), countryJson);
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public String getLanguage() { return language; }
    public String getCountry() { return country; }

    public ArrayList<Article> getArticles() {
        return articles;
    }

    public void addArticles(Article[] ar){
        articles.addAll(Arrays.asList(ar));
    }


    public String setCountry(String code, String jsonString){
        JSONObject jObjMain = null;
        try {
            jObjMain = new JSONObject(jsonString);
            JSONArray countries = jObjMain.getJSONArray("countries");

            for(int i=0; i<countries.length(); i++){
                JSONObject obj = countries.getJSONObject(i);
                if(obj.getString("code").equals(code)){
                    return obj.getString("name");

                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "None";
    }

    public String setLanguage(String code, String jsonString){
        JSONObject jObjMain = null;
        try {
            jObjMain = new JSONObject(jsonString);
            JSONArray countries = jObjMain.getJSONArray("languages");

            for(int i=0; i<countries.length(); i++){
                JSONObject obj = countries.getJSONObject(i);
                if(obj.getString("code").equals(code)){
                    return obj.getString("name");
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "None";
    }


    @Override
    public String toString() {
        String articleStr="";
        for(int i=0; i<articles.size(); i++){
            Article a = articles.get(i);
            articleStr=articleStr+a.getId()+" ";
        }
        return "Source{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", category='" + category + '\'' +
                ", language='" + language + '\'' +
                ", country='" + country + '\'' +
                ", articles=" + articleStr +
                '}';
    }
}
