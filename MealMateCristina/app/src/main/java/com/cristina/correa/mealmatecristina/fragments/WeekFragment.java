package com.cristina.correa.mealmatecristina.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cristina.correa.mealmatecristina.R;
import com.cristina.correa.mealmatecristina.adapters.CustomMealAdapter;
import com.cristina.correa.mealmatecristina.models.MealModel;
import com.cristina.correa.mealmatecristina.utils.ToastUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * A {@link Fragment} that displays the planned meals for each day of the current week.
 * Meals are fetched from Firebase and organized by day and type.
 *
 * @author Cristina Correa
 * @since 1.0
 */
public class WeekFragment extends Fragment {

    private final String[] DAYS_OF_THE_WEEK = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};

    private RecyclerView[] dayRecyclerViews;
    private CustomMealAdapter[] mealAdapters;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;

    /**
     * Inflates the fragment's view and initializes the Firebase authentication instance and UI elements.
     * @param inflater LayoutInflater to inflate the view.
     * @param container Parent view group.
     * @param savedInstanceState Saved state of the fragment.
     * @return The root view of the fragment.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_week, container, false);

        firebaseAuth = FirebaseAuth.getInstance();
        setupRecyclerViews(view);
        fetchMealsForWeek();

        return view;
    }

    /**
     * Sets up the RecyclerViews for each day of the week.
     * @param view The root view containing the RecyclerViews.
     */
    private void setupRecyclerViews(View view) {
        dayRecyclerViews = new RecyclerView[7];
        mealAdapters = new CustomMealAdapter[7];

        int[] recyclerViewIds = {
                R.id.monday_meals,
                R.id.tuesday_meals,
                R.id.wednesday_meals,
                R.id.thursday_meals,
                R.id.friday_meals,
                R.id.saturday_meals,
                R.id.sunday_meals
        };

        for (int i = 0; i < 7; i++) {
            dayRecyclerViews[i] = view.findViewById(recyclerViewIds[i]);
            dayRecyclerViews[i].setLayoutManager(new LinearLayoutManager(getContext()));

            mealAdapters[i] = new CustomMealAdapter();

            dayRecyclerViews[i].setAdapter(mealAdapters[i]);
        }
    }

    /**
     * Fetches meals for each day of the current week from the Firebase database.
     * Meals are fetched for the current logged-in user.
     */
    private void fetchMealsForWeek() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if (currentUser == null) {
            ToastUtils.showCustomToast(getContext(), "User not logged in!");
            return;
        }

        String userId = currentUser.getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference("PlannedMeals").child(userId);

        for (int i = 0; i < 7; i++) {
            String dateForDay = getDateForDay(i);
            int finalI = i;

            databaseReference.child(dateForDay).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        List<String> mealIds = new ArrayList<>();

                        for (DataSnapshot mealSnapshot : snapshot.getChildren()) {
                            mealIds.add(mealSnapshot.getValue(String.class));
                        }

                        loadMealDetails(mealIds, finalI);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    ToastUtils.showCustomToast(getContext(), "Error fetching data for " + DAYS_OF_THE_WEEK[finalI]);
                }
            });
        }
    }

    /**
     * Loads details for the specified meals and updates the adapter for the corresponding day.
     * @param mealIds List of meal IDs to fetch.
     * @param dayIndex Index of the day in the week.
     */
    private void loadMealDetails(List<String> mealIds, int dayIndex) {
        DatabaseReference mealsRef = FirebaseDatabase.getInstance().getReference("Meal");

        mealsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<MealModel> dayMeals = new ArrayList<>();

                for (String mealId : mealIds) {
                    if (snapshot.hasChild(mealId)) {
                        MealModel meal = snapshot.child(mealId).getValue(MealModel.class);

                        dayMeals.add(meal);
                    }
                }

                if (!dayMeals.isEmpty()) {
                    Collections.sort(dayMeals, new Comparator<MealModel>() {
                        @Override
                        public int compare(MealModel o1, MealModel o2) {
                            return getMealTypeOrder(o1.getMealType()) - getMealTypeOrder(o2.getMealType());
                        }
                    });

                    mealAdapters[dayIndex].updateData(dayMeals);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                ToastUtils.showCustomToast(getContext(), "Error fetching meal details for " + DAYS_OF_THE_WEEK[dayIndex]);
            }
        });
    }

    /**
     * Retrieves the date for a given day index in the week.
     * @param dayIndex Index of the day in the week (0 = Monday).
     * @return The date in "yyyy-MM-dd" format.
     */
    private String getDateForDay(int dayIndex) {
        Calendar calendar = Calendar.getInstance();

        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        calendar.add(Calendar.DAY_OF_YEAR, dayIndex);

        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.getTime());
    }

    /**
     * Determines the order of meal types for sorting purposes.
     * @param type The type of the meal.
     * @return The order of the meal type (lower value = higher priority).
     */
    private int getMealTypeOrder(String type) {
        switch (type.toLowerCase()) {
            case "breakfast":
                return 1;
            case "lunch":
                return 2;
            case "snack":
                return 3;
            case "dinner":
                return 4;
            default:
                return 5;
        }
    }
}