package com.csed.foodtracker;

import java.io.Serializable;

/**
 * Ingredient Class that represents an individual Ingredient
 */

public class Ingredient implements Serializable {

    //Attributes
    private int id;
    private String name;
    private String bestBefore;
    private String number;
    private String units;

    public Ingredient(){}

    public void setId(int id){
        this.id = id;
    }

    public void setName(String name){
        this.name = name;
    }

    public void setUnits(String units){
        this.units = units;
    }

    public void setBestBefore(String bestBefore){
        this.bestBefore = bestBefore;
    }

    public void setNumber(String number){
        this.number = number;
    }

    public int getId(){
        return id;
    }

    public String getName(){
        return name;
    }

    public String getBestBefore(){
        return bestBefore;
    }

    public String getNumber(){
        return number;
    }

    public String getUnits(){
        return units;
    }

    @Override
    public String toString(){
        return name;
    }
}
