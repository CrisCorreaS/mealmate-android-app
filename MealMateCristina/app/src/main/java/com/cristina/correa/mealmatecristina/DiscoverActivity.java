package com.cristina.correa.mealmatecristina;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cristina.correa.mealmatecristina.adapters.MealAdapter;
import com.cristina.correa.mealmatecristina.models.MealModel;
import com.cristina.correa.mealmatecristina.utils.UserUtils;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Activity for discovering new meals and meal categories.
 * This class provides a user interface to browse recently added meals, navigate through meal categories,
 * and interact with various sections of the app, including the user's profile and popular meals.
 *
 * @author Cristina Correa
 * @since 1.0
 */
public class DiscoverActivity extends AppCompatActivity {

    private TextView newTastyMealsText, viewAllPopular;
    private ImageView userProfilePictureImageView;
    private LinearLayout breakfastCategory, lunchCategory, snackCategory, dinnerCategory;
    private RecyclerView newMealsRecyclerView;

    private BottomNavigationView bottomNavigationView;

    private MealAdapter newMealAdapter;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_discover);

        newTastyMealsText = findViewById(R.id.new_tasty_meals);
        userProfilePictureImageView = findViewById(R.id.user_profile_picture);
        newMealsRecyclerView = findViewById(R.id.new_recycler_view);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        viewAllPopular = findViewById(R.id.view_all_popular);
        breakfastCategory = findViewById(R.id.breakfast_category);
        lunchCategory = findViewById(R.id.lunch_category);
        snackCategory = findViewById(R.id.snack_category);
        dinnerCategory = findViewById(R.id.dinner_category);

        bottomNavigationView.setSelectedItemId(R.id.bottom_discover);

        styleNewTastyMeals();

        UserUtils.loadUserProfileImage(userProfilePictureImageView);

        LinearLayoutManager newLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        newMealsRecyclerView.setLayoutManager(newLayoutManager);

        FirebaseRecyclerOptions<MealModel> newOptions = new FirebaseRecyclerOptions.Builder<MealModel>()
                .setQuery(FirebaseDatabase.getInstance().getReference().child("Meal").limitToLast(5), MealModel.class)
                .build();

        newMealAdapter = new MealAdapter(newOptions);
        newMealsRecyclerView.setAdapter(newMealAdapter);
        newMealAdapter.startListening();

        userProfilePictureImageView.setOnClickListener(v -> {
            Intent intent = new Intent(DiscoverActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        viewAllPopular.setOnClickListener(v -> {
            Intent intent = new Intent(DiscoverActivity.this, ListMealsActivity.class);
            intent.putExtra("isPopular", false);

            startActivity(intent);
        });

        breakfastCategory.setOnClickListener(view -> openCategory("breakfast"));
        lunchCategory.setOnClickListener(view -> openCategory("lunch"));
        snackCategory.setOnClickListener(view -> openCategory("snack"));
        dinnerCategory.setOnClickListener(view -> openCategory("dinner"));

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.bottom_home) {
                startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                finish();

                return true;
            } else if (itemId == R.id.bottom_discover) {
                return true;
            } else if (itemId == R.id.bottom_list) {
                startActivity(new Intent(getApplicationContext(), ListActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();

                return true;
            } else if (itemId == R.id.bottom_profile) {
                startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();

                return true;
            }
            return false;
        });
    }

    @Override
    protected void onStart() {
        newMealAdapter.startListening();

        super.onStart();
    }

    @Override
    protected void onStop() {
        newMealAdapter.stopListening();

        super.onStop();
    }

    /**
     * Applies custom styling to the new tasty meals text.
     */
    private void styleNewTastyMeals() {
        String newTastyMeals = "New Tasty Meals!";
        SpannableStringBuilder builder = new SpannableStringBuilder(newTastyMeals);

        int newStart = newTastyMeals.indexOf("New");
        int newEnd = newStart + "New".length();
        int tastyMealsStart = newTastyMeals.indexOf("Tasty Meals!");
        int tastyMealsEnd = tastyMealsStart + "Tasty Meals!".length();


        builder.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.primary)), newStart, newEnd, 0);
        builder.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.primary_dark)), tastyMealsStart, tastyMealsEnd, 0);

        newTastyMealsText.setText(builder);
    }

    /**
     * Opens the specified meal category.
     *
     * @param mealType the type of meal category to open.
     */
    private void openCategory(String mealType) {
        Intent intent = new Intent(this, MealCategoryActivity.class);
        intent.putExtra("mealType", mealType);
        startActivity(intent);
    }
}