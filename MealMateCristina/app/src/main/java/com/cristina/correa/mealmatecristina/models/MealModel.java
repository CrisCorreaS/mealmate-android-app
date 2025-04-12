package com.cristina.correa.mealmatecristina.models;

import java.util.List;

/**
 * A model class representing a meal with various attributes such as title, type, ingredients,
 * nutritional information (calories, proteins, fats, carbs), and cooking details.
 * This class is used to store and manage meal-related data in the application.
 *
 * @author Cristina Correa
 * @since 1.0
 */
public class MealModel {
    String meal_id;
    String title;
    String mealType;
    long servings;
    long duration;
    String img_url;
    String description;
    List<String> ingredients;
    String instructions;
    long calories;
    long proteins;
    long carbs;
    long fats;

    public MealModel() {
    }

    public MealModel(String title, String mealType, long duration, String img_url, String description, long calories) {
        this.title = title;
        this.mealType = mealType;
        this.duration = duration;
        this.img_url = img_url;
        this.description = description;
        this.calories = calories;
    }

    public MealModel(String meal_id, String title, String mealType, long fats, long carbs, long proteins, long calories, String instructions, List<String> ingredients, String description, String img_url, long duration, long servings) {
        this.meal_id = meal_id;
        this.title = title;
        this.mealType = mealType;
        this.fats = fats;
        this.carbs = carbs;
        this.proteins = proteins;
        this.calories = calories;
        this.instructions = instructions;
        this.ingredients = ingredients;
        this.description = description;
        this.img_url = img_url;
        this.duration = duration;
        this.servings = servings;
    }

    public String getMeal_id() {
        return meal_id;
    }

    public void setMeal_id(String meal_id) {
        this.meal_id = meal_id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMealType() {
        return mealType;
    }

    public void setMealType(String mealType) {
        this.mealType = mealType;
    }

    public long getServings() {
        return servings;
    }

    public void setServings(long servings) {
        this.servings = servings;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<String> ingredients) {
        this.ingredients = ingredients;
    }

    public String getImg_url() {
        return img_url;
    }

    public void setImg_url(String img_url) {
        this.img_url = img_url;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public long getCalories() {
        return calories;
    }

    public void setCalories(long calories) {
        this.calories = calories;
    }

    public long getCarbs() {
        return carbs;
    }

    public void setCarbs(long carbs) {
        this.carbs = carbs;
    }

    public long getProteins() {
        return proteins;
    }

    public void setProteins(long proteins) {
        this.proteins = proteins;
    }

    public long getFats() {
        return fats;
    }

    public void setFats(long fats) {
        this.fats = fats;
    }
}
