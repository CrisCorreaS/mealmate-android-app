package com.cristina.correa.mealmatecristina;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.cristina.correa.mealmatecristina.utils.ToastUtils;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

/**
 * Represents the WelcomeActivity screen of the MealMate application.
 * Provides an entry point for users to log in or sign up, including options for email-based login and Google Sign-In.
 * Allows navigation to the {@link LoginActivity}, {@link RegisterActivity}, and {@link HomeActivity} screens.
 *
 * @author Cristina Correa
 * @since 1.0
 */
public class WelcomeActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 9001;

    private TextView title;
    private Button buttonSignIn;
    private LinearLayout layoutSignInGoogle;
    private TextView signUpNow;
    private ProgressBar progressBar;

    private FirebaseAuth firebaseAuth;
    private GoogleSignInClient googleSignInClient;
    private GoogleSignInOptions googleSignInOptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_welcome);

        title = findViewById(R.id.title_slogan);
        buttonSignIn = findViewById(R.id.btn_sign_in);
        layoutSignInGoogle = findViewById(R.id.btn_sign_in_google);
        signUpNow = findViewById(R.id.sing_up_now);
        progressBar = findViewById(R.id.progressBar);

        styleSlogan();

        /**
         * Initializes Firebase Authentication and configures Google Sign-In for the application.
         *
         * A {@link GoogleSignInOptions} object is created with the default sign-in configuration requesting an ID token and the user's email address.
         */
        firebaseAuth = FirebaseAuth.getInstance();

        googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(R.string.web_id)).requestEmail().build();
        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);

        GoogleSignInAccount googleSignInAccount = GoogleSignIn.getLastSignedInAccount(this);
        if (googleSignInAccount != null) {
            progressBar.setVisibility(View.VISIBLE);

            Intent intent = new Intent(WelcomeActivity.this, HomeActivity.class);
            startActivity(intent);

            finish();
        }

        if (firebaseAuth.getCurrentUser() != null) {
            progressBar.setVisibility(View.VISIBLE);

            Intent intent = new Intent(WelcomeActivity.this, HomeActivity.class);
            startActivity(intent);

            finish();
        }

        ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

                    try {
                        task.getResult(ApiException.class);
                        finish();

                        Intent intent = new Intent(WelcomeActivity.this, HomeActivity.class);
                        startActivity(intent);
                    } catch (ApiException e) {
                        ToastUtils.showCustomToast(WelcomeActivity.this, "LoginActivity with Google failed");
                    }
                }
            }
        });

        layoutSignInGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent signInIntent = googleSignInClient.getSignInIntent();
                activityResultLauncher.launch(signInIntent);
            }
        });

        /**
         * Listener for the "LoginActivity Now" button, which navigates to the {@link LoginActivity} activity.
         */
        buttonSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
            }
        });

        /**
         * Listener for the Google "LoginActivity" button. Authenticates the user with Firebase and navigates to the {@link HomeActivity} upon successful login.
         */
        layoutSignInGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent sininIntent = googleSignInClient.getSignInIntent();
                startActivityForResult(sininIntent, RC_SIGN_IN);
            }
        });

        /**
         * Listener for the "Sign Up Now" text, which navigates to the {@link RegisterActivity} activity.
         */
        signUpNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(intent);
            }
        });

    }

    /**
     * Applies custom styling to the slogan text.
     */
    private void styleSlogan() {
        String slogan = getString(R.string.slogan);
        SpannableString spannableString = new SpannableString(slogan);

        int discoveringStart = slogan.indexOf("Discovering");
        int discoveringEnd = discoveringStart + "Discovering".length();
        int quickStart = slogan.indexOf("Quick");
        int quickEnd = quickStart + "Quick".length();
        int tastyHealthyStart = slogan.indexOf(", Tasty, Healthy");
        int tastyHealthyEnd = tastyHealthyStart + ", Tasty, Healthy".length();
        int mealsStart = slogan.indexOf("Meals");
        int mealsEnd = mealsStart + "Meals".length();

        spannableString.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.primary_dark)), discoveringStart, discoveringEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.primary)), quickStart, quickEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.primary_dark)), tastyHealthyStart, tastyHealthyEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.secondary)), mealsStart, mealsEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        title.setText(spannableString);
    }
}
