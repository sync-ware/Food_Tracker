package com.csed.foodtracker;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;

public class AddIngredientActivity extends AppCompatActivity {

    protected SQLiteDatabase mDb;
    protected DatabaseHelper mDBHelper;

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
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_add_ingredient);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        mDBHelper = new DatabaseHelper(this);

        try {
            mDBHelper.updateDataBase();
        } catch (IOException mIOException) {
            throw new Error("UnableToUpdateDatabase");
        }

        try {
            mDb = mDBHelper.getWritableDatabase();

        } catch (SQLException mSQLException) {
//            throw mSQLException;
        }

        //Initialise UI elements for the popup
        final TextInputEditText textName =  findViewById(R.id.input_name);
        final EditText textAmount =  findViewById(R.id.input_amount);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!textName.getText().toString().equals("")) {
                    if (textAmount.getText().toString().equals("")) {
                        textAmount.setText("0");
                    }
                    String name = textName.getText().toString();
                    String newName = name.substring(0, 1).toUpperCase() + name.substring(1); // Should capitalise the first letter

                    Snackbar.make(view, "Ingredient Added", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    Cursor cursor = mDb.rawQuery("SELECT num FROM Ingredients WHERE name='" + newName + "'", null);
                    if (cursor.getCount() == 0) { // Insert if not exists
                        mDb.execSQL("Insert into 'Ingredients'(name, best_before, num) VALUES('" + newName + "','0000-03-00','" + textAmount.getText().toString() + "')");
                    } else {
                        cursor.moveToPosition(0);
                        int count = cursor.getInt(cursor.getColumnIndex("num")); // If this returns anything then it's fine
                        cursor.close();
                        int value = count + Integer.parseInt(textAmount.getText().toString());
                        mDb.execSQL("UPDATE Ingredients SET num =" + value + " WHERE name = '" + newName + "'"); // Should increase the value
                    }
                    finish();
                } else {
                    Toast.makeText(AddIngredientActivity.this, "Enter a name!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
}
