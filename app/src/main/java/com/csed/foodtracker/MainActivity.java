package com.csed.foodtracker;

import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private DatabaseHelper mDBHelper;
    private SQLiteDatabase mDb;
    private ArrayList<Recipe> recipeList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        mDBHelper = new DatabaseHelper(this);

        try {
            mDBHelper.updateDataBase();
        } catch (IOException mIOException) {
            throw new Error("UnableToUpdateDatabase");
        }

        try {
            mDb = mDBHelper.getWritableDatabase();

        } catch (SQLException mSQLException) {
            throw mSQLException;
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(),AddRecipeActivity.class));

            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //Generate a recipe list from the current contents of the database
        createRecipeList();

        RecyclerView recipeListView = findViewById(R.id.recipe_recyclerview);
        RecipeAdapter recipeAdapter = new RecipeAdapter(recipeList);
        recipeListView.setAdapter(recipeAdapter);
        recipeListView.setLayoutManager(new LinearLayoutManager(this));
        recipeListView.addItemDecoration(new DividerItemDecoration(getApplicationContext(),
                DividerItemDecoration.VERTICAL));

    }

    private void createRecipeList(){
        if (recipeList.isEmpty()){
            Cursor recipeTable = mDb.rawQuery("SELECT Recipes.recipe_id, Recipes.name, Recipes.description, Recipes.image," +
                    "Recipes.prep_time, Recipes.calories, Recipes.url FROM Recipes",null);

            recipeTable.moveToPosition(0);
            while (recipeTable.getPosition() < recipeTable.getCount()){
                Recipe recipe = new Recipe();

                int id = recipeTable.getInt(recipeTable.getColumnIndex("recipe_id"));
                String name = recipeTable.getString(recipeTable.getColumnIndex("name"));
                String description = recipeTable.getString(recipeTable.getColumnIndex("description"));
                String image = recipeTable.getString(recipeTable.getColumnIndex("image"));
                String prepTime = recipeTable.getString(recipeTable.getColumnIndex("prep_time"));
                int calories = recipeTable.getInt(recipeTable.getColumnIndex("calories"));
                String url = recipeTable.getString(recipeTable.getColumnIndex("url"));

                recipe.setId(id);
                recipe.setName(name);
                recipe.setDescription(description);
                recipe.setImage(image);
                recipe.setPrepTime(prepTime);
                recipe.setCalories(calories);
                recipe.setUrl(url);

                recipeList.add(recipe);
                recipeTable.moveToNext();

            }

            recipeTable.close();
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
