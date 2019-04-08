package com.csed.foodtracker;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
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
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import java.io.FileNotFoundException;
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
    int GALLERY = 1;
    ImageView imageView;
    Uri contentURI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mDBHelper = new DatabaseHelper(this);
        final String themeVal;
        SharedPreferences themePrefs;
        themePrefs = getSharedPreferences("com.csed.foodtracker.theme", 0);
        themeVal = themePrefs.getString("theme", "1");
        if (themeVal.equals("1")) {
            setTheme(R.style.AppTheme);
        } else {
            setTheme(R.style.AppThemeDark);
        }

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

        setContentView(R.layout.activity_view_recipe);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Retrieve Recipe from intent that was passed on to this activity
        recipe = (Recipe) getIntent().getSerializableExtra("recipe");

        /*Initialise UI elements, all are disable at first and cannot be edited
        * They are assigned the recipes attributes respectively
        */

        imageView = (ImageView) findViewById(R.id.image);
        //TODO: Actually request the permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permission not granted, using default image", Toast.LENGTH_SHORT).show();
            // Permission is not granted
        } else {
            try {
                Uri uri = Uri.parse(recipe.getImage());
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                imageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                Toast.makeText(this, "No image found, using default", Toast.LENGTH_SHORT).show();
            }
        }
        final Button uploadImage = (Button) findViewById(R.id.addImageButton);
        uploadImage.setEnabled(false);
        uploadImage.setVisibility(View.GONE);
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
        final FloatingActionButton cancel = findViewById(R.id.cancel);
        final Drawable notFav = ResourcesCompat.getDrawable(getResources(), R.drawable.not_favourite, null);
        final Drawable notFavDark = ResourcesCompat.getDrawable(getResources(), R.drawable.not_favourite_dark, null);
        final Drawable fav = ResourcesCompat.getDrawable(getResources(), R.drawable.favourite, null);

        favouriteSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    favouriteSwitch.setThumbDrawable(fav);
                } else {
                    if (themeVal.equals("1")) {
                        favouriteSwitch.setThumbDrawable(notFav);
                    } else {
                        favouriteSwitch.setThumbDrawable(notFavDark);
                    }
                }
            }
        });

        if (recipe.getFavourite() == 1) {
            favouriteSwitch.setChecked(true);
            favouriteSwitch.setThumbDrawable(fav);
        } else {
            favouriteSwitch.setChecked(false);
            if (themeVal.equals("1")) {
                favouriteSwitch.setThumbDrawable(notFav);
            } else {
                favouriteSwitch.setThumbDrawable(notFavDark);
            }
        }

        uploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, GALLERY);
                Toast.makeText(ViewRecipeActivity.this, "Photo Uploaded", Toast.LENGTH_SHORT).show();
            }
        });

        Cursor cursor = mDb.rawQuery("SELECT Ingredients.name, RecipeIngredients.measurement, Ingredients.units" +
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
            String unit = cursor.getString(cursor.getColumnIndex("units"));
            Ingredient ingredient = new Ingredient();
            ingredient.setName(name);
            ingredient.setNumber(measurement);
            ingredient.setUnits(unit);
            ingList.add(ingredient);
            cursor.moveToNext();
        }
        cursor.close();
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
                    cancel.setVisibility(View.VISIBLE);
                    cancel.setEnabled(true);
                    uploadImage.setEnabled(true);
                    uploadImage.setVisibility(View.VISIBLE);
                }
                else {
                    if (ingList.size() > 0) {
                        int mode = 0;
                        boolean favourite = favouriteSwitch.isChecked();
                        if (favourite) {
                            mode = 1;
                        }
                        mDb.execSQL("UPDATE Recipes SET name = '" + recipeName.getText() + "', description = '"
                                + recipeDesc.getText() + "', image = '"+ contentURI.toString() + "', prep_time = '" + recipePrepTime.getText() + "', calories = "
                                + recipeCalories.getText() + ", url = '" + recipeUrl.getText() + "', favourite = '" + mode + "' WHERE recipe_id = "
                                + recipe.getId());
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
                        cancel.setVisibility(View.GONE);
                        cancel.setEnabled(false);
                        uploadImage.setEnabled(false);
                        uploadImage.setVisibility(View.GONE);
                        finish();
                    } else {
                        Toast.makeText(ViewRecipeActivity.this, "A Recipe Needs Ingredients!", Toast.LENGTH_SHORT).show();
                    }
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
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(ViewRecipeActivity.this, "Recipe Not Updated", Toast.LENGTH_SHORT).show();
                //TODO: Change text boxes back to what it was before
                fab.setImageResource(R.drawable.ic_edit);
                recipeName.setEnabled(false);
                recipeDesc.setEnabled(false);
                recipePrepTime.setEnabled(false);
                recipeCalories.setEnabled(false);
                recipeUrl.setEnabled(false);
                favouriteSwitch.setEnabled(false);
                addIngredientButton.setEnabled(false);
                addIngredientButton.setVisibility(View.GONE);
                cancel.setVisibility(View.GONE);
                cancel.setEnabled(false);
                uploadImage.setEnabled(false);
                uploadImage.setVisibility(View.GONE);
            }
        });
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
                    Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
// Vibrate for 500 milliseconds
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        v.vibrate(VibrationEffect.createOneShot(250, VibrationEffect.DEFAULT_AMPLITUDE));
                    } else {
                        //deprecated in API 26
                        v.vibrate(250);
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
        final Context context = this;

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
                popup.setElevation(5.0f);
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
                final Spinner spinner = (Spinner) popupView.findViewById(R.id.unit_spinner);
                // Create an ArrayAdapter using the string array and a default spinner layout
                ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(context,
                        R.array.unit_options, android.R.layout.simple_spinner_item);
                // Specify the layout to use when the list of choices appears
                adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                // Apply the adapter to the spinner
                spinner.setAdapter(adapter1);
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
                        String name = textName.getText().toString();
                        String newName = name.substring(0, 1).toUpperCase() + name.substring(1); // Should capitalise the first letter
                        ingredient.setName(newName);
                        String amount = textAmount.getText().toString();
                        String unit = spinner.getSelectedItem().toString();
                        if (amount.equals("")) {
                            amount = "1";
                        }
                        try {
                            Integer.parseInt(amount);
                        } catch (NumberFormatException e) {
                            amount = "2147483647";
                        }
                        ingredient.setNumber(amount);
                        //Today's date + 3 days
                        boolean found = false;
                        for (Ingredient ing : ingList) {
                            if (ing.getName().equals(newName)) {
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            mDb.execSQL("Insert into 'Ingredients'(name, best_before, num, units) VALUES('" + newName + "','0000-03-00','0','"+unit+"')");
                        }
                        Cursor cursor = mDb.rawQuery("SELECT ing_id FROM Ingredients WHERE name='"+ingredient.getName()+"'",null);
                        cursor.moveToPosition(0);
                        int count = cursor.getInt(cursor.getColumnIndex("ing_id"));
                        ingredient.setId(count);
                        ingredient.setUnits(unit);
                        boolean find = false;
                        int d = -1;
                        for (int i = 0; i < ingList.size(); i ++) {
                            Ingredient ing = ingList.get(i);
                            if (ing.getName().equals(ingredient.getName())) {
                                find = true;
                                d = i;
                                break;
                            }
                        }
                        if (find) {
                            int newCount;
                            try {
                                int oldCount = Integer.parseInt(ingList.get(d).getNumber());
                                newCount = oldCount + Integer.parseInt(ingredient.getNumber());
                            } catch (NumberFormatException e) {
                                newCount = 2147483647; // Max number to stop overflow
                            }
                            ingList.get(d).setNumber(String.valueOf(newCount));
                            mDb.execSQL("UPDATE 'RecipeIngredients' SET measurement = '"+String.valueOf(newCount)+"' WHERE ing_id = '"+ingredient.getId()+"'");
                            ingredientAdapter.notifyItemChanged(d);
                        } else {
                            recipe.addIngredient(ingredient);
                            ingList.add(ingredient);
                            mDb.execSQL("INSERT INTO 'RecipeIngredients' (recipe_id,ing_id,measurement,detail) " +
                                    "VALUES ('"+recipe.getId()+"','"+ingredient.getId()+"','"+textAmount.getText()+"','detail')");
//                                Insert into 'Ingredients'(name, best_before, num) VALUES('" + textName.getText().toString() + "','0000-03-00','0')");
                            ingredientAdapter.notifyItemInserted(ingList.size());
                        }

                        //Add to the list
                        //Notifying the adapter means the list UI can be updated to show the new ingredient
                        //Popup can now disappear
                        popup.dismiss();
                        cursor.close();
                    }
                });
                //Show popup at the middle of the screen
                popup.showAtLocation(view, Gravity.CENTER, 0, 0);
            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == this.RESULT_CANCELED) {
            return;
        }
        if (requestCode == GALLERY) {
            if (data != null) {
                contentURI = data.getData();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), contentURI);
                    Toast.makeText(ViewRecipeActivity.this, "Image Saved!", Toast.LENGTH_SHORT).show();


                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(ViewRecipeActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                }
            }
        }
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
