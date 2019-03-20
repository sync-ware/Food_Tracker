package com.csed.foodtracker;

import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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
import android.widget.PopupWindow;
import android.widget.Spinner;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class AddRecipeActivity extends AppCompatActivity {

    protected SQLiteDatabase mDb;
    protected DatabaseHelper mDBHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
        final IngredientAdapter ingAdapter = new IngredientAdapter(recipeIngredientList);
        listIngredients.setAdapter(ingAdapter);
        listIngredients.setLayoutManager(new LinearLayoutManager(this));
        listIngredients.addItemDecoration(new DividerItemDecoration(getApplicationContext(),
                DividerItemDecoration.VERTICAL));

        //TODO: Button for adding the recipe to the database
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


        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Recipe Added", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Add Ingredient button that generates a small popup menu
        Button addIngredientButton = (Button) findViewById(R.id.button_add_ingredient);
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
                ArrayAdapter<Ingredient> adapter = new ArrayAdapter<>(getApplicationContext(),
                        android.R.layout.simple_spinner_item,ingredientList);
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
                        //TODO: Needs to update the list of ingredients straight after this, so the user doesn't need to go back to the home screen
                        //New Ingredient object
                        Ingredient ingredient = new Ingredient();
                        //Assign attributes
                        ingredient.setName(textName.getText().toString());
                        ingredient.setNumber(textAmount.getText().toString());
                        //Today's date + 3 days
                        SimpleDateFormat dtf = new SimpleDateFormat("yyyy-MM-dd");
                        Date date = new Date();
                        System.out.println(dtf.format(date));
                        //TODO: Make sure it's reading from the database
                        mDb.execSQL("Insert into 'Ingredients'(name, best_before, num) VALUES('"+textName.getText().toString()+"','"+dtf.format(date)+"','"+textAmount.getText().toString()+"')");
                        //Add to the list
                        recipeIngredientList.add(ingredient);
                        //Notifying the adapter means the list UI can be updated to show the new ingredient
                        ingAdapter.notifyItemInserted(recipeIngredientList.size());
                        //Popup can now disappear
                        popup.dismiss();
                    }
                });

                //Show popup at the middle of the screen
                popup.showAtLocation(view, Gravity.CENTER, 0, 0);


            }
        });

    }

}
