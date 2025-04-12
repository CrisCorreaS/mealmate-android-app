package com.cristina.correa.mealmatecristina.models;

/**
 * A model class representing a user in the system.
 * It stores the user's name, email, and password.
 * This class is used for managing user information.
 *
 * @author Cristina Correa
 * @since 1.0
 */
public class UserModel {
    String name;
    String email;
    String password;

    public UserModel() {
    }

    public UserModel(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
