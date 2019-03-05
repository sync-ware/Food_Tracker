package com.csed.foodtracker;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

// Code adapted from https://guides.codepath.com/android/using-the-recyclerview

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.ViewHolder> {
    public class ViewHolder extends RecyclerView.ViewHolder{

        public TextView nameTextView;

        public ViewHolder(View itemView){
            super(itemView);

            nameTextView = (TextView) itemView.findViewById(R.id.recipe_name);
        }
    }

    private List<Ingredient> mRecipes;

    public RecipeAdapter(List<Ingredient> recipes){
        mRecipes = recipes;
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
    public void onBindViewHolder(RecipeAdapter.ViewHolder viewHolder, int position) {
        // Get the data model based on position
        Ingredient Ingredient = mRecipes.get(position);

        // Set item views based on your views and data model
        TextView textView = viewHolder.nameTextView;
        textView.setText(Ingredient.getName());

    }

    // Returns the total count of items in the list
    @Override
    public int getItemCount() {
        return mRecipes.size();
    }

}
