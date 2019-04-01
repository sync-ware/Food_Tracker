package com.csed.foodtracker;

import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.MenuInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;


import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private int filterMode; // Filter mode is
    private DatabaseHelper mDBHelper;
    private SQLiteDatabase mDb;
    private SQLiteDatabase testDB;
    private ArrayList<Recipe> recipeList = new ArrayList<>();
    private ArrayList<Ingredient> ingredientList = new ArrayList<>();


    private boolean filterAll;
    private boolean filterAvailable;
    private boolean filterUnavailable;

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
            testDB = mDBHelper.getWritableDatabase();

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



        FloatingActionMenu materialDesignFAM;
        FloatingActionButton floatingActionButton1, floatingActionButton2, floatingActionButton3;

        materialDesignFAM = (FloatingActionMenu) findViewById(R.id.material_design_android_floating_action_menu);
        floatingActionButton1 = (com.github.clans.fab.FloatingActionButton) findViewById(R.id.material_design_floating_action_menu_item1);
        floatingActionButton2 = (com.github.clans.fab.FloatingActionButton) findViewById(R.id.material_design_floating_action_menu_item2);
        floatingActionButton3 = (com.github.clans.fab.FloatingActionButton) findViewById(R.id.material_design_floating_action_menu_item3);


        floatingActionButton1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Intent to transfer from current page to new the add recipe activity
                Intent intent = new Intent(getApplicationContext(),AddRecipeActivity.class);
                //Put ingredient list into Intent
                intent.putExtra("ingredientList",ingredientList);
                //Begin new activity
                startActivity(intent);
            }
        });
        floatingActionButton2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Intent to transfer from current page to new the add recipe activity
                Intent intent = new Intent(getApplicationContext(),AddIngredientActivity.class);
                //Put ingredient list into Intent
//                intent.putExtra("ingredientList",ingredientList);
                //Begin new activity
                startActivity(intent);
            }
        });
        floatingActionButton3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),UploadRecieptActivity.class);
                startActivity(intent);
            }
        });

        filterAll = true;
        filterAvailable = false;
        filterUnavailable = false;



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
                    "Recipes.prep_time, Recipes.calories, Recipes.url, Recipes.favourite FROM Recipes",null);


            // Returns all. But we want to "order" by cookable
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
                int favourite = recipeTable.getInt(recipeTable.getColumnIndex("favourite"));



                //Set recipe attributes with data from the database
                recipe.setId(id);
                recipe.setName(name);
                recipe.setDescription(description);
                recipe.setImage(image);
                recipe.setPrepTime(prepTime);
                recipe.setCalories(calories);
                recipe.setUrl(url);
                recipe.setFavourite(favourite);

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
                    "Ingredients.num FROM Ingredients ", null);

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
        } else { // Duplicate code from checkIngredientList
            Cursor ingredientTable = mDb.rawQuery("SELECT Ingredients.ing_id, Ingredients.name, Ingredients.best_before," +
                    "Ingredients.num FROM Ingredients ", null);

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
                boolean found = false;
                for (int i=0; i <ingredientList.size(); i++) {
                    if (ingredientList.get(i).getName().equals(ingredient.getName())) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    ingredientList.add(ingredient);
                }
                ingredientTable.moveToNext();
            }

            ingredientTable.close();
        }
    }

    /**
     * Generate List UI.
     * This does one of three things, based on user's filter option. If they choose to show all (mode 0)
     * Then it simply assigns the recipe adapter to the original recipe list
     * If the user chooses one of the other options (1 or 2), then a new list is created, filtering
     * out ones which can or can't be cooked, based on the choice
     */
    private void initialiseListUI(){
        RecyclerView recipeListView = findViewById(R.id.recipe_recyclerview);
        RecipeAdapter recipeAdapter;
        final List<Recipe> newRecipeList = new ArrayList<>(); // This is the list that ends up being used for recipeListView
        if (filterMode == 0) { // filterMode 0 is all option
            recipeList.sort(new RecipeComparitor());
            recipeAdapter = new RecipeAdapter(recipeList); // Ordered one instead
        } else {
                try {
                    for (int i = 0; i < 100; i++) {
                        Cursor fullQuery = mDb.rawQuery("SELECT Recipes.recipe_id " +
                                "FROM Ingredients,RecipeIngredients INNER JOIN Recipes " +
                                "ON RecipeIngredients.ing_id = Ingredients.ing_id " +
                                "AND Recipes.recipe_id = RecipeIngredients.recipe_id " +
                                "WHERE Recipes.recipe_id ='" + i + "' ORDER BY Recipes.recipe_id;",
                                null);
                        fullQuery.moveToPosition(0);
                        // Defining the Recipe ID array list variables
                        List<Integer> fullRecipeIDList = new ArrayList<>();
                        List<Integer> cookableRecipeIDList = new ArrayList<>();
                        //Keep looping until you reach the last row

                        while (fullQuery.getPosition() < fullQuery.getCount()) {
                            //Retrieve data from each column
                            int id = fullQuery.getInt(fullQuery.getColumnIndex("recipe_id"));
                            fullRecipeIDList.add(id);
                            fullQuery.moveToNext();
                        }
                        fullQuery.close();
                        Cursor cookableQuery = mDb.rawQuery("SELECT Recipes.recipe_id " +
                                "FROM Ingredients,RecipeIngredients " +
                                "INNER JOIN Recipes ON RecipeIngredients.ing_id = Ingredients.ing_id " +
                                "AND Recipes.recipe_id = RecipeIngredients.recipe_id " +
                                "AND RecipeIngredients.measurement <= Ingredients.num " +
                                "WHERE Recipes.recipe_id ='" + i + "' ORDER BY Recipes.recipe_id;",
                                null);
                        cookableQuery.moveToPosition(0);
                        while (cookableQuery.getPosition() < cookableQuery.getCount()) {
                            //Retrieve data from each column
                            int id = cookableQuery.getInt(cookableQuery.getColumnIndex("recipe_id"));
                            cookableRecipeIDList.add(id);
                            cookableQuery.moveToNext();
                        }
                        cookableQuery.close();
                        // The following statement checks to see if they have all of the required ingredients
                        if (fullRecipeIDList.size() == cookableRecipeIDList.size() && filterMode == 1) { // 1 is cookable only
                            for (Recipe r : recipeList) { // If they do, then it loops through to find the recipe they're trying to cook
                                if (cookableRecipeIDList.indexOf(r.getId()) != -1) {
                                    newRecipeList.add(r);
                                }
                            }
                        } else if (filterMode == 2) { // 2 is non cookable only
                            int ingCount = 0;
                            int actCount = 0;
                            int curr = -1;
                            // Loops through to count the number of instances of each ID
                            // Then only shows the, of the count is equal
                            for (int ing : fullRecipeIDList) {
                                for (int ii : cookableRecipeIDList) {
                                    if (ii == ing) {
                                        ingCount++;
                                    }
                                }
                                if (ing == curr) {
                                    actCount++;
                                    System.out.println(actCount == ingCount);
                                }
                                if (ingCount == actCount) {
                                    for (Recipe r : recipeList) {
                                        if (r.getId() == ing) {
                                            newRecipeList.add(r);
                                        }
                                    }
                                }
                                actCount = 0;
                                ingCount = 0;
                                curr = ing;
                            }
                        }
                    }
                } catch (IndexOutOfBoundsException e) { // Bodge to avoid another SQL query
                    Toast.makeText(MainActivity.this, "Finished Loading", Toast.LENGTH_SHORT).show();
                }
                recipeAdapter = new RecipeAdapter(newRecipeList); // Defines the adapter with the newRecipeList
            }

        //Setting the list adapter
        recipeListView.setAdapter(recipeAdapter);
        //Generating a layout and dividers for th]e list
        recipeListView.setLayoutManager(new LinearLayoutManager(this));
        recipeListView.addItemDecoration(new DividerItemDecoration(getApplicationContext(),
                DividerItemDecoration.VERTICAL));

        //Item select event
        recipeAdapter.setOnItemClickListener(new RecipeAdapter.ClickListener() {
            @Override
            public void onItemClick(int position, View v) {
                //Building a new intent to go from the current context to the ViewRecipe page
                Intent intent = new Intent(getApplicationContext(), ViewRecipeActivity.class);
                intent.putExtra("mode",filterMode); // Added a new intent to pass the filter mode into the ViewRecipeActivity
                //Passing the recipe that was clicked on to the new page
                if (filterMode == 0) { // This is added to make sure the correct recipe appears
                    intent.putExtra("recipe",recipeList.get(position));

                } else { // newRecipeList only is used during other filter modes.
                    intent.putExtra("recipe", newRecipeList.get(position));
                }
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
        switch (item.getItemId()) {
            case R.id.action_settings:
                // User chose the "Settings" item, show the app settings UI...
                return true;

            case R.id.action_filter:

                //Show filter options
                View view = findViewById(R.id.action_filter);
                PopupMenu popup = new PopupMenu(this, view, Gravity.CENTER);
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.filters, popup.getMenu());
                popup.show();

                if (filterAll){
                    popup.getMenu().getItem(0).setChecked(true);
                }
                else if (filterAvailable){
                    popup.getMenu().getItem(1).setChecked(true);
                } else if (filterUnavailable) {

                    popup.getMenu().getItem(2).setChecked(true);
                }

                //Filter click events, here the Recipe list will be sorted
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.filter_available:

                                Toast.makeText(MainActivity.this, "Show Available", Toast.LENGTH_SHORT).show();
                                item.setChecked(true);
                                filterAvailable = true;
                                filterAll = false;
                                filterUnavailable = false;
                                filterMode = 1;
                                initialiseListUI();

                                return true;

                            case R.id.filter_unavailable:

                                Toast.makeText(MainActivity.this, "Show Unavailable", Toast.LENGTH_SHORT).show();
                                item.setChecked(true);
                                filterUnavailable = true;
                                filterAll = false;
                                filterAvailable = false;
                                filterMode = 2;
                                initialiseListUI();

                                return true;
                            case R.id.filter_all:

                                Toast.makeText(MainActivity.this, "Show All", Toast.LENGTH_SHORT).show();
                                item.setChecked(true);
                                filterAll = true;
                                filterAvailable = false;
                                filterUnavailable = false;
                                filterMode = 0;
                                initialiseListUI();
                                return true;

                            default:
                                // If we got here, the user's action was not recognized.
                                // Invoke the superclass to handle it.
                                return false;

                        }


                    }
                });

                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    //Action when main activity is resumed
    @Override
    public void onResume(){
        super.onResume();

        //Reinitialise recipe list to reflect any changes
        recipeList.clear();
        createRecipeList();
        initialiseListUI();
        createIngredientList();


    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id) {

            case R.id.nav_ingredients:
                Intent intent = new Intent(this, IngredientsActivity.class);
                intent.putExtra("ingredientList", ingredientList);
                startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
