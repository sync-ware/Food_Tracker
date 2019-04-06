package com.csed.foodtracker;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ViewRecipeActivity extends AppCompatActivity {

    private Recipe recipe;
    private int filterMode;
    DatabaseHelper mDBHelper;
    SQLiteDatabase mDb;
    List<Ingredient> ingList;
    IngredientAdapter ingredientAdapter;


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
/*        String themeVal;
        SharedPreferences themePrefs;
        themePrefs = getSharedPreferences("com.csed.foodtracker.theme", 0);
        themeVal = themePrefs.getString("theme", "1");
        if (themeVal == "1") {
            setTheme(R.style.AppTheme);
        } else {
            setTheme(R.style.AppThemeDark);
        }*/
        setContentView(R.layout.activity_view_recipe);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Retrieve Recipe from intent that was passed on to this activity
        recipe = (Recipe) getIntent().getSerializableExtra("recipe");

        /*Initialise UI elements, all are disable at first and cannot be edited
        * They are assigned the recipes attributes respectively
        */
        final TextInputEditText recipeName = (TextInputEditText) findViewById(R.id.text_name);
        recipeName.setText(recipe.getName());
        final TextInputEditText recipeDesc = (TextInputEditText) findViewById(R.id.text_desc);
        recipeDesc.setText(recipe.getDescription());
        final EditText recipePrepTime = (EditText) findViewById(R.id.text_preptime);
        recipePrepTime.setText(recipe.getPrepTime());
        final EditText recipeCalories = (EditText) findViewById(R.id.text_calories);
        recipeCalories.setText(Integer.toString(recipe.getCalories()));
        final TextInputEditText recipeUrl = (TextInputEditText) findViewById(R.id.text_url);
        recipeUrl.setText(recipe.getUrl());
        final Switch favouriteSwitch = (Switch) findViewById(R.id.switch2);
        final Button addIngredientButton = (Button) findViewById(R.id.button_add_ingredient);
        //inflater pulls a layout resource
        LayoutInflater inflater = (LayoutInflater) getApplicationContext().
                getSystemService(LAYOUT_INFLATER_SERVICE);
        //Assigning the layout/UI to the popup window
        View popupView = inflater.inflate(R.layout.popup_add_ingredient,null);

        final TextInputEditText textName = (TextInputEditText) popupView.findViewById(R.id.text_name);
        final EditText textAmount = (EditText) popupView.findViewById(R.id.text_number);
        Button confirmIngredientButton = (Button) popupView.findViewById(R.id.button_confirm_ingredient);
        favouriteSwitch.setEnabled(false);
        addIngredientButton.setEnabled(false);
        addIngredientButton.setVisibility(View.GONE);

        if (recipe.getFavourite() == 1) {
            favouriteSwitch.setChecked(true);
        } else {
            favouriteSwitch.setChecked(false);
        }



        Cursor cursor = mDb.rawQuery("SELECT Ingredients.name, RecipeIngredients.measurement" +
                " FROM Ingredients INNER JOIN RecipeIngredients ON" +
                " RecipeIngredients.ing_id = Ingredients.ing_id WHERE RecipeIngredients.recipe_id="+recipe.getId()
                ,null);
        ingList = new ArrayList<>();
        //Start at first row
        cursor.moveToPosition(0);
        //Keep looping until you reach the last row
        while (cursor.getPosition() < cursor.getCount()){
            String name = cursor.getString(cursor.getColumnIndex("name"));
            String measurement = cursor.getString(cursor.getColumnIndex("measurement"));
            Ingredient ingredient = new Ingredient();
            ingredient.setName(name);
            ingredient.setNumber(measurement);
            ingList.add(ingredient);
            cursor.moveToNext();
        }

        RecyclerView recipeListView = findViewById(R.id.list_ingredients);




        //URL click event, opens the web page into a chrome custom tab
        recipeUrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Chrome Custom Tabs can be used to open up web pages in the app itself
                CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                CustomTabsIntent customTabsIntent = builder.build();
                customTabsIntent.launchUrl(getApplicationContext(), Uri.parse(recipeUrl.getText().toString()));
            }
        });

        /*Click event used to switch between editable and non editable, if the user wants to edit the
        * recipe and then confirm any changes
        */
        final FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!recipeName.isEnabled()) {
                    fab.setImageResource(R.drawable.ic_tick);
                    recipeName.setEnabled(true);
                    recipeDesc.setEnabled(true);
                    recipePrepTime.setEnabled(true);
                    recipeCalories.setEnabled(true);
                    recipeUrl.setEnabled(true);
                    favouriteSwitch.setEnabled(true);
                    addIngredientButton.setEnabled(true);
                    addIngredientButton.setVisibility(View.VISIBLE);
                }
                else{
                    int mode = 0;
                    boolean favourite = favouriteSwitch.isChecked();
                    if (favourite) {
                        mode = 1;
                    }
                    mDb.execSQL("UPDATE Recipes SET name = '" + recipeName.getText() + "', description = '"
                            + recipeDesc.getText() + "', prep_time = '" + recipePrepTime.getText() + "', calories = "
                            + recipeCalories.getText() + ", url = '" + recipeUrl.getText() + "', favourite = '"+ mode + "' WHERE recipe_id = "
                            + recipe.getId() + ";");
                    Toast.makeText(ViewRecipeActivity.this, "Recipe Updated!", Toast.LENGTH_SHORT).show();
                    fab.setImageResource(R.drawable.ic_edit);
                    recipeName.setEnabled(false);
                    recipeDesc.setEnabled(false);
                    recipePrepTime.setEnabled(false);
                    recipeCalories.setEnabled(false);
                    recipeUrl.setEnabled(false);
                    favouriteSwitch.setEnabled(false);
                    addIngredientButton.setEnabled(false);
                    addIngredientButton.setVisibility(View.GONE);
                    finish();
                }
            }
        });

        filterMode = (Integer) getIntent().getSerializableExtra("mode"); // Passed in variable filter mode
        List<Ingredient> oldIngList = new ArrayList<>();
        List<Ingredient> newIngList = new ArrayList<>();
        List<String> cookableList = new ArrayList<>();
        boolean cookable = false;
        if (filterMode == 1) { // Cookable if they came from the available option
            cookable = true;
        } else { // User came from view all, so we need to make sure it can be cooked.
            Cursor cookableQuery = mDb.rawQuery("SELECT Ingredients.name " +
                            "FROM Ingredients,RecipeIngredients " +
                            "INNER JOIN Recipes ON RecipeIngredients.ing_id = Ingredients.ing_id " +
                            "AND Recipes.recipe_id = RecipeIngredients.recipe_id " +
                            "AND RecipeIngredients.measurement <= Ingredients.num " +
                            "WHERE Recipes.recipe_id ='" + recipe.getId() + "' ORDER BY Recipes.recipe_id;",
                    null);
            cookableQuery.moveToPosition(0);
            while (cookableQuery.getPosition() < cookableQuery.getCount()) {
                //Retrieve data from each column
                String name = cookableQuery.getString(cookableQuery.getColumnIndex("name"));
                cookableList.add(name);
                cookableQuery.moveToNext();
            }
            cookableQuery.close();
            if (cookableList.size() == ingList.size()) {
                cookable = true;
            } else {
                for (Ingredient ingredient: ingList) {
                    boolean found = false;
                    for (String nam: cookableList) {
                        if (ingredient.getName().equals(nam)) {
                            found = true;
                            oldIngList.add(ingredient);
                            break;
                        }
                    }
                    if (!found) {
                        // Need to make the ingredient appear red here
                        newIngList.add(ingredient);
                    }
                }
            }
        }
        final FloatingActionButton cook = findViewById(R.id.cook);

        /* Defines the recycler view(s) here, in order to populate it with ingredients the user doesn't have first,
        Or in the case where it is cookable, so it ignores the second list entirely
         */
        ingredientAdapter = new IngredientAdapter(ingList);
        recipeListView.setAdapter(ingredientAdapter);

        if (cookable) {
            cook.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    for (Ingredient ing : ingList) {
                        // Remove from db for each ingredient.
                        mDb.execSQL("UPDATE Ingredients SET num = num-'" + ing.getNumber() + "' WHERE name = '" + ing.getName() + "'");
                    }
                    Toast.makeText(ViewRecipeActivity.this, "Nice Meal! Ingredients Removed.", Toast.LENGTH_SHORT).show();
                    finish(); // Should return
                }
            });
            //Setting the list adapter
            recipeListView.setAdapter(ingredientAdapter);
            //Generating a layout and dividers for the list
            recipeListView.setLayoutManager(new LinearLayoutManager(this));
            recipeListView.addItemDecoration(new DividerItemDecoration(getApplicationContext(),
                    DividerItemDecoration.VERTICAL));
        } else {
            IngredientAdapter neededIngredientsAdapter = new IngredientAdapter(newIngList);
            //Setting the list adapter
            recipeListView.setAdapter(neededIngredientsAdapter);
            //Generating a layout and dividers for the list
            recipeListView.setLayoutManager(new LinearLayoutManager(this));
            recipeListView.addItemDecoration(new DividerItemDecoration(getApplicationContext(),
                    DividerItemDecoration.VERTICAL));
            recipeListView.setBackgroundColor(Color.RED);
            RecyclerView ownedIngredientsView = findViewById(R.id.cookable_list_ingredients);
            IngredientAdapter ingredientAdapter = new IngredientAdapter(oldIngList);
            //Setting the list adapter
            ownedIngredientsView.setAdapter(ingredientAdapter);
            //Generating a layout and dividers for the list
            ownedIngredientsView.setLayoutManager(new LinearLayoutManager(this));
            ownedIngredientsView.addItemDecoration(new DividerItemDecoration(getApplicationContext(),
                    DividerItemDecoration.VERTICAL));
            cook.setVisibility(View.INVISIBLE);
        }


        //Add Ingredient button that generates a small popup menu
        addIngredientButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //inflater pulls a layout resource
                LayoutInflater inflater = (LayoutInflater) getApplicationContext().
                        getSystemService(LAYOUT_INFLATER_SERVICE);
                //Assigning the layout/UI to the popup window
                View popupView = inflater.inflate(R.layout.popup_add_ingredient,null);

                //Initialise Popup with the layout view
                final PopupWindow popup = new PopupWindow(
                        popupView,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                //Allow text boxes to be clicked on
                popup.setFocusable(true);
                //Small UI thing that makes the popup stick out
                if(Build.VERSION.SDK_INT>=21){
                    popup.setElevation(5.0f);
                }

                //Spinner object (Dropdown menu)
                final Spinner spInventory = (Spinner) popupView.findViewById(R.id.spinner_inventory);
                /*Giving the spinner a list of ingredients the user currently has.
                 * The user can click on one to quickly add an ingredient
                 */
                final ArrayAdapter<Ingredient> adapter = new ArrayAdapter<>(getApplicationContext(),
                        android.R.layout.simple_spinner_item,ingList);
                //UI element for a drop down item
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spInventory.setAdapter(adapter);

                //Initialise UI elements for the popup
                final TextInputEditText textName = (TextInputEditText) popupView.findViewById(R.id.text_name);
                final EditText textAmount = (EditText) popupView.findViewById(R.id.text_number);
                Button confirmIngredientButton = (Button) popupView.findViewById(R.id.button_confirm_ingredient);

                //When an item is clicked on the spinner it adds the name to the name text box
                spInventory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        textName.setText(adapterView.getItemAtPosition(i).toString());
                    }

                    //Currently does nothing when nothing is selected
                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) { }
                });

                //Confirming the ingredient adds the ingredient to the recipe ingredient list
                confirmIngredientButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //New Ingredient object
                        Ingredient ingredient = new Ingredient();
                        //Assign attributes
                        ingredient.setName(textName.getText().toString());
                        ingredient.setNumber(textAmount.getText().toString());
                        //Today's date + 3 days
                        boolean found = false;
                        for (Ingredient ing : ingList) {
                            if (ing.getName().equals(textName.getText().toString())) {
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            mDb.execSQL("Insert into 'Ingredients'(name, best_before, num) VALUES('" + textName.getText().toString() + "','0000-03-00','0')");
                        }
                        Cursor cursor = mDb.rawQuery("SELECT ing_id FROM Ingredients WHERE name='"+ingredient.getName()+"'",null);
                        cursor.moveToPosition(0);
                        int count = cursor.getInt(cursor.getColumnIndex("ing_id"));
                        ingredient.setId(count);
                        System.out.println(count);
                        recipe.addIngredient(ingredient);
                        ingList.add(ingredient);

                        mDb.execSQL("INSERT INTO 'RecipeIngredients' (recipe_id,ing_id,measurement,detail) " +
                                "VALUES ('"+recipe.getId()+"','"+ingredient.getId()+"','"+textAmount.getText()+"','detail')");
//                                Insert into 'Ingredients'(name, best_before, num) VALUES('" + textName.getText().toString() + "','0000-03-00','0')");

                        //Add to the list
                        //Notifying the adapter means the list UI can be updated to show the new ingredient
                        ingredientAdapter.notifyItemInserted(ingList.size());
                        //Popup can now disappear
                        popup.dismiss();
                    }
                });
                //Show popup at the middle of the screen
                popup.showAtLocation(view, Gravity.CENTER, 0, 0);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.recipe_options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            //Deletes Recipe from Ingredients
            case R.id.action_delete:

                mDb.execSQL("DELETE FROM Recipes WHERE recipe_id = " + recipe.getId() + ";");
                mDb.execSQL("DELETE FROM RecipeIngredients WHERE recipe_id = " + recipe.getId() + ";");

                //Go back to MainActivity
                finish();

                return true;

            default:
                finish(); // Bodge
                return super.onOptionsItemSelected(item);

        }
    }


}
