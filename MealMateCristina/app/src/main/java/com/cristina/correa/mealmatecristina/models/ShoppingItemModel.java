package com.cristina.correa.mealmatecristina.models;

import java.io.Serializable;

/**
 * A model class representing an item in the shopping list.
 * It stores information about the ingredient, its price, type,
 * and whether it has been checked off the shopping list.
 * This class implements {@link Serializable} for allowing easy transfer of data.
 *
 * @author Cristina Correa
 * @since 1.0
 */
public class ShoppingItemModel implements Serializable {
    String id;
    String ingredientName;
    String price;
    String type;
    boolean isChecked;

    public ShoppingItemModel() {
    }

    public ShoppingItemModel(String id, String ingredientName, String type, boolean isChecked) {
        this.id = id;
        this.ingredientName = ingredientName;
        this.type = type;
        this.isChecked = isChecked;
    }

    public ShoppingItemModel(String id, String ingredientName, String price, String type, boolean isChecked) {
        this.id = id;
        this.ingredientName = ingredientName;
        this.price = price;
        this.type = type;
        this.isChecked = isChecked;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIngredientName() {
        return ingredientName;
    }

    public void setIngredientName(String ingredientName) {
        this.ingredientName = ingredientName;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean getIsChecked() {
        return isChecked;
    }

    public void setIsChecked(boolean isChecked) {
        this.isChecked = isChecked;
    }
}
