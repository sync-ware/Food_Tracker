package com.csed.foodtracker;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
    private ArrayList<Recipe> recipeList = new ArrayList<>();
    private ArrayList<Ingredient> ingredientList = new ArrayList<>();
    String themeVal;
    SharedPreferences themePrefs;
    FloatingActionMenu materialDesignFAM;
    RecipeAdapter recipeAdapter;
    RecyclerView recipeListView;




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
        themePrefs = getSharedPreferences("com.csed.foodtracker.theme", 0);
        themeVal = themePrefs.getString("theme", "1");
        if (themeVal.equals("1")) {
            setTheme(R.style.AppTheme);
        } else {
            setTheme(R.style.AppThemeDark);
        }
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        recipeListView = findViewById(R.id.recipe_recyclerview);
        recipeListView.setLayoutManager(new LinearLayoutManager(this));
        recipeListView.addItemDecoration(new DividerItemDecoration(getApplicationContext(),
                DividerItemDecoration.VERTICAL));

        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        FloatingActionButton floatingActionButton1, floatingActionButton2, floatingActionButton3;

        materialDesignFAM = (FloatingActionMenu) findViewById(R.id.material_design_android_floating_action_menu);
        floatingActionButton1 = (com.github.clans.fab.FloatingActionButton) findViewById(R.id.material_design_floating_action_menu_item1);
        floatingActionButton2 = (com.github.clans.fab.FloatingActionButton) findViewById(R.id.material_design_floating_action_menu_item2);
        floatingActionButton3 = (com.github.clans.fab.FloatingActionButton) findViewById(R.id.material_design_floating_action_menu_item3);
        floatingActionButton1.setColorNormal(Color.parseColor("#D81A60"));
        floatingActionButton2.setColorNormal(Color.parseColor("#D81A60"));
        floatingActionButton3.setColorNormal(Color.parseColor("#D81A60"));



        floatingActionButton1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Intent to transfer from current page to new the add recipe activity
                Intent intent = new Intent(getApplicationContext(), AddRecipeActivity.class);
                //Put ingredient list into Intent
                intent.putExtra("ingredientList", ingredientList);
                //Begin new activity
                startActivity(intent);
            }
        });
        floatingActionButton2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Intent to transfer from current page to new the add recipe activity
                Intent intent = new Intent(getApplicationContext(), AddIngredientActivity.class);
                //Put ingredient list into Intent
//                intent.putExtra("ingredientList",ingredientList);
                //Begin new activity
                startActivity(intent);
            }
        });
        floatingActionButton3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), UploadRecieptActivity.class);
                startActivity(intent);
            }
        });
        filterMode = 0;

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
    private void createRecipeList() {
        //Stops items in the list from duplication which can occur when relaunching the activity
        if (recipeList.isEmpty()) {
            //Select SQL Query that pulls data from database and stores it in cursor object
            Cursor recipeTable = mDb.rawQuery("SELECT Recipes.recipe_id, Recipes.name, Recipes.description, Recipes.image," +
                    "Recipes.prep_time, Recipes.calories, Recipes.url, Recipes.favourite FROM Recipes ORDER BY Recipes.favourite DESC, Recipes.name ASC", null);

            // Returns all. But we want to "order" by cookable
            //Start at first row
            recipeTable.moveToPosition(0);
            //Keep looping until you reach the last row
            while (recipeTable.getPosition() < recipeTable.getCount()) {
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
    private void createIngredientList() {
        if (ingredientList.isEmpty()) {
            Cursor ingredientTable = mDb.rawQuery("SELECT Ingredients.ing_id, Ingredients.name, Ingredients.best_before," +
                    "Ingredients.num, Ingredients.units FROM Ingredients ", null);

            ingredientTable.moveToPosition(0);
            while (ingredientTable.getPosition() < ingredientTable.getCount()) {
                Ingredient ingredient = new Ingredient();

                int id = ingredientTable.getInt(ingredientTable.getColumnIndex("ing_id"));
                String name = ingredientTable.getString(ingredientTable.getColumnIndex("name"));
                String bestBefore = ingredientTable.getString(ingredientTable.getColumnIndex("best_before"));
                String num = ingredientTable.getString(ingredientTable.getColumnIndex("num"));
                String units = ingredientTable.getString(ingredientTable.getColumnIndex("units"));
                ingredient.setId(id);
                ingredient.setName(name);
                ingredient.setBestBefore(bestBefore);
                ingredient.setNumber(num);
                ingredient.setUnits(units);

                ingredientList.add(ingredient);
                ingredientTable.moveToNext();
            }

            ingredientTable.close();
        } else { // Duplicate code from checkIngredientList
            Cursor ingredientTable = mDb.rawQuery("SELECT Ingredients.ing_id, Ingredients.name, Ingredients.best_before," +
                    "Ingredients.num, Ingredients.units FROM Ingredients ", null);

            ingredientTable.moveToPosition(0);
            while (ingredientTable.getPosition() < ingredientTable.getCount()) {
                Ingredient ingredient = new Ingredient();

                int id = ingredientTable.getInt(ingredientTable.getColumnIndex("ing_id"));
                String name = ingredientTable.getString(ingredientTable.getColumnIndex("name"));
                String bestBefore = ingredientTable.getString(ingredientTable.getColumnIndex("best_before"));
                String num = ingredientTable.getString(ingredientTable.getColumnIndex("num"));
                String units = ingredientTable.getString(ingredientTable.getColumnIndex("units"));

                ingredient.setId(id);
                ingredient.setName(name);
                ingredient.setBestBefore(bestBefore);
                ingredient.setNumber(num);
                ingredient.setUnits(units);
                boolean found = false;
                for (int i = 0; i < ingredientList.size(); i++) {
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
    protected void initialiseListUI() {
/*        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);*/
        final List<Recipe> newRecipeList = new ArrayList<>(); // This is the list that ends up being used for recipeListView
        if (filterMode == 0) { // filterMode 0 is all option
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                recipeList.sort(new RecipeComparitor());
            }
            recipeAdapter = new RecipeAdapter(recipeList, getResources(), this, themeVal); // Ordered one instead
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
                            }
                            if (ingCount == actCount) {
                                for (Recipe r : recipeList) {
                                    if (r.getId() == ing) {
                                        if (newRecipeList.indexOf(r) == -1) {
                                            newRecipeList.add(r);
                                        }
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
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                newRecipeList.sort(new RecipeComparitor());
            }
            recipeAdapter = new RecipeAdapter(newRecipeList, getResources(), this, themeVal); // Defines the adapter with the newRecipeList
        }

        //Setting the list adapter
        recipeListView.setAdapter(recipeAdapter);
        //Generating a layout and dividers for th]e list

        //Item select event
        recipeAdapter.setOnItemClickListener(new RecipeAdapter.ClickListener() {
            @Override
            public void onItemClick(int position, View v) {
                //Building a new intent to go from the current context to the ViewRecipe page
                Intent intent = new Intent(getApplicationContext(), ViewRecipeActivity.class);
                intent.putExtra("mode", filterMode); // Added a new intent to pass the filter mode into the ViewRecipeActivity
                //Passing the recipe that was clicked on to the new page
                if (filterMode == 0) { // This is added to make sure the correct recipe appears
                    intent.putExtra("recipe", recipeList.get(position));

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
            case R.id.action_theme:
                SharedPreferences.Editor themeEditor = themePrefs.edit();
                if (themeVal.equals("1")) {
                    themeEditor.putString("theme", "0").apply();
                    Toast.makeText(MainActivity.this, "Switched to dark theme", Toast.LENGTH_SHORT).show();
                } else {
                    themeEditor.putString("theme", "1").apply();
                    Toast.makeText(MainActivity.this, "Switched to light theme", Toast.LENGTH_SHORT).show();
                }
                recreate();
                // User chose the "Settings" item, show the app settings UI...
                return true;
            case R.id.action_resetDb:
                // Deletes all records from the database
                mDb.execSQL("DELETE FROM Ingredients;");
                mDb.execSQL("DELETE FROM Recipes;");
                mDb.execSQL("DELETE FROM RecipeIngredients;");
                // Refills the datahase with stock data
                mDb.execSQL("INSERT INTO 'main'.'Recipes' ('recipe_id', 'name', 'description', 'image', 'prep_time', 'calories', 'url', 'favourite') VALUES ('1', 'Spaghetti Bolognese', 'Put the onion and oil in a large pan and fry over a fairly high heat for 3-4 mins. Add the garlic and mince and fry until they both brown. Add the mushrooms and herbs, and cook for another couple of mins.', 'stock1', '00:10', '640', 'https://www.goodtoknow.co.uk/recipes/spaghetti-bolognese-1', '0');");
                mDb.execSQL("INSERT INTO 'main'.'Recipes' ('recipe_id', 'name', 'description', 'image', 'prep_time', 'calories', 'url', 'favourite') VALUES ('2', 'Pasta and Pesto', 'Boil water in a pan, then add the pasta. Leave for 10 minutes until soft. Drain water and add pesto and cheese. ', 'stock2', '00:10', '10', 'http://allrecipes.co.uk/consent/?dest=/recipe/1646/hey-pesto-pasta.aspx', '0');");
                mDb.execSQL("INSERT INTO 'main'.'Recipes' ('recipe_id', 'name', 'description', 'image', 'prep_time', 'calories', 'url', 'favourite') VALUES ('3', 'Bacon Pasta', 'Cook pasta in pot and cook bacon in pan, then put together.', 'stock3', '00:13', '100', 'https://www.recipetineats.com/tomato-bacon-pasta/', '0');");
                mDb.execSQL("INSERT INTO 'main'.'Recipes' ('recipe_id', 'name', 'description', 'image', 'prep_time', 'calories', 'url', 'favourite') VALUES ('4', 'Pizza', 'Cook the pizza in the oven for about 10 minutes. Add any extra toppings to taste', 'stock4', '55:55', '555', 'http://www.pizza.com/', '0');");
                mDb.execSQL("INSERT INTO 'main'.'Recipes' ('recipe_id', 'name', 'description', 'image', 'prep_time', 'calories', 'url', 'favourite') VALUES ('5', 'Toast', 'Put bread in the toaster. Set the dial to prefered setting. Wait for it to pop, then add butter to taste', 'stock5', '01:00', '1', 'https://www.toast.chicken/', '0');");
                mDb.execSQL("INSERT INTO 'main'.'Recipes' ('recipe_id', 'name', 'description', 'image', 'prep_time', 'calories', 'url', 'favourite') VALUES ('6', 'Cake', 'Put the flour in a bowl, add eggs and sugar. Mix together. Cook for half an hour then wait to cool.', 'stock6', '01:30', '400', 'https://www.houseandgarden.co.uk/recipe/simple-vanilla-cake-recipe', '0');");
                mDb.execSQL("INSERT INTO 'main'.'Recipes' ('recipe_id', 'name', 'description', 'image', 'prep_time', 'calories', 'url', 'favourite') VALUES ('7', 'Egg Noodles', 'Cook noodles in a pan, then poach the eggs. Once cooked, put together', 'stock7', '00:20', '199', 'https://www.allrecipes.com/recipe/239525/noodles-and-eggs/', '0');");
                mDb.execSQL("INSERT INTO 'main'.'Recipes' ('recipe_id', 'name', 'description', 'image', 'prep_time', 'calories', 'url', 'favourite') VALUES ('8', 'Bacon and Chips', 'Chop potatoes into chip shaped slices. Heat frying pan to ~200 degrees. Put chips in and wait till crisp. Meanwhile, fry bacon in a pan.', 'stock8', '00:40', '851', 'https://blog.paleohacks.com/bacon-chips/', '0');");
                mDb.execSQL("INSERT INTO 'main'.'Recipes' ('recipe_id', 'name', 'description', 'image', 'prep_time', 'calories', 'url', 'favourite') VALUES ('9', 'Baked Potato', 'Wrap potato in tin foil, stab with a fork and put in the microwave for 10 minutes. Unwrap, add baked beans and cook for a further 5 minutes', 'stock9', '00:15', '12', 'https://www.bbcgoodfood.com/howto/guide/how-make-ultimate-baked-potato', '0');");
                mDb.execSQL("INSERT INTO 'main'.'Recipes' ('recipe_id', 'name', 'description', 'image', 'prep_time', 'calories', 'url', 'favourite') VALUES ('10', 'Stir Fry', 'Chop up chicken into small slices, begin cooking in pan. Meanwhile, chop up peppers, carrots and onions and add to pan. Once all is nearly cooked, add sauce to taste. Cook rice in a pan to supplement it', 'stock10', '01:00', '134', 'https://www.bbcgoodfood.com/recipes/collection/stir-fry', '0');");
                mDb.execSQL("INSERT INTO 'main'.'Ingredients' ('ing_id', 'name', 'best_before', 'num', 'units') VALUES ('1', 'Onion', '0000-03-00', '2', 'Whole');");
                mDb.execSQL("INSERT INTO 'main'.'Ingredients' ('ing_id', 'name', 'best_before', 'num', 'units') VALUES ('5', 'Carrot', '0000-03-00', '5', 'Whole');");
                mDb.execSQL("INSERT INTO 'main'.'Ingredients' ('ing_id', 'name', 'best_before', 'num', 'units') VALUES ('6', 'Pasta', '0000-03-00', '400', 'g');");
                mDb.execSQL("INSERT INTO 'main'.'Ingredients' ('ing_id', 'name', 'best_before', 'num', 'units') VALUES ('7', 'Pesto', '0000-03-00', '150', 'g');");
                mDb.execSQL("INSERT INTO 'main'.'Ingredients' ('ing_id', 'name', 'best_before', 'num', 'units') VALUES ('8', 'Bread', '0000-03-00', '15', 'Pieces');");
                mDb.execSQL("INSERT INTO 'main'.'Ingredients' ('ing_id', 'name', 'best_before', 'num', 'units') VALUES ('9', 'Bacon', '0000-03-00', '8', 'Pieces');");
                mDb.execSQL("INSERT INTO 'main'.'Ingredients' ('ing_id', 'name', 'best_before', 'num', 'units') VALUES ('10', 'Pizza', '0000-03-00', '1', 'Whole');");
                mDb.execSQL("INSERT INTO 'main'.'Ingredients' ('ing_id', 'name', 'best_before', 'num', 'units') VALUES ('11', 'Cheese', '0000-01-00', '50', 'g');");
                mDb.execSQL("INSERT INTO 'main'.'Ingredients' ('ing_id', 'name', 'best_before', 'num', 'units') VALUES ('12', 'Chicken', '0000-01-00', '0', 'Pieces');");
                mDb.execSQL("INSERT INTO 'main'.'Ingredients' ('ing_id', 'name', 'best_before', 'num', 'units') VALUES ('13', 'Sausages', '0000-11-00', '0', 'Whole');");
                mDb.execSQL("INSERT INTO 'main'.'Ingredients' ('ing_id', 'name', 'best_before', 'num', 'units') VALUES ('14', 'Milk', '0000-02-00', '0', 'ml');");
                mDb.execSQL("INSERT INTO 'main'.'Ingredients' ('ing_id', 'name', 'best_before', 'num', 'units') VALUES ('15', 'Celery', '0000-01-00', '0', 'Pieces');");
                mDb.execSQL("INSERT INTO 'main'.'Ingredients' ('ing_id', 'name', 'best_before', 'num', 'units') VALUES ('16', 'Mince', '0000-01-00', '0', 'g');");
                mDb.execSQL("INSERT INTO 'main'.'Ingredients' ('ing_id', 'name', 'best_before', 'num', 'units') VALUES ('17', 'Ham', '0000-01-00', '0', 'Slices');");
                mDb.execSQL("INSERT INTO 'main'.'Ingredients' ('ing_id', 'name', 'best_before', 'num', 'units') VALUES ('18', 'Rice', '0000-01-00', '0', 'g');");
                mDb.execSQL("INSERT INTO 'main'.'Ingredients' ('ing_id', 'name', 'best_before', 'num', 'units') VALUES ('19', 'Avocados', '0000-01-00', '0', 'Whole');");
                mDb.execSQL("INSERT INTO 'main'.'Ingredients' ('ing_id', 'name', 'best_before', 'num', 'units') VALUES ('20', 'Butter', '0000-01-00', '0', 'g');");
                mDb.execSQL("INSERT INTO 'main'.'Ingredients' ('ing_id', 'name', 'best_before', 'num', 'units') VALUES ('21', 'Bananas', '0000-01-00', '0', 'Whole');");
                mDb.execSQL("INSERT INTO 'main'.'Ingredients' ('ing_id', 'name', 'best_before', 'num', 'units') VALUES ('22', 'Potatoes', '0000-01-00', '0', 'Whole');");
                mDb.execSQL("INSERT INTO 'main'.'Ingredients' ('ing_id', 'name', 'best_before', 'num', 'units') VALUES ('23', 'Tortillas', '0000-01-00', '0', 'Whole');");
                mDb.execSQL("INSERT INTO 'main'.'Ingredients' ('ing_id', 'name', 'best_before', 'num', 'units') VALUES ('24', 'Peppers', '0000-01-00', '0', 'Whole');");
                mDb.execSQL("INSERT INTO 'main'.'Ingredients' ('ing_id', 'name', 'best_before', 'num', 'units') VALUES ('25', 'Tuna', '0000-01-00', '0', 'g');");
                mDb.execSQL("INSERT INTO 'main'.'Ingredients' ('ing_id', 'name', 'best_before', 'num', 'units') VALUES ('26', 'Tomatoes', '0000-01-00', '0', 'Whole');");
                mDb.execSQL("INSERT INTO 'main'.'Ingredients' ('ing_id', 'name', 'best_before', 'num', 'units') VALUES ('27', 'Spaghetti', '0000-01-00', '0', 'g');");
                mDb.execSQL("INSERT INTO 'main'.'Ingredients' ('ing_id', 'name', 'best_before', 'num', 'units') VALUES ('28', 'Noodles', '0000-01-00', '0', 'g');");
                mDb.execSQL("INSERT INTO 'main'.'Ingredients' ('ing_id', 'name', 'best_before', 'num', 'units') VALUES ('29', 'Sauce', '0000-01-00', '0', 'g');");
                mDb.execSQL("INSERT INTO 'main'.'Ingredients' ('ing_id', 'name', 'best_before', 'num', 'units') VALUES ('30', 'Peas', '0000-01-00', '0', 'g');");
                mDb.execSQL("INSERT INTO 'main'.'Ingredients' ('ing_id', 'name', 'best_before', 'num', 'units') VALUES ('31', 'Seasoning', '0000-01-00', '0', 'g');");
                mDb.execSQL("INSERT INTO 'main'.'Ingredients' ('ing_id', 'name', 'best_before', 'num', 'units') VALUES ('32', 'Flour', '0000-01-00', '0', 'g');");
                mDb.execSQL("INSERT INTO 'main'.'Ingredients' ('ing_id', 'name', 'best_before', 'num', 'units') VALUES ('33', 'Sugar', '0000-01-00', '0', 'g');");
                mDb.execSQL("INSERT INTO 'main'.'Ingredients' ('ing_id', 'name', 'best_before', 'num', 'units') VALUES ('34', 'Eggs', '0000-01-00', '0', 'Whole');");
                mDb.execSQL("INSERT INTO 'main'.'Ingredients' ('ing_id', 'name', 'best_before', 'num', 'units') VALUES ('35', 'Beans', '0000-01-00', '0', 'g');");
                mDb.execSQL("INSERT INTO 'main'.'RecipeIngredients' ('id', 'recipe_id', 'ing_id', 'measurement', 'detail') VALUES ('1', '1', '1', '1.0', 'Peeled and chopped');");
                mDb.execSQL("INSERT INTO 'main'.'RecipeIngredients' ('id', 'recipe_id', 'ing_id', 'measurement', 'detail') VALUES ('2', '1', '6', '100.0', 'Just put it in');");
                mDb.execSQL("INSERT INTO 'main'.'RecipeIngredients' ('id', 'recipe_id', 'ing_id', 'measurement', 'detail') VALUES ('3', '2', '6', '100.0', 'Just put it in');");
                mDb.execSQL("INSERT INTO 'main'.'RecipeIngredients' ('id', 'recipe_id', 'ing_id', 'measurement', 'detail') VALUES ('4', '2', '7', '25.0', 'Put it on top');");
                mDb.execSQL("INSERT INTO 'main'.'RecipeIngredients' ('id', 'recipe_id', 'ing_id', 'measurement', 'detail') VALUES ('5', '3', '6', '100.0', 'Just put it in');");
                mDb.execSQL("INSERT INTO 'main'.'RecipeIngredients' ('id', 'recipe_id', 'ing_id', 'measurement', 'detail') VALUES ('6', '3', '9', '3.0', 'All the bacons');");
                mDb.execSQL("INSERT INTO 'main'.'RecipeIngredients' ('id', 'recipe_id', 'ing_id', 'measurement', 'detail') VALUES ('7', '4', '10', '1.0', 'It''s pizza');");
                mDb.execSQL("INSERT INTO 'main'.'RecipeIngredients' ('id', 'recipe_id', 'ing_id', 'measurement', 'detail') VALUES ('8', '5', '8', '2.0', 'Toast');");
                mDb.execSQL("INSERT INTO 'main'.'RecipeIngredients' ('id', 'recipe_id', 'ing_id', 'measurement', 'detail') VALUES ('9', '2', '11', '30.0', 'Sprinkle it on top');");
                mDb.execSQL("INSERT INTO 'main'.'RecipeIngredients' ('id', 'recipe_id', 'ing_id', 'measurement', 'detail') VALUES ('10', '1', '29', '100.0', 'Put in');");
                mDb.execSQL("INSERT INTO 'main'.'RecipeIngredients' ('id', 'recipe_id', 'ing_id', 'measurement', 'detail') VALUES ('11', '1', '26', '2.0', 'Put in');");
                mDb.execSQL("INSERT INTO 'main'.'RecipeIngredients' ('id', 'recipe_id', 'ing_id', 'measurement', 'detail') VALUES ('12', '6', '33', '100.0', 'Put in');");
                mDb.execSQL("INSERT INTO 'main'.'RecipeIngredients' ('id', 'recipe_id', 'ing_id', 'measurement', 'detail') VALUES ('13', '6', '32', '250.0', 'Put in');");
                mDb.execSQL("INSERT INTO 'main'.'RecipeIngredients' ('id', 'recipe_id', 'ing_id', 'measurement', 'detail') VALUES ('14', '6', '20', '75.0', 'Put in');");
                mDb.execSQL("INSERT INTO 'main'.'RecipeIngredients' ('id', 'recipe_id', 'ing_id', 'measurement', 'detail') VALUES ('15', '6', '14', '250.0', 'Put in');");
                mDb.execSQL("INSERT INTO 'main'.'RecipeIngredients' ('id', 'recipe_id', 'ing_id', 'measurement', 'detail') VALUES ('16', '7', '28', '80.0', 'Put in');");
                mDb.execSQL("INSERT INTO 'main'.'RecipeIngredients' ('id', 'recipe_id', 'ing_id', 'measurement', 'detail') VALUES ('17', '7', '34', '2.0', 'Put in');");
                mDb.execSQL("INSERT INTO 'main'.'RecipeIngredients' ('id', 'recipe_id', 'ing_id', 'measurement', 'detail') VALUES ('18', '8', '9', '3.0', 'Put in');");
                mDb.execSQL("INSERT INTO 'main'.'RecipeIngredients' ('id', 'recipe_id', 'ing_id', 'measurement', 'detail') VALUES ('19', '8', '22', '4.0', 'Put in');");
                mDb.execSQL("INSERT INTO 'main'.'RecipeIngredients' ('id', 'recipe_id', 'ing_id', 'measurement', 'detail') VALUES ('20', '9', '22', '1.0', 'Put in');");
                mDb.execSQL("INSERT INTO 'main'.'RecipeIngredients' ('id', 'recipe_id', 'ing_id', 'measurement', 'detail') VALUES ('21', '9', '35', '30.0', 'Put in');");
                mDb.execSQL("INSERT INTO 'main'.'RecipeIngredients' ('id', 'recipe_id', 'ing_id', 'measurement', 'detail') VALUES ('22', '10', '18', '100.0', 'Put in');");
                mDb.execSQL("INSERT INTO 'main'.'RecipeIngredients' ('id', 'recipe_id', 'ing_id', 'measurement', 'detail') VALUES ('23', '10', '29', '50.0', 'Put in');");
                mDb.execSQL("INSERT INTO 'main'.'RecipeIngredients' ('id', 'recipe_id', 'ing_id', 'measurement', 'detail') VALUES ('24', '10', '24', '3.0', 'Put in');");
                mDb.execSQL("INSERT INTO 'main'.'RecipeIngredients' ('id', 'recipe_id', 'ing_id', 'measurement', 'detail') VALUES ('25', '10', '5', '2.0', 'Put in');");
                Toast.makeText(MainActivity.this, "Reset Database", Toast.LENGTH_SHORT).show();
                recreate();

                return true;
            case R.id.action_filter:

                //Show filter options
                View view = findViewById(R.id.action_filter);
                PopupMenu popup = new PopupMenu(this, view, Gravity.CENTER);
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.filters, popup.getMenu());
                popup.show();

                if (filterMode == 0) {
                    popup.getMenu().getItem(0).setChecked(true);
                } else if (filterMode == 1) {
                    popup.getMenu().getItem(1).setChecked(true);
                } else if (filterMode == 2) {

                    popup.getMenu().getItem(2).setChecked(true);
                }

                //Filter click events, here the Recipe list will be sorted
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.filter_available:
                                Toast.makeText(MainActivity.this, "Showing Cookable", Toast.LENGTH_SHORT).show();
                                item.setChecked(true);
                                filterMode = 1;
                                initialiseListUI();
                                return true;
                            case R.id.filter_unavailable:
                                Toast.makeText(MainActivity.this, "Showing Uncookable", Toast.LENGTH_SHORT).show();
                                item.setChecked(true);
                                filterMode = 2;
                                initialiseListUI();
                                return true;
                            case R.id.filter_all:
                                Toast.makeText(MainActivity.this, "Showing All", Toast.LENGTH_SHORT).show();
                                item.setChecked(true);
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
    public void onResume() {
        super.onResume();
        //Reinitialise recipe list to reflect any changes
        recipeList.clear();
        materialDesignFAM.close(false);
        createRecipeList();
        initialiseListUI();
        createIngredientList();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Intent intent;
        switch (id) {
            case R.id.nav_recipes:
                intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_ingredients:
                intent = new Intent(this, IngredientsActivity.class);
                intent.putExtra("ingredientList", ingredientList);
                startActivity(intent);
                break;
            case R.id.nav_addRecipe:
                intent = new Intent(getApplicationContext(), AddRecipeActivity.class);
                //Put ingredient list into Intent
                intent.putExtra("ingredientList", ingredientList);
                //Begin new activity
                startActivity(intent);
                break;
            case R.id.nav_addIngredient:
                intent = new Intent(getApplicationContext(), AddIngredientActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_uploadPhoto:
                intent = new Intent(getApplicationContext(), UploadRecieptActivity.class);
                startActivity(intent);
                break;
        }
        return true;
    }
}
