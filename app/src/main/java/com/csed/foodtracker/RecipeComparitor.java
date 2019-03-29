package com.csed.foodtracker;

import java.util.Comparator;

public class RecipeComparitor implements Comparator<Recipe> {
    @Override
    public int compare(Recipe o1, Recipe o2) {
        if (o1.getFavourite() == 0) {
            if (o2.getFavourite() == 1) {
                return 1;
            } else {
                return 0;
            }
        } else {
            if (o2.getFavourite() == 1) {
                return 0;
            } else {
                return -1;
            }
        }
    }
}
