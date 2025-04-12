package com.cristina.correa.mealmatecristina;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cristina.correa.mealmatecristina.adapters.CustomMealAdapter;
import com.cristina.correa.mealmatecristina.models.MealModel;
import com.cristina.correa.mealmatecristina.utils.ToastUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An activity for displaying a list of meals fetched from a Firebase database.
 * The meals are displayed in a RecyclerView with customizable sorting based on whether
 * the activity is set to display "Popular Meals" or "New Meals."
 *
 * <p>This activity integrates with Firebase Realtime Database to dynamically fetch and sort meals.
 * It also allows navigation to the {@link DiscoverActivity} when the title is clicked.</p>
 *
 * @author Cristina Correa
 * @since 1.0
 */
public class ListMealsActivity extends AppCompatActivity {

    private boolean isPopular;

    private TextView title;
    private RecyclerView recyclerView;
    private CustomMealAdapter customMealAdapter;
    private FirebaseDatabase database;
    private DatabaseReference mealsRef;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_list_meals);

        title = findViewById(R.id.title);
        recyclerView = findViewById(R.id.recycler_view);

        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        customMealAdapter = new CustomMealAdapter();
        recyclerView.setAdapter(customMealAdapter);

        database = FirebaseDatabase.getInstance();
        mealsRef = database.getReference("Meal");

        isPopular = getIntent().getBooleanExtra("isPopular", false);

        title.setText(isPopular ? "New Meals" : "Popular Meals");

        title.setOnClickListener(view -> {
            Intent intent = new Intent(ListMealsActivity.this, DiscoverActivity.class);
            startActivity(intent);
        });

        loadMeals();
    }

    /**
     * Fetches the list of meals from the Firebase Realtime Database.
     */
    private void loadMeals() {
        mealsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<MealModel> mealList = new ArrayList<>();

                for (DataSnapshot mealSnapshot : snapshot.getChildren()) {
                    MealModel meal = mealSnapshot.getValue(MealModel.class);

                    if (meal != null) {
                        mealList.add(meal);
                    }
                }

                if (isPopular) {
                    Collections.sort(mealList, (o1, o2) -> {
                        boolean isMeal1New = o1.getMeal_id().matches("^m\\d+$");
                        boolean isMeal2New = o2.getMeal_id().matches("^m\\d+$");

                        if (!isMeal1New && isMeal2New) return -1;
                        if (isMeal1New && !isMeal2New) return 1;

                        return o1.getMeal_id().compareTo(o2.getMeal_id());
                    });
                } else {
                    Collections.sort(mealList, (o1, o2) -> {
                        boolean isMeal1New = o1.getMeal_id().matches("^m\\d+$");
                        boolean isMeal2New = o2.getMeal_id().matches("^m\\d+$");

                        if (isMeal1New && !isMeal2New) return -1;
                        if (!isMeal1New && isMeal2New) return 1;

                        return o2.getMeal_id().compareTo(o1.getMeal_id());
                    });
                }

                customMealAdapter.updateData(mealList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                ToastUtils.showCustomToast(ListMealsActivity.this, "Error: " + error.getMessage());
            }
        });
    }
}