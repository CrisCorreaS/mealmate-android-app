package com.cristina.correa.mealmatecristina;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.cristina.correa.mealmatecristina.models.MealModel;
import com.cristina.correa.mealmatecristina.utils.ToastUtils;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Arrays;
import java.util.List;

/**
 * Activity for creating a new meal entry.
 * This class allows users to input meal details such as title, description, duration, nutritional values,
 * ingredients, and instructions, and save the meal to Firebase Realtime Database.
 * It also provides navigation to other activities and a spinner for meal type selection.
 *
 * @author Cristina Correa
 * @since 1.0
 */
public class CreateMealActivity extends AppCompatActivity {

    private TextView toolbarTittleCross;
    private ImageView toolbarBinIcon;
    private EditText mealTitle, mealDescription, mealDuration, mealServings, mealCalories, mealProteins, mealCarbs, mealFats, mealImgUrl, mealIngredients, mealInstructions;
    private Spinner mealTypeSpinner;
    private Button saveMealButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_meal);

        toolbarTittleCross = findViewById(R.id.toolbar_title);
        toolbarBinIcon = findViewById(R.id.toolbar_menu_icon);
        mealTitle = findViewById(R.id.meal_title_input);
        mealDescription = findViewById(R.id.meal_description_input);
        mealDuration = findViewById(R.id.meal_duration_input);
        mealServings = findViewById(R.id.meal_servings_input);
        mealCalories = findViewById(R.id.meal_calories_input);
        mealProteins = findViewById(R.id.meal_proteins_input);
        mealCarbs = findViewById(R.id.meal_carbs_input);
        mealFats = findViewById(R.id.meal_fats_input);
        mealImgUrl = findViewById(R.id.meal_img_url_input);
        mealIngredients = findViewById(R.id.meal_ingredients_input);
        mealInstructions = findViewById(R.id.meal_instructions_input);
        mealTypeSpinner = findViewById(R.id.meal_type_spinner);
        saveMealButton = findViewById(R.id.save_meal_button);

        toolbarTittleCross.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                startActivity(intent);

                finish();
            }
        });

        toolbarBinIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
                startActivity(intent);
                finish();
            }
        });

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.meal_types_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mealTypeSpinner.setAdapter(adapter);

        saveMealButton.setOnClickListener(v -> saveMeal());
    }

    /**
     * Saves the meal to Firebase Realtime Database with the provided user input.
     * Validates the input fields and displays appropriate messages for errors or success.
     */
    private void saveMeal() {
        String title = mealTitle.getText().toString().trim();
        String description = mealDescription.getText().toString().trim();
        String durationStr = mealDuration.getText().toString().trim();
        String servingsStr = mealServings.getText().toString().trim();
        String caloriesStr = mealCalories.getText().toString().trim();
        String proteinsStr = mealProteins.getText().toString().trim();
        String carbsStr = mealCarbs.getText().toString().trim();
        String fatsStr = mealFats.getText().toString().trim();
        String imgUrl = mealImgUrl.getText().toString().trim();
        String ingredientsStr = mealIngredients.getText().toString().trim();
        String instructions = mealInstructions.getText().toString().trim();
        String mealType = mealTypeSpinner.getSelectedItem().toString();

        if (title.isEmpty() || mealType.isEmpty() || servingsStr.isEmpty() || durationStr.isEmpty() || description.isEmpty() || ingredientsStr.isEmpty() || instructions.isEmpty() || caloriesStr.isEmpty() || proteinsStr.isEmpty() || carbsStr.isEmpty() || fatsStr.isEmpty() || imgUrl.isEmpty()) {
            ToastUtils.showCustomToast(this, "Please fill in all mandatory fields");
            return;
        }

        long duration = Long.parseLong(durationStr);
        int servings = Integer.parseInt(servingsStr);
        int calories = caloriesStr.isEmpty() ? 0 : Integer.parseInt(caloriesStr);
        int proteins = proteinsStr.isEmpty() ? 0 : Integer.parseInt(proteinsStr);
        int carbs = carbsStr.isEmpty() ? 0 : Integer.parseInt(carbsStr);
        int fats = fatsStr.isEmpty() ? 0 : Integer.parseInt(fatsStr);

        List<String> ingredients = Arrays.asList(ingredientsStr.split("\\s*,\\s*"));

        DatabaseReference mealsRef = FirebaseDatabase.getInstance().getReference("Meal");
        String mealId = mealsRef.push().getKey();

        MealModel newMeal = new MealModel(mealId, title, mealType, fats, carbs, proteins, calories, instructions, ingredients, description, imgUrl, duration, servings);

        mealsRef.child(mealId).setValue(newMeal).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                ToastUtils.showCustomToast(this, "Meal created successfully!");

                Intent intent = new Intent(CreateMealActivity.this, ProfileActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);

                finish();
            } else {
                ToastUtils.showCustomToast(this, "Failed to create meal. Please try again.");
            }
        });
    }
}