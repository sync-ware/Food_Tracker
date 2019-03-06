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
    private ArrayList<Ingredient> ingredientList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //Initialise Database
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

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        //Sends the list of ingredients user has to the add recipe page
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Intent to transfer from current page to new the add recipe activity
                Intent intent = new Intent(getApplicationContext(),AddRecipeActivity.class);
                //Put ingredient list into Intent
                intent.putExtra("ingredientList",ingredientList);
                //Begin new activity
                startActivity(intent);

            }
        });

        //Generate a recipe list from the current contents of the database
        createRecipeList();

        //Generate an ingredients list from the current contents of the database
        createIngredientList();

        //Draw the UI of the list of recipes, includes clickable event of item in list
        initialiseListUI();

    }

    /**
     * Method that will go through each row in the database and generate new Recipe object which will
     * then be added to the list
     */
    private void createRecipeList(){
        //Stops items in the list from duplication which can occur when relaunching the activity
        if (recipeList.isEmpty()){
            //Select SQL Query that pulls data from database and stores it in cursor object
            Cursor recipeTable = mDb.rawQuery("SELECT Recipes.recipe_id, Recipes.name, Recipes.description, Recipes.image," +
                    "Recipes.prep_time, Recipes.calories, Recipes.url FROM Recipes",null);

            //Start at first row
            recipeTable.moveToPosition(0);
            //Keep looping until you reach the last row
            while (recipeTable.getPosition() < recipeTable.getCount()){
                Recipe recipe = new Recipe();

                //Retrieve data from each column
                int id = recipeTable.getInt(recipeTable.getColumnIndex("recipe_id"));
                String name = recipeTable.getString(recipeTable.getColumnIndex("name"));
                String description = recipeTable.getString(recipeTable.getColumnIndex("description"));
                String image = recipeTable.getString(recipeTable.getColumnIndex("image"));
                String prepTime = recipeTable.getString(recipeTable.getColumnIndex("prep_time"));
                int calories = recipeTable.getInt(recipeTable.getColumnIndex("calories"));
                String url = recipeTable.getString(recipeTable.getColumnIndex("url"));

                //Set recipe attributes with data from the database
                recipe.setId(id);
                recipe.setName(name);
                recipe.setDescription(description);
                recipe.setImage(image);
                recipe.setPrepTime(prepTime);
                recipe.setCalories(calories);
                recipe.setUrl(url);

                //Add recipe to the list
                recipeList.add(recipe);
                //Next row in table
                recipeTable.moveToNext();

            }

            recipeTable.close();
        }
    }

    /**
     * Refer to createRecipeList() Method for understanding. Works in exactly the same way except we
     * are now creating ingredients objects
     */
    private void createIngredientList(){
        if (ingredientList.isEmpty()){
            Cursor ingredientTable = mDb.rawQuery("SELECT Ingredients.ing_id, Ingredients.name, Ingredients.best_before," +
                    "Ingredients.num FROM Ingredients", null);

            ingredientTable.moveToPosition(0);
            while (ingredientTable.getPosition() < ingredientTable.getCount()){
                Ingredient ingredient = new Ingredient();

                int id = ingredientTable.getInt(ingredientTable.getColumnIndex("ing_id"));
                String name = ingredientTable.getString(ingredientTable.getColumnIndex("name"));
                String bestBefore = ingredientTable.getString(ingredientTable.getColumnIndex("best_before"));
                String num = ingredientTable.getString(ingredientTable.getColumnIndex("num"));

                ingredient.setId(id);
                ingredient.setName(name);
                ingredient.setBestBefore(bestBefore);
                ingredient.setNumber(num);

                ingredientList.add(ingredient);
                ingredientTable.moveToNext();

            }

            ingredientTable.close();
        }
    }

    /**
     * Generate List UI
     */
    private void initialiseListUI(){
        //Initialise RecyclerView; This is basically the list itself
        RecyclerView recipeListView = findViewById(R.id.recipe_recyclerview);
        /*An adapter is the lists contents, in this case we are having a list of Recipe objects
        * therefore a custom adapter class is made to be able to parse the Recipe objects into the list
        */
        RecipeAdapter recipeAdapter = new RecipeAdapter(recipeList);
        //Setting the list adapter
        recipeListView.setAdapter(recipeAdapter);
        //Generating a layout and dividers for the list
        recipeListView.setLayoutManager(new LinearLayoutManager(this));
        recipeListView.addItemDecoration(new DividerItemDecoration(getApplicationContext(),
                DividerItemDecoration.VERTICAL));

        //Item select event
        recipeAdapter.setOnItemClickListener(new RecipeAdapter.ClickListener() {
            @Override
            public void onItemClick(int position, View v) {
                //Building a new intent to go from the current context to the ViewRecipe page
                Intent intent = new Intent(getApplicationContext(),ViewRecipeActicity.class);
                //Passing the recipe that was clicked on to the new page
                intent.putExtra("recipe",recipeList.get(position));
                //Beginning the View Recipe Activity
                startActivity(intent);
            }
        });
    }

    //Default Methods for the activity, you don't need to worry about these at the moment

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
