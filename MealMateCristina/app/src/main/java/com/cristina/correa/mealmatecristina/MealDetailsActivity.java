package com.cristina.correa.mealmatecristina;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.cristina.correa.mealmatecristina.models.MealModel;
import com.cristina.correa.mealmatecristina.models.ShoppingItemModel;
import com.cristina.correa.mealmatecristina.utils.DescriptionUtils;
import com.cristina.correa.mealmatecristina.utils.ToastUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Activity for displaying detailed information about a selected meal.
 * Users can add ingredients to their shopping list and plan the meal for a specific date.
 *
 * @author Cristina Correa
 * @since 1.0
 */
public class MealDetailsActivity extends AppCompatActivity {

    private ImageView mealImageView, mealTypeImage;
    private ImageButton arrowImageButton;
    private TextView mealTitle, mealServings, mealDuration, mealDescription, readMore, mealIngredients, mealInstructions, mealCalories, mealProteins, mealCarbs, mealFats;
    private LinearLayout readMoreLinearLayout, addMealLinearLayout;

    private String fullDescription = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meal_details);

        readMoreLinearLayout = findViewById(R.id.ingredients_instructions_container);
        readMoreLinearLayout.setVisibility(View.GONE);

        mealImageView = findViewById(R.id.meal_image_view);
        arrowImageButton = findViewById(R.id.back_arrow_button);
        mealTypeImage = findViewById(R.id.meal_type_image);
        mealTitle = findViewById(R.id.meal_title);
        mealServings = findViewById(R.id.meal_servings);
        mealDuration = findViewById(R.id.meal_duration);
        mealDescription = findViewById(R.id.meal_description);
        readMore = findViewById(R.id.read_more);
        mealIngredients = findViewById(R.id.meal_ingredients);
        mealInstructions = findViewById(R.id.meal_instructions);
        mealCalories = findViewById(R.id.meal_calories);
        mealProteins = findViewById(R.id.meal_proteins);
        mealCarbs = findViewById(R.id.meal_carbs);
        mealFats = findViewById(R.id.meal_fats);
        addMealLinearLayout = findViewById(R.id.add_meal);

        String meal_id = getIntent().getStringExtra("meal_id");

        DatabaseReference mealRef = FirebaseDatabase.getInstance().getReference("Meal").child(meal_id);
        mealRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                MealModel meal = snapshot.getValue(MealModel.class);

                if (meal != null) {
                    Picasso.get().load(meal.getImg_url()).into(mealImageView);
                    mealTitle.setText(meal.getTitle());

                    mealServings.setText(meal.getServings() + " servings");
                    mealDuration.setText(meal.getDuration() + " min");
                    mealCalories.setText(meal.getCalories() + " Kcal");
                    mealProteins.setText(meal.getProteins() + "g Proteins");
                    mealCarbs.setText(meal.getCarbs() + "g Carbs");
                    mealFats.setText(meal.getFats() + "g Fats");

                    fullDescription = meal.getDescription();
                    mealDescription.setText(DescriptionUtils.truncateDescription(fullDescription, 90, 15));

                    int mealTypeImageRes = getMealTypeImageRes(meal.getMealType());
                    mealTypeImage.setImageResource(mealTypeImageRes);

                    mealIngredients.setText(formatIngredients(meal.getIngredients()));
                    mealInstructions.setText(formatInstructions(meal.getInstructions()));

                    styleInstructions();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                ToastUtils.showCustomToast(MealDetailsActivity.this, "Sorry! We couldn't get the data");
            }


        });

        arrowImageButton.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), DiscoverActivity.class);
            startActivity(intent);
        });

        readMore.setOnClickListener(view -> {
            readMore.setVisibility(View.GONE);
            mealDescription.setText(fullDescription);
            readMoreLinearLayout.setVisibility(View.VISIBLE);
        });

        addMealLinearLayout.setOnClickListener(view -> {
            addIngredientsToShoppingList();

            DatePickerDialog datePickerDialog = new DatePickerDialog(MealDetailsActivity.this, (view1, year, month, dayOfMonth) -> {
                String selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
                addMealToPlannedMeals(selectedDate);
            }, Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH));

            datePickerDialog.show();
        });

    }

    /**
     * Gets the appropriate image resource based on the meal type.
     *
     * @param mealType The type of the meal (e.g., "breakfast", "lunch").
     * @return The resource ID of the corresponding image.
     */
    private int getMealTypeImageRes(String mealType) {
        switch (mealType.toLowerCase()) {
            case "breakfast":
                return R.drawable.meal_types_breakfast;
            case "lunch":
                return R.drawable.meal_types_lunch;
            case "snack":
                return R.drawable.meal_types_snack;
            case "dinner":
                return R.drawable.meal_types_dinner;
            default:
                return R.drawable.meal_types_lunch;
        }
    }

    /**
     * Formats the ingredients list to display each ingredient on a new line.
     *
     * @param ingredients List of ingredients for the meal.
     * @return A formatted string with each ingredient on a new line.
     */
    private String formatIngredients(List<String> ingredients) {
        StringBuilder formattedIngredients = new StringBuilder();

        for (String ingredient : ingredients) {
            formattedIngredients.append(ingredient.toLowerCase()).append("\n\n");
        }

        if (formattedIngredients.length() > 0) {
            formattedIngredients.setLength(formattedIngredients.length() - 2);
        }

        return formattedIngredients.toString();
    }

    /**
     * Formats the instructions list to display each step on a new line.
     *
     * @param instructions A string containing the instructions for the meal.
     * @return A formatted string with each step labeled and on a new line.
     */
    private String formatInstructions(String instructions) {
        String[] steps = instructions.split("\\.");
        StringBuilder formattedInstructions = new StringBuilder();

        for (int i = 0; i < steps.length; i++) {
            String step = steps[i].trim();

            if (!step.isEmpty()) {
                formattedInstructions.append("Step ").append(i + 1).append("\n");
                formattedInstructions.append(step).append(".\n\n");
            }
        }

        if (formattedInstructions.length() > 2) {
            formattedInstructions.setLength(formattedInstructions.length() - 2);
        }

        return formattedInstructions.toString();
    }

    /**
     * Applies custom styling to the instructions text, making step numbers bold and colored.
     */
    private void styleInstructions() {
        String instructionsText = mealInstructions.getText().toString();
        SpannableString spannableString = new SpannableString(instructionsText);

        String[] steps = instructionsText.split("\\n");
        for (int i = 0; i < steps.length; i++) {
            String step = steps[i].trim();

            if (step.startsWith("Step")) {
                int stepStart = instructionsText.indexOf(step);
                int stepEnd = stepStart + step.length();

                spannableString.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), stepStart, stepEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                spannableString.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.primary)), stepStart, stepEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }

        mealInstructions.setText(spannableString);
    }


    /**
     * Adds the ingredients of the selected meal to the user's shopping list.
     * Verifies and avoids duplicating existing ingredients in the list.
     */
    private void addIngredientsToShoppingList() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            ToastUtils.showCustomToast(this, "You need to be logged in to add ingredients to your shopping list.");
            return;
        }

        String uid = currentUser.getUid();
        DatabaseReference shoppingListRef = FirebaseDatabase.getInstance().getReference("ShoppingLists").child(uid).child("ingredients");

        DatabaseReference mealRef = FirebaseDatabase.getInstance().getReference("Meal").child(getIntent().getStringExtra("meal_id"));
        mealRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                MealModel meal = snapshot.getValue(MealModel.class);

                if (meal == null) {
                    ToastUtils.showCustomToast(MealDetailsActivity.this, "Error loading meal details.");
                    return;
                }

                shoppingListRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot shoppingListSnapshot) {
                        Set<String> existingIngredientNames = new HashSet<>();

                        for (DataSnapshot itemSnapshot : shoppingListSnapshot.getChildren()) {
                            ShoppingItemModel existingItem = itemSnapshot.getValue(ShoppingItemModel.class);

                            if (existingItem != null) {
                                existingIngredientNames.add(existingItem.getIngredientName().toLowerCase());
                            }
                        }

                        for (String ingredient : meal.getIngredients()) {
                            if (!existingIngredientNames.contains(ingredient.toLowerCase())) {
                                String itemId = shoppingListRef.push().getKey();

                                ShoppingItemModel shoppingItem = new ShoppingItemModel(itemId, ingredient, "", "None", false);
                                shoppingListRef.child(itemId).setValue(shoppingItem);
                            }
                        }

                        ToastUtils.showCustomToast(MealDetailsActivity.this, "Ingredients added to your shopping list!");
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        ToastUtils.showCustomToast(MealDetailsActivity.this, "Error checking existing ingredients: " + error.getMessage());
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                ToastUtils.showCustomToast(MealDetailsActivity.this, "Error loading meal data: " + error.getMessage());
            }

        });
    }

    /**
     * Adds the selected meal to the user's planned meals for a specific date.
     * Ensures the meal is not already planned for the chosen date before adding.
     *
     * @param date The date for which the meal is being planned (format: YYYY-MM-DD).
     */
    private void addMealToPlannedMeals(String date) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            ToastUtils.showCustomToast(this, "You need to be logged in to plan meals");
            return;
        }

        String uid = currentUser.getUid();
        DatabaseReference plannedMealsRef = FirebaseDatabase.getInstance().getReference("PlannedMeals").child(uid).child(date);
        String mealId = getIntent().getStringExtra("meal_id");

        plannedMealsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> plannedMeals = new ArrayList<>();

                if (snapshot.exists()) {
                    for (DataSnapshot mealSnapshot : snapshot.getChildren()) {
                        String plannedMealId = mealSnapshot.getValue(String.class);

                        if (plannedMealId != null) {
                            plannedMeals.add(plannedMealId);
                        }
                    }
                }

                if (!plannedMeals.contains(mealId)) {
                    plannedMeals.add(mealId);

                    plannedMealsRef.setValue(plannedMeals)
                            .addOnSuccessListener(aVoid ->
                                    ToastUtils.showCustomToast(MealDetailsActivity.this, "Meal planned successfully!"))
                            .addOnFailureListener(e ->
                                    ToastUtils.showCustomToast(MealDetailsActivity.this, "Failed to plan meal: " + e.getMessage()));
                } else {
                    ToastUtils.showCustomToast(MealDetailsActivity.this, "This meal is already planned for the selected date");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                ToastUtils.showCustomToast(MealDetailsActivity.this, "Error planning meal: " + error.getMessage());
            }
        });
    }

}