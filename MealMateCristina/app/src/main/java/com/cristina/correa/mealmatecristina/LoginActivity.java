package com.cristina.correa.mealmatecristina;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.cristina.correa.mealmatecristina.utils.ToastUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Represents the LoginActivity screen for the MealMate application.
 * Handles user authentication using Firebase and navigation between LoginActivity and Registration screens.
 *
 * @author Cristina Correa
 * @since 1.0
 */
public class LoginActivity extends AppCompatActivity {

    private EditText editTextEmail, editTextPassword;
    private TextView errorText, forgotPassword, signUpNow;
    private Button buttonLoginEmailPassword;
    private ProgressBar progressBar;

    private FirebaseAuth firebaseAuth;

    /**
     * Checks if a user is already authenticated and navigates to the {@link HomeActivity} if so.
     */
    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if (currentUser != null) {
            Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
            startActivity(intent);

            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);
        buttonLoginEmailPassword = findViewById(R.id.btn_login_password);
        progressBar = findViewById(R.id.progressBar);
        errorText = findViewById(R.id.error);
        forgotPassword = findViewById(R.id.forgot_password);
        signUpNow = findViewById(R.id.sing_up_now);

        firebaseAuth = FirebaseAuth.getInstance();

        /**
         * Listener for the regular "LoginActivity" button with email and password. Authenticates the user with Firebase and navigates to the {@link HomeActivity} upon successful login.
         */
        buttonLoginEmailPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = String.valueOf(editTextEmail.getText());
                String password = String.valueOf(editTextPassword.getText());

                progressBar.setVisibility(View.VISIBLE);

                // Validate inputs
                if (TextUtils.isEmpty(email)) {
                    ToastUtils.showCustomToast(LoginActivity.this, "Please, enter mail");

                    return;
                }

                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    ToastUtils.showCustomToast(LoginActivity.this, "Please, enter a valid mail");

                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    ToastUtils.showCustomToast(LoginActivity.this, "Please, enter password");

                    return;
                }

                /**
                 * Authenticate the user using Firebase Authentication.
                 *
                 * @param email    the email address entered by the user
                 * @param password the password entered by the user
                 * @return a {@link Task<AuthResult>} indicating the success or failure of the login attempt
                 */
                firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressBar.setVisibility(View.INVISIBLE);
                        errorText.setVisibility(View.INVISIBLE);

                        if (task.isSuccessful()) {
                            ToastUtils.showCustomToast(LoginActivity.this, "Login successful");

                            Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                            startActivity(intent);

                            finish();
                        } else {
                            ToastUtils.showCustomToast(LoginActivity.this, "Authentication failed.");
                            errorText.setVisibility(View.VISIBLE);
                        }
                    }
                });
            }
        });

        /**
         * Listener for the "Forgot Password" text. Navigates to the {@link ForgotPasswordActivity} activity.
         */
        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ForgotPasswordActivity.class);
                startActivity(intent);
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
}