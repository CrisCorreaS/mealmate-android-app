package com.cristina.correa.mealmatecristina;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.cristina.correa.mealmatecristina.fragments.TodayFragment;
import com.cristina.correa.mealmatecristina.fragments.WeekFragment;
import com.cristina.correa.mealmatecristina.utils.UserUtils;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

/**
 * Activity for managing and displaying user profile details.
 * Provides options for navigating the app and switching between "Today" and "Week" views.
 *
 * @author Cristina Correa
 * @since 1.0
 */
public class ProfileActivity extends AppCompatActivity {

    private ImageView userProfilePictureImageView;
    private TextView userNameTextView, userEmailTextView;
    private LinearLayout todayButtonCalendarCardViewLinearLayout, todayCardViewCalendarButtonLinearLayout;
    private Button todayButton, calendarButton;
    private CardView todayCardView, calendarCardView;

    private BottomNavigationView bottomNavigationView;

    private FirebaseAuth firebaseAuth;
    private GoogleSignInClient googleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        userProfilePictureImageView = findViewById(R.id.user_profile_picture);
        userNameTextView = findViewById(R.id.user_name);
        userEmailTextView = findViewById(R.id.user_email);
        todayButtonCalendarCardViewLinearLayout = findViewById(R.id.today_button_calendar_cardview);
        todayCardViewCalendarButtonLinearLayout = findViewById(R.id.today_cardview_calendar_button);
        todayButton = findViewById(R.id.button_today);
        calendarButton = findViewById(R.id.button_calendar);
        todayCardView = findViewById(R.id.cardview_today);
        calendarCardView = findViewById(R.id.cardview_calendar);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        UserUtils.loadUserProfileImage(userProfilePictureImageView);

        firebaseAuth = FirebaseAuth.getInstance();

        googleSignInClient = GoogleSignIn.getClient(this, new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.web_id))
                .requestEmail()
                .build());

        userProfilePictureImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logoutUser();
            }
        });

        UserUtils.getUserName(name -> {
            if (name != null) {
                userNameTextView.setText(name);
            }
        });
        userEmailTextView.setText(UserUtils.getUserEmail());

        todayCardViewCalendarButtonLinearLayout.setVisibility(View.GONE);
        updateFragment();

        todayButton.setOnClickListener(v -> {
            if (todayCardViewCalendarButtonLinearLayout.getVisibility() == View.GONE) {
                todayButtonCalendarCardViewLinearLayout.setVisibility(View.VISIBLE);
                todayCardViewCalendarButtonLinearLayout.setVisibility(View.GONE);
                updateFragment();
            }
        });

        calendarCardView.setOnClickListener(v -> {
            todayButtonCalendarCardViewLinearLayout.setVisibility(View.GONE);
            todayCardViewCalendarButtonLinearLayout.setVisibility(View.VISIBLE);
            updateFragment();
        });

        calendarButton.setOnClickListener(v -> {
            if (todayButtonCalendarCardViewLinearLayout.getVisibility() == View.GONE) {
                todayCardViewCalendarButtonLinearLayout.setVisibility(View.VISIBLE);
                updateFragment();
            }
        });

        todayCardView.setOnClickListener(v -> {
            todayButtonCalendarCardViewLinearLayout.setVisibility(View.VISIBLE);
            todayCardViewCalendarButtonLinearLayout.setVisibility(View.GONE);
            updateFragment();
        });


        bottomNavigationView.setSelectedItemId(R.id.bottom_profile);

        loadFragment(new TodayFragment());

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.bottom_home) {
                startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                finish();

                return true;
            } else if (itemId == R.id.bottom_discover) {
                startActivity(new Intent(getApplicationContext(), DiscoverActivity.class));
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                finish();

                return true;
            } else if (itemId == R.id.bottom_list) {
                startActivity(new Intent(getApplicationContext(), ListActivity.class));
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                finish();

                return true;
            } else if (itemId == R.id.bottom_profile) {
                return true;
            }
            return false;
        });
    }

    /**
     * Logs out the user and redirects to the WelcomeActivity.
     */
    private void logoutUser() {
        firebaseAuth.signOut();

        googleSignInClient.signOut().addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(Task<Void> task) {
                Intent intent = new Intent(ProfileActivity.this, WelcomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);

                finish();
            }
        });
    }

    /**
     * Updates the current fragment based on the selected view (Today or Week).
     */
    private void updateFragment() {
        if (todayCardViewCalendarButtonLinearLayout.getVisibility() == View.GONE) {
            loadFragment(new TodayFragment());
        } else {
            loadFragment(new WeekFragment());
        }
    }

    /**
     * Loads the specified fragment into the fragment container.
     *
     * @param fragment the fragment to display.
     */
    private void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}