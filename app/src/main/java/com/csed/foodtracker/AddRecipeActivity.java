package com.csed.foodtracker;

import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
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
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import java.util.ArrayList;

public class AddRecipeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_recipe);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Passing the ingredients the user currently has
        final ArrayList<Ingredient> ingredientList = (ArrayList<Ingredient>)
                getIntent().getSerializableExtra("ingredientList");

        //The Ingredients that belong to a recipe
        final ArrayList<Ingredient> recipeIngredientList = new ArrayList<>();

        RecyclerView listIngredients = (RecyclerView) findViewById(R.id.list_ingredients);
        final IngredientAdapter ingAdapter = new IngredientAdapter(recipeIngredientList);
        listIngredients.setAdapter(ingAdapter);
        listIngredients.setLayoutManager(new LinearLayoutManager(this));
        listIngredients.addItemDecoration(new DividerItemDecoration(getApplicationContext(),
                DividerItemDecoration.VERTICAL));

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Recipe Added", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Button addIngredientButton = (Button) findViewById(R.id.button_add_ingredient);
        addIngredientButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                LayoutInflater inflater = (LayoutInflater) getApplicationContext().
                        getSystemService(LAYOUT_INFLATER_SERVICE);
                View popupView = inflater.inflate(R.layout.popup_add_ingredient,null);

                final PopupWindow popup = new PopupWindow(
                        popupView,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                popup.setFocusable(true);
                if(Build.VERSION.SDK_INT>=21){
                    popup.setElevation(5.0f);
                }

                final Spinner spInventory = (Spinner) popupView.findViewById(R.id.spinner_inventory);
                ArrayAdapter<Ingredient> adapter = new ArrayAdapter<>(getApplicationContext(),
                        android.R.layout.simple_spinner_item,ingredientList);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spInventory.setAdapter(adapter);

                final TextInputEditText textName = (TextInputEditText) popupView.findViewById(R.id.text_name);
                final EditText textAmount = (EditText) popupView.findViewById(R.id.text_number);

                Button addButton = (Button) popupView.findViewById(R.id.button_confirm_ingredient);

                spInventory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        textName.setText(adapterView.getItemAtPosition(i).toString());


                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {

                    }
                });

                addButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Ingredient ingredient = new Ingredient();
                        ingredient.setName(textName.getText().toString());
                        ingredient.setNumber(textAmount.getText().toString());
                        recipeIngredientList.add(ingredient);
                        ingAdapter.notifyItemInserted(recipeIngredientList.size());
                        popup.dismiss();
                    }
                });

                popup.showAtLocation(view, Gravity.CENTER, 0, 0);


            }
        });

    }

}
