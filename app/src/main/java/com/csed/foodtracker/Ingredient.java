package com.csed.foodtracker;

import java.io.Serializable;

public class Ingredient implements Serializable {

    private int id;
    private String name;
    private String bestBefore;
    private String number;

    public Ingredient(){}

    public void setId(int id){
        this.id = id;
    }

    public void setName(String name){
        this.name = name;
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

    @Override
    public String toString(){
        return name;
    }
}
