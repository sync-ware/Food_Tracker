package com.csed.foodtracker;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Recipe class that represents an individual recipe.
 */

public class Recipe implements Serializable {

    //Attributes
    private int id;
    private String name;
    private String description;
    private String image;
    private String prepTime;
    private int calories;
    private String url;
    private ArrayList<Ingredient> ingredients;
    private int favourite = 0;

    public Recipe(){
        ingredients = new ArrayList<>();
    }

    public void setId(int id){
        this.id = id;
    }

    public void setName(String name){
        this.name = name;
    }

    public void setDescription(String description){
        this.description = description;
    }

    public void setImage(String image){
        this.image = image;
    }

    public void setPrepTime(String prepTime){
        this.prepTime = prepTime;
    }

    public void setCalories(int calories){
        this.calories = calories;
    }

    public void setUrl(String url){
        this.url = url;
    }

    public void addIngredient(Ingredient ingredient){
        ingredients.add(ingredient);
    }

    public int getId(){
        return id;
    }

    public String getName(){
        return name;
    }

    public String getDescription(){
        return description;
    }

    public String getPrepTime(){
        return prepTime;
    }

    public int getCalories(){
        return calories;
    }

    public String getUrl(){
        return url;
    }

    public ArrayList<Ingredient> getIngredients() {
        return ingredients;
    }

    public void setFavourite(int fav) {
        favourite = fav;
    }

    public int getFavourite() {
        return  favourite;
    }

}
