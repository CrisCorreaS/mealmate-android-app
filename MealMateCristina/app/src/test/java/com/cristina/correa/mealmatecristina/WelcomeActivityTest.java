package com.cristina.correa.mealmatecristina;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class WelcomeActivityTest {

    private WelcomeActivity activity;

    @Before
    public void setUp() {
        activity = new WelcomeActivity();
        activity.onCreate(null);
    }

    @Test
    public void testUIElementsExist() {
        TextView title = activity.findViewById(R.id.title_slogan);
        assertNotNull("Title TextView should not be null", title);

        Button signInButton = activity.findViewById(R.id.btn_sign_in);
        assertNotNull("Sign-In button should not be null", signInButton);

        LinearLayout googleSignInLayout = activity.findViewById(R.id.btn_sign_in_google);
        assertNotNull("Google Sign-In layout should not be null", googleSignInLayout);

        TextView signUpNow = activity.findViewById(R.id.sing_up_now);
        assertNotNull("\"Sign Up Now\" TextView should not be null", signUpNow);

        ProgressBar progressBar = activity.findViewById(R.id.progressBar);
        assertNotNull("ProgressBar should not be null", progressBar);
        assertEquals("ProgressBar should initially be GONE", View.GONE, progressBar.getVisibility());
    }

    @Test
    public void testSignInButtonClickStartsLoginActivity() {
        Button signInButton = activity.findViewById(R.id.btn_sign_in);
        assertNotNull("Sign-In button should not be null", signInButton);

        signInButton.performClick();

        Intent expectedIntent = new Intent(activity, LoginActivity.class);
        assertEquals("Intent to LoginActivity should match", expectedIntent.getComponent(), activity.getIntent().getComponent());
    }

    @Test
    public void testSignUpNowClickStartsRegisterActivity() {
        TextView signUpNow = activity.findViewById(R.id.sing_up_now);
        assertNotNull("\"Sign Up Now\" TextView should not be null", signUpNow);

        signUpNow.performClick();

        Intent expectedIntent = new Intent(activity, RegisterActivity.class);
        assertEquals("Intent to RegisterActivity should match", expectedIntent.getComponent(), activity.getIntent().getComponent());
    }
}

