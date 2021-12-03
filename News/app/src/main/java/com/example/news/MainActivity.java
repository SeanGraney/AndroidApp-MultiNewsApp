package com.example.news;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager2.widget.ViewPager2;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;

import com.google.android.material.navigation.NavigationView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private TextView textView;
    private Menu menu;

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private String[] items;

    private static final ArrayList<Source> Sources = new ArrayList<>();
    private static ArrayList<Source> CurrentSources = new ArrayList<>();
    private static ArrayList<Article> CurrentArticles = new ArrayList<>();

    private ArticleAdapter articleAdapter;
    private ArrayAdapter<String> arrayAdapter;
    private ViewPager2 viewPager;
    private NewsLoader loaderTaskRunnable;

    private String[] subMenu;
    private Source selectedSource;
    private ArrayList<String> topics = new ArrayList<>();
    private ArrayList<String> countries = new ArrayList<>();
    private ArrayList<String> languages = new ArrayList<>();
    private int topicPointer = 0;
    private int countriesPointer = 0;
    private int languagesPointer = 0;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//      --  set global vars  --
        textView = findViewById(R.id.textView);
        String topics = getString(R.string.topics);
        String countries = getString(R.string.countries);
        String languages = getString(R.string.languages);
        subMenu = new String[]{topics, countries, languages};

//      ------------------------
        loaderTaskRunnable = new NewsLoader(this);
        new Thread(loaderTaskRunnable).start();


        mDrawerLayout = findViewById(R.id.drawer_layout); // <== Important!
        mDrawerList = findViewById(R.id.left_drawer); // <== Important!


        mDrawerList.setOnItemClickListener(   // <== Important!
                (parent, view, position, id) -> {
                    try {
                        selectItem(position);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
        );

        mDrawerToggle = new ActionBarDrawerToggle(   // <== Important!
                this,                /* host Activity */
                mDrawerLayout,             /* DrawerLayout object */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
        );

        if (getSupportActionBar() != null) {  // <== Important!
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        articleAdapter = new ArticleAdapter(this, CurrentArticles);
        viewPager = findViewById(R.id.viewpager);
        viewPager.setAdapter(articleAdapter);

    }

//     ----------------------------------viewPager-----------------------------------------
    @SuppressLint("NotifyDataSetChanged")
    private void selectItem(int position) throws InterruptedException {

        viewPager.setBackground(null);

        selectedSource = CurrentSources.get(position);
        CurrentArticles.clear();
        Thread t = new Thread(loaderTaskRunnable);
        t.start();
        t.join();

        ArrayList<Article> articles = selectedSource.getArticles();

        if (articles.size()==0) {
            Toast.makeText(this,
                    MessageFormat.format("No articles found for {0}", selectedSource),
                    Toast.LENGTH_LONG).show();
            return;
        }
        viewPager.setBackground(getDrawable(R.drawable.whitegb));
        CurrentArticles.addAll(articles);
        articleAdapter.notifyDataSetChanged();
//        textView.setText(String.format(Locale.getDefault(),
//                "You picked %s", items[position]));

        viewPager.setCurrentItem(0);

        for (int  i=0; i<CurrentArticles.size(); i++) {
            Log.d("Article Disection", CurrentArticles.get(i).toString());
        }

        mDrawerLayout.closeDrawer(mDrawerList);

        setTitle(selectedSource.getName());

    }


    // ----------------------------------Drawer-----------------------------------------
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState(); // <== IMPORTANT
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig); // <== IMPORTANT
    }


    public void updateDrawer(){
        // Make sample items for the drawer list
        items = new String[CurrentSources.size()];
        for (int i = 0; i < items.length; i++) {
            items[i] = CurrentSources.get(i).getName();
        }

        mDrawerList.setAdapter(new ArrayAdapter<>(this,   // <== Important!
                R.layout.drawer_list, items));

    }
    // ---------------------------------------------------------------------------------------

    // ----------------------------------Menu-------------------------------------------------
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        //        drawer
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            Log.d(TAG, "onOptionsItemSelected: mDrawerToggle " + item);
            return true;
        }
//        setTitle(item.getTitle());

        //         Menu
        if(item.hasSubMenu()){
            return true;
        }

        //index selected
        int menu1 = item.getGroupId();
        int menu2 = item.getItemId();

        CurrentSources = new ArrayList<>(Sources);
//        topic was selected
        if(menu1==0){
            topicPointer=menu2;
        }
//        country was selected
        else if(menu1==1){
            countriesPointer=menu2;
        }
//        languages was selected
        else if(menu1==2){
            languagesPointer=menu2;
        }

        //filter current sources
        for(int i=0; i<Sources.size(); i++){
            if (topicPointer!=0){
                if(!Sources.get(i).getCategory().equals(topics.get(topicPointer))){
                    CurrentSources.remove(Sources.get(i));
                }
            }
            if (languagesPointer!=0){
                if(!Sources.get(i).getLanguage().equals(languages.get(languagesPointer))){
                    CurrentSources.remove(Sources.get(i));
                }
            }
            if (countriesPointer!=0){
                if(!Sources.get(i).getCountry().equals(countries.get(countriesPointer))){
                    CurrentSources.remove(Sources.get(i));
                }
            }
        }
        updateDrawer();
//        textView.setText(
//                        "Topics: " +topics.get(topicPointer)+"\n"+
//                                "countries: " +countries.get(countriesPointer)+"\n"+
//                                "languages: " +languages.get(languagesPointer)
//        );

        arrayAdapter.notifyDataSetChanged();
        return super.onOptionsItemSelected(item);
    }

    public void makeMenu() {
        menu.clear();
        SubMenu sub;
        ArrayList<ArrayList> men = new ArrayList<>();
        men.add(topics);
        men.add(countries);
        men.add(languages);

        for (int i = 0; i < subMenu.length; i++) {
            sub = menu.addSubMenu(subMenu[i]);
            for (int j = 0; j < men.get(i).size(); j++) {
                sub.add(i, j, j,men.get(i).get(j).toString());
            }
        }
        hideKeyboard();
    }

    public void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (imm == null) return;

        //Find the currently focused view
        View view = getCurrentFocus();

        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null)
            view = new View(this);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    // ---------------------------------------------------------------------------------------

//    Api Data Collection
    public void handleError(String s) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Data Problem")
                .setMessage(s)
                .setPositiveButton("OK", (dialogInterface, i) -> {})
                .create().show();

    }

    public void updateData(Source[] sr){
        Sources.addAll(Arrays.asList(sr));
        Log.d(TAG, String.valueOf(Sources));

        CurrentSources = new ArrayList<>(Sources);
        ArrayList<String> str = new ArrayList<>();
        for (int i=0; i<CurrentSources.size(); i++){
            str.add(CurrentSources.get(i).getId());
        }
        updateDrawer();
        updateArrays();
        makeMenu();

        arrayAdapter = new ArrayAdapter<>(this, R.layout.drawer_list, str);
        mDrawerList.setAdapter(arrayAdapter);
        findViewById(R.id.progressBar).setVisibility(View.GONE);
    }

    //Updates Arrays for menu creation
    public void updateArrays(){
        topics.add("all");
        countries.add("all");
        languages.add("all");

        for(int i=0; i<Sources.size(); i++){
            Source s = Sources.get(i);
            //topics
            if(!topics.contains(s.getCategory())){
                topics.add(s.getCategory());
            }
            //countries
            if(!countries.contains(s.getCountry())){
                countries.add(s.getCountry());
            }
            //languages
            if(!languages.contains(s.getLanguage())){
                languages.add(s.getLanguage());
            }
        }

        Collections.sort(topics, String.CASE_INSENSITIVE_ORDER);
        Collections.sort(countries, String.CASE_INSENSITIVE_ORDER);
        Collections.sort(languages, String.CASE_INSENSITIVE_ORDER);
    }

    public String getCurrentSource(){
        return selectedSource.getId();
    }
    public int getCurrentSourceIdx(){
        return Sources.indexOf(selectedSource);
    }


}