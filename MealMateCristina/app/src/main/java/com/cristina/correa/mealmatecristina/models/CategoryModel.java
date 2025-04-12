package com.cristina.correa.mealmatecristina.models;

/**
 * A model class representing a category with a name and a drawable resource identifier.
 * This class is used to store and manage category data.
 *
 * @author Cristina Correa
 * @since 1.0
 */
public class CategoryModel {
    private String name;
    private String drawableRes;

    public CategoryModel() {
    }

    public CategoryModel(String name, String drawableRes) {
        this.name = name;
        this.drawableRes = drawableRes;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDrawableRes() {
        return drawableRes;
    }

    public void setDrawableRes(String drawableRes) {
        this.drawableRes = drawableRes;
    }
}
