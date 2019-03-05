package com.csed.foodtracker;

import java.util.ArrayList;
import java.util.Date;

public class Recipe {

    private int id;
    private String name;
    private String description;
    private String image;
    private String prepTime;
    private int calories;
    private String url;
    private ArrayList<Ingredient> ingredients;

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

}
