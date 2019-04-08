package com.csed.foodtracker;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

// Code adapted from https://guides.codepath.com/android/using-the-recyclerview

public class IngredientAdapter extends RecyclerView.Adapter<IngredientAdapter.ViewHolder> {
    public class ViewHolder extends RecyclerView.ViewHolder{

        public TextView nameTextView;

        public ViewHolder(View itemView){
            super(itemView);

            nameTextView = (TextView) itemView.findViewById(R.id.recipe_name);
        }
    }

    private List<Ingredient> mRecipes;

    public IngredientAdapter(List<Ingredient> recipes){
        mRecipes = recipes;
    }

    // Usually involves inflating a layout from XML and returning the holder
    @Override
    public IngredientAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View recipeView = inflater.inflate(R.layout.item_recipe_view, parent, false);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(recipeView);
        return viewHolder;
    }

    // Involves populating data into the item through holder
    @Override
    public void onBindViewHolder(IngredientAdapter.ViewHolder viewHolder, int position) {
        // Get the data model based on position
        Ingredient Ingredient = mRecipes.get(position);

        // Set item views based on your views and data model
        TextView textView = viewHolder.nameTextView;
        //TODO: Needs to be slightly different based on units
        StringBuilder setText = new StringBuilder(Ingredient.getNumber());
        if (Ingredient.getUnits().equals("g") || Ingredient.getUnits().equals("ml")) {
            setText.append(Ingredient.getUnits());
            setText.append(" of ");
        } else {
            setText.append(" ");
            setText.append(Ingredient.getUnits());
            setText.append(" ");
            if (!Ingredient.getUnits().equals("Whole")) {
                setText.append("of ");
            }
        }
        setText.append(Ingredient.getName());
        textView.setText(setText.toString());

    }

    // Returns the total count of items in the list
    @Override
    public int getItemCount() {
        return mRecipes.size();
    }

}
