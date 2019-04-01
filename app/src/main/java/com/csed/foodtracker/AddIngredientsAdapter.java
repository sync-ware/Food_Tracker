package com.csed.foodtracker;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AddIngredientsAdapter extends ArrayAdapter<Ingredient> {

    private Context iContext;
    private List<Ingredient> ingredientList;
    DatabaseHelper mDBHelper;
    SQLiteDatabase mDb;
    View listItem;



    public AddIngredientsAdapter(@NonNull Context context, ArrayList<Ingredient> list) {
        super(context, 0 , list);
        iContext = context;
        ingredientList = list;
    }

    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        listItem = convertView;
        if(listItem == null) {
            listItem = LayoutInflater.from(iContext).inflate(R.layout.list_items, parent, false);
        }

        Ingredient currentIngredient = ingredientList.get(position);

        TextView text = (TextView) listItem.findViewById(R.id.Item_name);
        text.setText(currentIngredient.getName());

        EditText count = (EditText) listItem.findViewById(R.id.Item_price);
        currentIngredient.setNumber(count.getText().toString());

        return listItem;
    }

    public void storeData(Context context) {
        mDBHelper = new DatabaseHelper(context);

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
        System.out.println(ingredientList.size());
        for (int i = 0; i <ingredientList.size(); i ++) {
            Ingredient currentIngredient = ingredientList.get(i);
            EditText count = (EditText) listItem.findViewById(R.id.Item_price);
            currentIngredient.setNumber(count.getText().toString());
            System.out.println(count.getText().toString());//TODO:Make this work
            mDb.execSQL("Insert into 'Ingredients'(name, best_before, num) VALUES('"+ingredientList.get(i).getName()+"','0000-03-00','"+count.getText().toString()+"')");
            //TODO: Need to make sure ingredient doesn't exist before adding it to the database
        }
        // SQL goes here
    }
}
