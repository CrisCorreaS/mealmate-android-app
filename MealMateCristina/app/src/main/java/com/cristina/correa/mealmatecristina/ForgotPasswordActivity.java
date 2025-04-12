package com.cristina.correa.mealmatecristina;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.cristina.correa.mealmatecristina.utils.ToastUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

/**
 * Represents the Forgot Password screen for the MealMate application.
 * Allows users to reset their password by sending a password reset email through Firebase Authentication.
 *
 * @author Cristina Correa
 * @since 1.0
 */
public class ForgotPasswordActivity extends AppCompatActivity {

    private TextView logInNow;
    private EditText editTextEmail;
    private Button buttonForgotPassword;

    private FirebaseAuth firebaseAuth;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forgot_password);

        logInNow = findViewById(R.id.log_in_now);
        editTextEmail = findViewById(R.id.email);
        buttonForgotPassword = findViewById(R.id.btn_login_password);

        firebaseAuth = FirebaseAuth.getInstance();

        buttonForgotPassword.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                String email = editTextEmail.getText().toString();

                if (TextUtils.isEmpty(email)) {
                    ToastUtils.showCustomToast(ForgotPasswordActivity.this, "Please, enter mail");

                    return;
                }

                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    ToastUtils.showCustomToast(ForgotPasswordActivity.this, "Please, enter a valid mail");

                    return;
                }

                /**
                 * Sends a password reset email using Firebase Authentication.
                 *
                 * @param email the email address of the user who wants to reset their password
                 * @return a {@link Task<Void>} indicating the success or failure of the email sending attempt
                 */
                firebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            ToastUtils.showCustomToast(ForgotPasswordActivity.this, "Please, check your email");
                        } else {
                            ToastUtils.showCustomToast(ForgotPasswordActivity.this, "Sorry! Something went wrong");
                        }
                    }
                });
            }
        });

        /**
         * Listener for the "Back to LoginActivity" text, which navigates to the {@link LoginActivity} activity.
         */
        logInNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
            }
        });
    }
}