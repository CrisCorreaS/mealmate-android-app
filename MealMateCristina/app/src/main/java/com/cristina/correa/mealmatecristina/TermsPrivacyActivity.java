package com.cristina.correa.mealmatecristina;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.cristina.correa.mealmatecristina.utils.ToastUtils;

/**
 * Represents the TermsPrivacyActivity screen for the MealMate application.
 *
 * @author Cristina Correa
 * @since 1.0
 */
public class TermsPrivacyActivity extends AppCompatActivity {

    Button acceptButton, declineButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_terms_privacy);

        acceptButton = findViewById(R.id.acceptButton);
        declineButton = findViewById(R.id.declineButton);

        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ToastUtils.showCustomToast(TermsPrivacyActivity.this, "Terms Accepted");

                Intent intent = new Intent(TermsPrivacyActivity.this, RegisterActivity.class);
                intent.putExtra("TERMS_ACCEPTED", true);
                startActivity(intent);

                finish();
            }
        });

        declineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ToastUtils.showCustomToast(TermsPrivacyActivity.this, "Terms Declined");

                Intent intent = new Intent(TermsPrivacyActivity.this, WelcomeActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}