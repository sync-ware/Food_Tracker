package com.csed.foodtracker;

import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class IngredientsActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private List<Ingredient> ingredientList;
    private DatabaseHelper mDBHelper;
    private SQLiteDatabase mDb;

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
        setContentView(R.layout.activity_ingredients);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ingredientList = (List<Ingredient>) getIntent().getSerializableExtra("ingredientList");
        checkIngredientList();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), AddIngredientActivity.class);
                startActivity(intent);

            }
        });

        List<Ingredient> availableIngredients = new ArrayList<>();


        for (Ingredient ing : ingredientList){
            if (ing.getNumber().equals("")) {
                Toast.makeText(IngredientsActivity.this, ing.getName(), Toast.LENGTH_SHORT).show();
                ing.setNumber("0");
            }
            if (Integer.parseInt(ing.getNumber()) > 0){
                availableIngredients.add(ing);
            }
        }

        IngredientAdapter adapter = new IngredientAdapter(availableIngredients);
        RecyclerView ingRecycler = (RecyclerView) findViewById(R.id.ingredients_recyclerview);
        ingRecycler.setAdapter(adapter);

        ingRecycler.setLayoutManager(new LinearLayoutManager(this));
        ingRecycler.addItemDecoration(new DividerItemDecoration(getApplicationContext(),
                DividerItemDecoration.VERTICAL));


    }

    /**
     * checkIngredientList calls another query to make sure that all ingredients are displayed, even ones which have just been added
     */
    private void checkIngredientList() {
        Cursor ingredientTable = mDb.rawQuery("SELECT Ingredients.ing_id, Ingredients.name, Ingredients.best_before," +
                "Ingredients.num FROM Ingredients ", null);

        ingredientTable.moveToPosition(0);
        while (ingredientTable.getPosition() < ingredientTable.getCount()) {
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id) {

            case R.id.nav_recipes:
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

}
