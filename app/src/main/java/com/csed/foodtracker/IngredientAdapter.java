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
    private static ClickListener clickListener;
    private boolean runWithView;

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        public TextView nameTextView;

        public ViewHolder(View itemView){
            super(itemView);

            nameTextView = (TextView) itemView.findViewById(R.id.recipe_name);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (runWithView) {
                clickListener.onItemClick(getAdapterPosition(), v);
            }
        }

    }

    private List<Ingredient> mRecipes;

    public IngredientAdapter(List<Ingredient> recipes, boolean runView){
        mRecipes = recipes;
        runWithView = runView;
    }

    public void setOnItemClickListener(ClickListener clickListener) {
        IngredientAdapter.clickListener = clickListener;
    }

    public interface ClickListener {
        void onItemClick(int position, View v);
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
        StringBuilder setText = new StringBuilder(Ingredient.getNumber());
        if (Ingredient.getUnits().equals("g") || Ingredient.getUnits().equals("ml")) {
            setText.append(Ingredient.getUnits());
            setText.append(" of ");
        } else {
            setText.append(" ");
            if (!Ingredient.getUnits().equals("Whole")) {
                setText.append(Ingredient.getUnits());
                setText.append(" of ");
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
