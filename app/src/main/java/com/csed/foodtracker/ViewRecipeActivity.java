package com.csed.foodtracker;

import android.net.Uri;
import android.os.Bundle;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;

public class ViewRecipeActivity extends AppCompatActivity {

    private Recipe recipe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_recipe_acticity);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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
                }
                else{
                    fab.setImageResource(R.drawable.ic_edit);
                    recipeName.setEnabled(false);
                    recipeDesc.setEnabled(false);
                    recipePrepTime.setEnabled(false);
                    recipeCalories.setEnabled(false);
                    recipeUrl.setEnabled(false);
                }
            }
        });
    }

}
