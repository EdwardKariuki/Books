package com.example.books;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

public class BookListActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {
    private ProgressBar mProgressBar;
    private RecyclerView rvBooks;
    URL bookUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_list);
        mProgressBar = (ProgressBar)findViewById(R.id.pb_loading);
        rvBooks = (RecyclerView)findViewById(R.id.rv_books);
        Intent intent = getIntent();
        String query = intent.getStringExtra("query");

        try {
            if (query==null || query.isEmpty()) {
                bookUrl = ApiUtil.buildURL("cooking");
            }else{
                bookUrl = new URL(query);
            }
            new BooksQueryTask().execute(bookUrl);
        }
        catch (Exception e){
            Log.d("error",e.getMessage());
        }

        //create the layoutManager for the books (linear in this case, scrolling vertically
        LinearLayoutManager booksLayoutManager = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        rvBooks.setLayoutManager(booksLayoutManager);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.book_list_menu,menu);
        final MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView)searchItem.getActionView();
        searchView.setOnQueryTextListener(this);

        //recent searches

        ArrayList<String>recentList = SpUtil.getQueryList(getApplicationContext());
        int itemNum = recentList.size();
        MenuItem recentMenu;
        for (int i = 0; i<itemNum; i++){
            recentMenu = menu.add(Menu.NONE, i, Menu.NONE, recentList.get(i));
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Handle item selection

        switch (item.getItemId()){
            case R.id.action_advanced_search:
                Intent intent = new Intent(this,SearchActivity.class);
                startActivity(intent);
                return true;
            default:
                int position = item.getItemId() + 1;
                String preferenceName = SpUtil.QUERY + String.valueOf(position);
                String query = SpUtil.getPreferenceString(getApplicationContext(), preferenceName);
                String prefParams[] = query.split("\\,");
                String queryParams[] = new String[4];
                for (int i=0; i<prefParams.length; i++){
                    queryParams[i] = prefParams[i];
                }
                URL bookUrl = ApiUtil.buildURL(
                        (queryParams[0]==null)?"":queryParams[0],
                        (queryParams[1]==null)?"":queryParams[1],
                        (queryParams[2]==null)?"":queryParams[2],
                        (queryParams[3]==null)?"":queryParams[3]
                );

                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        try {
            URL bookUrl = ApiUtil.buildURL(query);
            new BooksQueryTask().execute(bookUrl);

        }catch (Exception e){
            Log.d("error",e.getMessage());
        }
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    public class BooksQueryTask extends AsyncTask<URL, Void, String>{
        @Override
        protected String doInBackground(URL... urls) {
            URL searchURL = urls[0];
            String result = null;
            try {
                result = ApiUtil.getJson(searchURL);
            }
            catch (IOException e){
                Log.e("Error",e.getMessage());
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            TextView tvError = (TextView)findViewById(R.id.tv_error);
            mProgressBar.setVisibility(View.INVISIBLE);
            if(result == null){
                rvBooks.setVisibility(View.INVISIBLE);
                tvError.setVisibility(View.VISIBLE);
            }
            else{
                rvBooks.setVisibility(View.VISIBLE);
                tvError.setVisibility(View.INVISIBLE);
                ArrayList<Book>books = ApiUtil.getBooksFromJson(result);
                String resultString = "";

                BooksAdapter adapter = new BooksAdapter(books);
                rvBooks.setAdapter(adapter);
            }

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressBar.setVisibility(View.VISIBLE);
        }
    }
}