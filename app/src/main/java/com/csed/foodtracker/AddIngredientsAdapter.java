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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AddIngredientsAdapter extends ArrayAdapter<Ingredient> {

    private Context iContext;
    private List<Ingredient> ingredientList;
    DatabaseHelper mDBHelper;
    SQLiteDatabase mDb;



    public AddIngredientsAdapter(@NonNull Context context, ArrayList<Ingredient> list) {
        super(context, 0 , list);
        iContext = context;
        ingredientList = list;
    }


    class ListViewHolder {
        TextView itemName;
        EditText count;
        int id;

        ListViewHolder(View v) {
            itemName = (TextView) v.findViewById(R.id.Item_name);
            count = (EditText) v.findViewById(R.id.Item_price);
        }
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ListViewHolder viewHolder;
        if (view == null) {
            view = LayoutInflater.from(iContext).inflate(R.layout.list_items, parent, false);
            viewHolder = new ListViewHolder(view);
            viewHolder.itemName = (TextView) view.findViewById(R.id.Item_name);
            viewHolder.count = (EditText) view.findViewById(R.id.Item_price);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ListViewHolder) view.getTag();
// loadSavedValues();
        }
        viewHolder.itemName.setText(ingredientList.get(position).getName());
        viewHolder.count.setId(position);
        viewHolder.id = position;
        if (ingredientList != null && ingredientList.get(position) != null) {
            viewHolder.count.setText(ingredientList.get(position).getNumber());
        } else {
            viewHolder.count.setText("Hm");
        }
// Add listener for edit text
        viewHolder.count
                .setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        /*
                         * When focus is lost save the entered value for
                         * later use
                         */
                        if (!hasFocus) {
                            int itemIndex = v.getId();
                            String enteredPrice = ((EditText) v).getText()
                                    .toString();
                            ingredientList.get(itemIndex).setNumber(enteredPrice);
                        }
                    }
                });
        return view;
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
            //TODO: Doesn't reload until it restarts here. Add to universal ingredient list?
            mDb.execSQL("Insert into 'Ingredients'(name, best_before, num) VALUES('"+ingredientList.get(i).getName()+"','0000-03-00','"+ingredientList.get(i).getNumber()+"')");
            //TODO: Need to make sure ingredient doesn't exist before adding it to the database
        }
        // SQL goes here
    }
}
