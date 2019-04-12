package com.csed.foodtracker;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

public class AddRecipeActivity extends AppCompatActivity {

    DatabaseHelper mDBHelper;
    SQLiteDatabase mDb;
    Recipe recipe = new Recipe();
    int GALLERY = 1;
    Uri contentURI;
    ImageView imageView;
    Button removeImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        String themeVal;
        SharedPreferences themePrefs;
        themePrefs = getSharedPreferences("com.csed.foodtracker.theme", 0);
        themeVal = themePrefs.getString("theme", "1");
        if (themeVal.equals("1")) {
            setTheme(R.style.AppTheme);
        } else {
            setTheme(R.style.AppThemeDark);
        }
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

        setContentView(R.layout.activity_add_recipe);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Passing the ingredients the user currently has ,receiving the intent that was passed on.
        final ArrayList<Ingredient> ingredientList = (ArrayList<Ingredient>)
                getIntent().getSerializableExtra("ingredientList");

        //The Ingredients that belong to a recipe
        final ArrayList<Ingredient> recipeIngredientList = new ArrayList<>();

        /*Each recipe has a list of ingredients, this is just another list view that will contain
        * ingredients the user adds
        */
        RecyclerView listIngredients = (RecyclerView) findViewById(R.id.list_ingredients);
        //A new custom adapter class that can parse the Ingredient object to the list
        final IngredientAdapter ingAdapter = new IngredientAdapter(recipeIngredientList, false);
        listIngredients.setAdapter(ingAdapter);
        listIngredients.setLayoutManager(new LinearLayoutManager(this));
        listIngredients.addItemDecoration(new DividerItemDecoration(getApplicationContext(),
                DividerItemDecoration.VERTICAL));

        //Initialise UI elements for the popup
        final TextInputEditText textName =  findViewById(R.id.text_name);
        final EditText description =  findViewById(R.id.text_description); //If it doesn't recognise text_description don't worry, just reload IDE do not replace with text_desc
        final EditText prepTime = findViewById(R.id.text_preptime);
        final EditText calories = findViewById(R.id.text_calories);
        final EditText url = findViewById(R.id.text_url);
        final Button uploadImage = findViewById(R.id.addImageButton);
        removeImage = findViewById(R.id.removeImageButton);
        removeImage.setVisibility(View.GONE);
        imageView = findViewById(R.id.image);
        imageView.setVisibility(View.GONE);
        uploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, GALLERY);
                Toast.makeText(AddRecipeActivity.this, "Photo Uploaded", Toast.LENGTH_SHORT).show();
            }
        });
        removeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageView.setImageBitmap(null);
                contentURI = null;
                removeImage.setVisibility(View.GONE);
                imageView.setVisibility(View.GONE);
            }
        });
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (recipe.getIngredients().size() > 0 && textName.getText().length() > 0) {
                    // Insert recipe to the respective table
                    String name = textName.getText().toString();
                    String newName = name.substring(0, 1).toUpperCase() + name.substring(1); // Should capitalise the first letter
                    if (contentURI != null) {
                        mDb.execSQL("Insert into 'Recipes'(name, description, image, prep_time, calories, url) VALUES('"
                                + newName + "','"
                                + description.getText().toString() + "','"
                                + contentURI.toString() + "','"
                                + prepTime.getText().toString() + "','"
                                + calories.getText().toString() + "','"
                                + url.getText().toString() + "')");
                    } else {
                        mDb.execSQL("Insert into 'Recipes'(name, description, prep_time, calories, url) VALUES('"
                                + newName + "','"
                                + description.getText().toString() + "','"
                                + prepTime.getText().toString() + "','"
                                + calories.getText().toString() + "','"
                                + url.getText().toString() + "')");
                    }

                    recipe.setName(newName);

                    Cursor cursor = mDb.rawQuery("SELECT recipe_id FROM Recipes WHERE name='" + recipe.getName() + "'", null);
                    cursor.moveToPosition(0);
                    int count = cursor.getInt(cursor.getColumnIndex("recipe_id"));
                    recipe.setId(count);
                    // Add each item in listIngredietns to the recipeingredients table
                    for (Ingredient ing : recipe.getIngredients()) {
                        mDb.execSQL("Insert into 'RecipeIngredients'(recipe_id, ing_id, measurement, detail)VALUES('"
                                + recipe.getId() + "','"
                                + ing.getId() + "','"
                                + ing.getNumber() + "','detail')");
                    }
                    cursor.close();
                    Toast.makeText(view.getContext(), "Recipe Added", Toast.LENGTH_SHORT).show();

                    //Go back to MainActivity
                    finish();
                } else {
                    Toast.makeText(AddRecipeActivity.this, "Missing some values", Toast.LENGTH_SHORT).show();
                }
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        final Context context = this;
        //Add Ingredient button that generates a small popup menu
        Button addIngredientButton = (Button) findViewById(R.id.button_add_ingredient);
        addIngredientButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //inflater pulls a layout resource
                final LayoutInflater inflater = (LayoutInflater) getApplicationContext().
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
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    popup.setElevation(5.0f);
                }

                //Spinner object (Dropdown menu)
                final Spinner spInventory = (Spinner) popupView.findViewById(R.id.spinner_inventory);
                /*Giving the spinner a list of ingredients the user currently has.
                 * The user can click on one to quickly add an ingredient
                 */
                ArrayAdapter<Ingredient> adapter = new ArrayAdapter<>(getApplicationContext(),
                        android.R.layout.simple_spinner_item,ingredientList);
                //UI element for a drop down item
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spInventory.setAdapter(adapter);

                //Initialise UI elements for the popup
                final TextInputEditText textName = (TextInputEditText) popupView.findViewById(R.id.text_name);
                final EditText textAmount = (EditText) popupView.findViewById(R.id.text_number);
                final Spinner spinner = (Spinner) popupView.findViewById(R.id.unit_spinner);
                // Create an ArrayAdapter using the string array and a default spinner layout
                ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(context,
                        R.array.unit_options, android.R.layout.simple_spinner_item);
                // Specify the layout to use when the list of choices appears
                adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                // Apply the adapter to the spinner
                spinner.setAdapter(adapter1);
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
                        for (Ingredient ing : ingredientList) {
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
                        ingredient.setUnits(unit);
                        int count = cursor.getInt(cursor.getColumnIndex("ing_id"));
                        ingredient.setId(count);
                        boolean find = false;
                        int c = -1;
                        int d = -1;
                        for (int i = 0; i < recipe.getIngredients().size(); i ++) {
                            Ingredient ing = recipe.getIngredients().get(i);
                            if (ing.getName().equals(ingredient.getName())) {
                                find = true;
                                c = i;
                                break;
                            }
                        }
                        for (int i = 0; i < recipeIngredientList.size(); i ++) {
                            Ingredient ing = recipeIngredientList.get(i);
                            if (ing.getName().equals(ingredient.getName())) {
                                find = true;
                                d = i;
                            }
                        }
                        if (find) {
                            int newCount;
                            try {
                                int oldCount = Integer.parseInt(recipe.getIngredients().get(c).getNumber());
                                newCount = oldCount + Integer.parseInt(ingredient.getNumber());
                            } catch (NumberFormatException e) {
                                newCount = 2147483647; // Max number to stop overflow
                            }
                            recipe.getIngredients().get(c).setNumber(String.valueOf(newCount));
                            recipeIngredientList.get(d).setNumber(String.valueOf(newCount));
                            ingAdapter.notifyItemChanged(d);
                        } else {
                            recipe.addIngredient(ingredient);
                            recipeIngredientList.add(ingredient);
                            ingAdapter.notifyItemInserted(recipeIngredientList.size());
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
        if (resultCode == RESULT_CANCELED) {
            return;
        }
        if (requestCode == GALLERY) {
            if (data != null) {
                contentURI = data.getData();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), contentURI);
                    imageView.setVisibility(View.VISIBLE);
                    imageView.setImageBitmap(bitmap);
                    Toast.makeText(AddRecipeActivity.this, "Image Saved!", Toast.LENGTH_SHORT).show();
                    removeImage.setVisibility(View.VISIBLE);
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(AddRecipeActivity.this, "There has been an error, please try again soon!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
