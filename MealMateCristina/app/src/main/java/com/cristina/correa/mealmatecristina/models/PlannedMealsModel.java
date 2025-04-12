package com.cristina.correa.mealmatecristina.models;

import java.util.List;
import java.util.Map;

/**
 * A model class representing the planned meals for a user.
 * It stores the user's ID and a map of planned meals where the key is a date (as a string)
 * and the value is a list of meal IDs planned for that day.
 * This class is used to manage meal planning data for users.
 *
 * @author Cristina Correa
 * @since 1.0
 */
public class PlannedMealsModel {

    private String userId;
    private Map<String, List<String>> plannedMeals;

    public PlannedMealsModel() {
    }

    public PlannedMealsModel(Map<String, List<String>> plannedMeals, String userId) {
        this.plannedMeals = plannedMeals;
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Map<String, List<String>> getPlannedMeals() {
        return plannedMeals;
    }

    public void setPlannedMeals(Map<String, List<String>> plannedMeals) {
        this.plannedMeals = plannedMeals;
    }
}
