package com.cristina.correa.mealmatecristina;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cristina.correa.mealmatecristina.adapters.CustomMealAdapter;
import com.cristina.correa.mealmatecristina.models.MealModel;
import com.cristina.correa.mealmatecristina.utils.ToastUtils;
import com.cristina.correa.mealmatecristina.utils.UserUtils;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * HomeActivity provides the main screen for the user.
 * Displays a personalized greeting, location, popular meals, and navigation options.
 * Includes search functionality and links to other activities such as profile and meal categories.
 *
 * @author Cristina Correa
 * @since 1.0
 */
public class HomeActivity extends AppCompatActivity {

    private final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private TextView goodMorningText, textLocation, viewAllNew;
    private EditText searchBoxEditText;
    private LinearLayout popularMealsInfoLinearLayout, breakfastCategory, lunchCategory, snackCategory, dinnerCategory;
    ;
    private ImageView userProfilePictureImageView;

    private RecyclerView popularMealsRecyclerView;
    private CustomMealAdapter customMealAdapter;
    private LocationManager locationManager;

    private LinearLayoutManager horizontalLayoutManager;
    private LinearLayoutManager verticalLayoutManager;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        goodMorningText = findViewById(R.id.good_morning);
        searchBoxEditText = findViewById(R.id.search);
        textLocation = findViewById(R.id.text_location);
        userProfilePictureImageView = findViewById(R.id.user_profile_picture);
        popularMealsInfoLinearLayout = findViewById(R.id.popular_meals_info);
        popularMealsRecyclerView = findViewById(R.id.popular_recycler_view);
        breakfastCategory = findViewById(R.id.breakfast_category);
        lunchCategory = findViewById(R.id.lunch_category);
        snackCategory = findViewById(R.id.snack_category);
        dinnerCategory = findViewById(R.id.dinner_category);
        viewAllNew = findViewById(R.id.view_all_new);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        UserUtils.loadUserProfileImage(userProfilePictureImageView);
        setupRecyclerView();
        styleGoodMorning();
        requestLocationPermission();
        setupSearch();

        userProfilePictureImageView.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        breakfastCategory.setOnClickListener(view -> openCategory("breakfast"));
        lunchCategory.setOnClickListener(view -> openCategory("lunch"));
        snackCategory.setOnClickListener(view -> openCategory("snack"));
        dinnerCategory.setOnClickListener(view -> openCategory("dinner"));

        viewAllNew.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, ListMealsActivity.class);
            intent.putExtra("isPopular", true);

            startActivity(intent);
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        setupBottomNavigation(bottomNavigationView);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    /**
     * Configures the RecyclerView for displaying popular meals.
     */
    private void setupRecyclerView() {
        horizontalLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        verticalLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        popularMealsRecyclerView.setLayoutManager(horizontalLayoutManager);

        FirebaseRecyclerOptions<MealModel> options = new FirebaseRecyclerOptions.Builder<MealModel>()
                .setQuery(FirebaseDatabase.getInstance().getReference().child("Meal").limitToFirst(5), MealModel.class)
                .build();

        customMealAdapter = new CustomMealAdapter();
        popularMealsRecyclerView.setAdapter(customMealAdapter);

        loadInitialData();
    }

    /**
     * Loads initial popular meals data from the database.
     */
    private void loadInitialData() {
        DatabaseReference mealRef = FirebaseDatabase.getInstance().getReference("Meal");
        mealRef.limitToFirst(5).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<MealModel> initialMeals = new ArrayList<>();

                for (DataSnapshot mealSnapshot : snapshot.getChildren()) {
                    MealModel meal = mealSnapshot.getValue(MealModel.class);

                    initialMeals.add(meal);
                }

                customMealAdapter.updateData(initialMeals);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                ToastUtils.showCustomToast(HomeActivity.this, "Error loading data");
            }
        });
    }

    /**
     * Styles the "Good Morning" text with colored spans.
     */
    private void styleGoodMorning() {
        String goodMorning = getString(R.string.good_morning);
        SpannableStringBuilder builder = new SpannableStringBuilder(goodMorning);

        int goodStart = goodMorning.indexOf("Good");
        int goodEnd = goodStart + "Good".length();
        int morningStart = goodMorning.indexOf("Morning");
        int morningEnd = morningStart + "Morning".length();

        builder.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.primary_dark)), goodStart, goodEnd, 0);
        builder.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.primary)), morningStart, morningEnd, 0);

        goodMorningText.setText(builder);
    }

    /**
     * Requests location permissions if not already granted.
     */
    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            getLocation();
        }
    }

    /**
     * Fetches the user's location and updates the location text.
     */
    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, new LocationListener() {

                @Override
                public void onLocationChanged(@NonNull Location location) {
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();

                    updateLocationText(latitude, longitude);
                }

                @Override
                public void onProviderDisabled(@NonNull String provider) {
                    textLocation.setText(R.string.location_disabled);
                }
            });
        }
    }

    /**
     * Updates the displayed location based on coordinates.
     *
     * @param latitude  the latitude.
     * @param longitude the longitude.
     */
    private void updateLocationText(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);

            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);

                String city = address.getLocality();
                String country = address.getCountryName();

                textLocation.setText(String.format("%s, %s", city, country));
            } else {
                textLocation.setText(R.string.location_not_found);
            }
        } catch (IOException e) {
            e.printStackTrace();
            textLocation.setText(R.string.location_error);
        }
    }

    /**
     * Configures search box to filter popular meals based on user input.
     */
    private void setupSearch() {
        searchBoxEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                performSearch(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    /**
     * Performs a search for meals based on the user's query.
     * Updates the RecyclerView to show either the default list of popular meals or filtered results.
     *
     * @param query the search query entered by the user.
     */
    private void performSearch(String query) {
        popularMealsInfoLinearLayout.setVisibility(View.GONE);

        if (query.isEmpty()) {
            popularMealsInfoLinearLayout.setVisibility(View.VISIBLE);
            popularMealsRecyclerView.setLayoutManager(horizontalLayoutManager);

            loadInitialData();
        } else {
            DatabaseReference mealRef = FirebaseDatabase.getInstance().getReference("Meal");
            Query searchQuery = mealRef.orderByChild("title")
                    .startAt(query)
                    .endAt(query + "\uf8ff");

            searchQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    List<MealModel> searchResults = new ArrayList<>();

                    for (DataSnapshot mealSnapshot : snapshot.getChildren()) {
                        MealModel meal = mealSnapshot.getValue(MealModel.class);
                        searchResults.add(meal);
                    }

                    popularMealsInfoLinearLayout.setVisibility(View.GONE);
                    popularMealsRecyclerView.setLayoutManager(verticalLayoutManager);
                    updateRecyclerView(searchResults);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    ToastUtils.showCustomToast(HomeActivity.this, "Error fetching search results");
                }
            });
        }
    }

    /**
     * Updates the RecyclerView with a new list of meals.
     *
     * @param meals the list of meals to display.
     */
    private void updateRecyclerView(List<MealModel> meals) {
        customMealAdapter.updateData(meals);
    }

    /**
     * Configures the BottomNavigationView to handle user navigation between different activities.
     *
     * @param bottomNavigationView the BottomNavigationView to set up.
     */
    private void setupBottomNavigation(BottomNavigationView bottomNavigationView) {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.bottom_home) {
                return true;
            } else if (itemId == R.id.bottom_discover) {
                startActivity(new Intent(this, DiscoverActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();

                return true;
            } else if (itemId == R.id.bottom_list) {
                startActivity(new Intent(this, ListActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();

                return true;
            } else if (itemId == R.id.bottom_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();

                return true;
            }
            return false;
        });
    }

    /**
     * Opens the MealCategoryActivity to display meals of a specific type.
     *
     * @param mealType the type of meal (e.g., breakfast, lunch, snack, dinner).
     */
    private void openCategory(String mealType) {
        Intent intent = new Intent(this, MealCategoryActivity.class);
        intent.putExtra("mealType", mealType);
        startActivity(intent);
    }
}
