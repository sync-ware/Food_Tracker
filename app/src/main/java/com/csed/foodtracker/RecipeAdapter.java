package com.csed.foodtracker;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;

// Code adapted from https://guides.codepath.com/android/using-the-recyclerview

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.ViewHolder> {
    public Resources res;
    MainActivity context;
    String themeVal;

    private static ClickListener clickListener;

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        public TextView nameTextView;
        public Button favouriteButton;



        public ViewHolder(View itemView){
            super(itemView);
            favouriteButton = (Button) itemView.findViewById(R.id.favourite_button);
            nameTextView = (TextView) itemView.findViewById(R.id.recipe_name);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            clickListener.onItemClick(getAdapterPosition(), v);
        }
    }

    public void setOnItemClickListener(ClickListener clickListener) {
        RecipeAdapter.clickListener = clickListener;
    }

    public interface ClickListener {
        void onItemClick(int position, View v);
    }

    private List<Recipe> mRecipes;

    public RecipeAdapter(List<Recipe> recipes, Resources re, MainActivity con, String theme){
        mRecipes = recipes;
        res = re;
        context = con;
        themeVal = theme;
    }

    // Usually involves inflating a layout from XML and returning the holder
    @Override
    public RecipeAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View recipeView = inflater.inflate(R.layout.item_recipe, parent, false);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(recipeView);
        return viewHolder;
    }

    // Involves populating data into the item through holder
    @Override
    public void onBindViewHolder(RecipeAdapter.ViewHolder viewHolder, final int position) {
        // Get the data model based on position
        final Recipe recipe = mRecipes.get(position);
        final SQLiteDatabase mDb;
        DatabaseHelper mDBHelper = new DatabaseHelper(context.getApplicationContext());

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
        // Set item views based on your views and data model
        TextView textView = viewHolder.nameTextView;
        textView.setText(recipe.getName());
        final Button favouriteButton = viewHolder.favouriteButton;
        final Drawable notFav = ResourcesCompat.getDrawable(res, R.drawable.not_favourite, null);
        final Drawable notFavDark =  ResourcesCompat.getDrawable(res, R.drawable.not_favourite_dark, null);
        final Drawable fav = ResourcesCompat.getDrawable(res, R.drawable.favourite, null);
        if (recipe.getFavourite() == 1) {
            favouriteButton.setBackground(fav);
        } else {
            if (themeVal.equals("1")) {
                favouriteButton.setBackground(notFav);
            } else {
                favouriteButton.setBackground(notFavDark);
            }
        }
        favouriteButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (recipe.getFavourite() == 0) {
                    favouriteButton.setBackground(fav);
                    recipe.setFavourite(1);
                    mDb.execSQL("UPDATE Recipes SET favourite = 1 WHERE name = '"+recipe.getName()+"'");
                    Toast.makeText(context.getApplicationContext(),"Added "+recipe.getName()+" to favourites!",Toast.LENGTH_SHORT).show();
                    context.initialiseListUI();
                } else {
                    if (themeVal.equals("1")) {
                        favouriteButton.setBackground(notFav);
                    } else {
                        favouriteButton.setBackground(notFavDark);
                    }
                    recipe.setFavourite(0);
                    mDb.execSQL("UPDATE Recipes SET favourite = 0 WHERE name = '"+recipe.getName()+"'");
                    Toast.makeText(context.getApplicationContext(),"Removed "+recipe.getName()+" from favourites!",Toast.LENGTH_SHORT).show();
                    context.initialiseListUI();
                }
            }
        });
//        Collections.swap(this.mListItems, oldIndex, index); notifyItemMoved(oldIndex, newIndex)

    }

    // Returns the total count of items in the list
    @Override
    public int getItemCount() {
        return mRecipes.size();
    }

}
