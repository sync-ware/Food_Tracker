package com.csed.foodtracker;

import java.util.Date;

public class Recipe {

    private int id;
    private String name;
    private String description;
    private String image;
    private Date prepTime;
    private int calories;
    private String url;

    public Recipe(){}

    public void setId(int id){
        this.id = id;
    }

    public void setName(String name){
        this.name = name;
    }

    public void setDescription(String description){
        this.description = description;
    }

    public void setImage(){

    }

    public void setPrepTime(Date prepTime){
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

    public Date getPrepTime(){
        return prepTime;
    }

    public int getCalories(){
        return calories;
    }

    public String getUrl(){
        return url;
    }
}
