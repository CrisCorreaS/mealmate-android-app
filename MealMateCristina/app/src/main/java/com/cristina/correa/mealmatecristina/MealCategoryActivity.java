package com.cristina.correa.mealmatecristina;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cristina.correa.mealmatecristina.adapters.MealAdapter;
import com.cristina.correa.mealmatecristina.models.MealModel;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Activity for displaying meals of a specific category (e.g., breakfast, lunch).
 * Fetches and displays data from Firebase based on the selected meal type.
 *
 * @author Cristina Correa
 * @since 1.0
 */
public class MealCategoryActivity extends AppCompatActivity {

    private TextView categoryTitle;
    private RecyclerView recyclerView;
    private MealAdapter mealAdapter;

    /**
     * Initializes the activity, sets up the UI, and configures the RecyclerView to display meals.
     *
     * @param savedInstanceState state of the activity if it is being re-initialized.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_meal_category);

        categoryTitle = findViewById(R.id.category_title);
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        String mealType = getIntent().getStringExtra("mealType");

        switch (mealType) {
            case "breakfast":
                categoryTitle.setText("Breakfast Meals");
                break;
            case "lunch":
                categoryTitle.setText("Lunch Meals");
                break;
            case "snack":
                categoryTitle.setText("Snack Meals");
                break;
            case "dinner":
                categoryTitle.setText("Dinner Meals");
                break;
        }

        categoryTitle.setOnClickListener(view -> {
            Intent intent = new Intent(MealCategoryActivity.this, DiscoverActivity.class);
            startActivity(intent);

            finish();
        });

        FirebaseRecyclerOptions<MealModel> options = new FirebaseRecyclerOptions.Builder<MealModel>()
                .setQuery(FirebaseDatabase.getInstance().getReference()
                        .child("Meal")
                        .orderByChild("mealType")
                        .equalTo(mealType), MealModel.class)
                .build();

        mealAdapter = new MealAdapter(options);
        recyclerView.setAdapter(mealAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mealAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mealAdapter.stopListening();
    }
}