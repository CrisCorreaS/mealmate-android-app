package com.cristina.correa.mealmatecristina.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cristina.correa.mealmatecristina.CreateMealActivity;
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
import java.util.TimeZone;

/**
 * A {@link Fragment} that displays the planned meals for the current day.
 * It also provides options to add new meals and displays the current time in UK and Spain.
 *
 * @author Cristina Correa
 * @since 1.0
 */
public class TodayFragment extends Fragment {

    private TextView dayOfTheWeek, dayOfTheMonth, hourUK, hourSpain;
    private FrameLayout addMealFrameLayout;
    private RecyclerView mealsTodayRecyclerView;

    private CustomMealAdapter customMealAdapter;
    private FirebaseAuth auth;
    private DatabaseReference databaseMealsRef;

    /**
     * Inflates the fragment layout and initializes the UI components.
     *
     * @param inflater           The LayoutInflater used to inflate the layout.
     * @param container          The parent ViewGroup for the fragment.
     * @param savedInstanceState A Bundle containing saved instance state data.
     * @return The root View of the inflated layout.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_today, container, false);

        auth = FirebaseAuth.getInstance();

        dayOfTheWeek = view.findViewById(R.id.day_of_the_week);
        dayOfTheMonth = view.findViewById(R.id.day_of_the_month);
        hourUK = view.findViewById(R.id.hour_uk);
        hourSpain = view.findViewById(R.id.hour_spain);
        mealsTodayRecyclerView = view.findViewById(R.id.meals_today);
        addMealFrameLayout = view.findViewById(R.id.add_meal);

        mealsTodayRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        customMealAdapter = new CustomMealAdapter();
        mealsTodayRecyclerView.setAdapter(customMealAdapter);

        setDayOfTheWeek();
        setDayOfTheMonth();
        setHourUK();
        setHourSpain();

        fetchMealsForToday();

        addMealFrameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), CreateMealActivity.class);
                startActivity(intent);
            }
        });

        return view;
    }

    /**
     * Sets the current day of the week in uppercase to the corresponding TextView.
     */
    private void setDayOfTheWeek() {
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.ENGLISH);
        String day = dayFormat.format(Calendar.getInstance().getTime()).toUpperCase();

        dayOfTheWeek.setText(day);
    }

    /**
     * Sets the current day of the month with a formatted string to the corresponding TextView.
     */
    private void setDayOfTheMonth() {
        Calendar calendar = Calendar.getInstance();

        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH) + 1;

        SimpleDateFormat monthFormat = new SimpleDateFormat("MMM", Locale.ENGLISH);
        String monthAbbreviation = monthFormat.format(calendar.getTime()).toUpperCase();

        String formattedDate = String.format(Locale.ENGLISH, "%d.%02d %s", day, month, monthAbbreviation);

        dayOfTheMonth.setText(formattedDate);
    }

    /**
     * Sets the current time in the UK timezone to the corresponding TextView.
     */
    private void setHourUK() {
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.ENGLISH);
        timeFormat.setTimeZone(TimeZone.getTimeZone("Europe/London"));

        String ukTime = timeFormat.format(Calendar.getInstance().getTime());
        hourUK.setText(ukTime);
    }

    /**
     * Sets the current time in the Spain timezone to the corresponding TextView.
     */
    private void setHourSpain() {
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.ENGLISH);
        timeFormat.setTimeZone(TimeZone.getTimeZone("Europe/Madrid"));

        String spainTime = timeFormat.format(Calendar.getInstance().getTime());
        hourSpain.setText(spainTime);
    }

    /**
     * Fetches the planned meals for today from the Firebase Realtime Database and updates the RecyclerView.
     */
    private void fetchMealsForToday() {
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            ToastUtils.showCustomToast(getContext(), "User not logged in!");
            return;
        }

        String userId = currentUser.getUid();
        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().getTime());

        databaseMealsRef = FirebaseDatabase.getInstance().getReference("PlannedMeals").child(userId);

        databaseMealsRef.child(todayDate).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    List<String> mealIds = new ArrayList<>();

                    for (DataSnapshot mealSnapshot : snapshot.getChildren()) {
                        mealIds.add(mealSnapshot.getValue(String.class));
                    }

                    loadMealDetails(mealIds);
                } else {
                    ToastUtils.showCustomToast(getContext(), "No meals planned for today");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                ToastUtils.showCustomToast(getContext(), "Error fetching data");
            }
        });
    }

    /**
     * Loads detailed information for the given list of meal IDs from Firebase Realtime Database
     * and updates the RecyclerView adapter.
     *
     * @param mealIds A list of meal IDs to fetch details for.
     */
    private void loadMealDetails(List<String> mealIds) {
        DatabaseReference mealsRef = FirebaseDatabase.getInstance().getReference("Meal");

        mealsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<MealModel> todayMeals = new ArrayList<>();

                for (String mealId : mealIds) {
                    if (snapshot.hasChild(mealId)) {
                        MealModel meal = snapshot.child(mealId).getValue(MealModel.class);
                        todayMeals.add(meal);
                    }
                }

                if (!todayMeals.isEmpty()) {
                    Collections.sort(todayMeals, new Comparator<MealModel>() {
                        @Override
                        public int compare(MealModel o1, MealModel o2) {
                            return getMealTypeOrder(o1.getMealType()) - getMealTypeOrder(o2.getMealType());
                        }
                    });

                    customMealAdapter.updateData(todayMeals);
                } else {
                    ToastUtils.showCustomToast(getContext(), "No meals found for today");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                ToastUtils.showCustomToast(getContext(), "Error fetching meal details");
            }
        });
    }

    /**
     * Returns the sorting order for meal types.
     *
     * @param type The meal type.
     * @return An integer representing the sorting priority of the meal type.
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