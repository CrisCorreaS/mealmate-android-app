package com.cristina.correa.mealmatecristina;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import android.content.Intent;
import android.widget.Button;

import org.junit.Before;
import org.junit.Test;

public class TermsPrivacyActivityTest {

    private TermsPrivacyActivity activity;

    @Before
    public void setUp() {
        activity = new TermsPrivacyActivity();
        activity.onCreate(null);
    }

    @Test
    public void testUIElementsExist() {
        Button acceptButton = activity.findViewById(R.id.acceptButton);
        assertNotNull("Accept Button should not be null", acceptButton);

        Button declineButton = activity.findViewById(R.id.declineButton);
        assertNotNull("Decline Button should not be null", declineButton);
    }

    @Test
    public void testAcceptButtonClickStartsRegisterActivity() {
        Button acceptButton = activity.findViewById(R.id.acceptButton);
        assertNotNull("Accept Button should not be null", acceptButton);

        acceptButton.performClick();

        Intent expectedIntent = new Intent(activity, RegisterActivity.class);
        expectedIntent.putExtra("TERMS_ACCEPTED", true);

        assertEquals("Intent to RegisterActivity should match", expectedIntent.getComponent(), activity.getIntent().getComponent());
    }

    @Test
    public void testDeclineButtonClickStartsWelcomeActivity() {
        Button declineButton = activity.findViewById(R.id.declineButton);
        assertNotNull("Decline Button should not be null", declineButton);

        declineButton.performClick();

        Intent expectedIntent = new Intent(activity, WelcomeActivity.class);

        assertEquals("Intent to WelcomeActivity should match", expectedIntent.getComponent(), activity.getIntent().getComponent());
    }
}