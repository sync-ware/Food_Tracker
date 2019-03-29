package com.csed.foodtracker;

import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

public class AddIngredientActivity extends AppCompatActivity {

    protected SQLiteDatabase mDb;
    protected DatabaseHelper mDBHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_ingredient);



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

        //Initialise UI elements for the popup
        final TextInputEditText textName =  findViewById(R.id.input_name);
        final EditText textAmount =  findViewById(R.id.input_amount);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Ingredient Added", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                System.out.println(textName.getText().toString());
                mDb.execSQL("Insert into 'Ingredients'(name, best_before, num) VALUES('"+textName.getText().toString()+"','0000-03-00','"+textAmount.getText().toString()+"')");
//                mDb.execSQL("Insert into 'Ingredients'(name, best_before, num) VALUES('"+"hi"+"','"+dtf.format(date)+"','"+"2"+"')");

            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
