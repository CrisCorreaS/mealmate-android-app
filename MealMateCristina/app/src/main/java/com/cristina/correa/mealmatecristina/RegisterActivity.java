package com.cristina.correa.mealmatecristina;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.cristina.correa.mealmatecristina.models.UserModel;
import com.cristina.correa.mealmatecristina.utils.ToastUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Represents the LoginActivity screen for the MealMate application.
 * Creates a new user using Firebase Authentication and navigation between LoginActivity and Registration screens.
 *
 * @author Cristina Correa
 * @since 1.0
 */
public class RegisterActivity extends AppCompatActivity {

    private EditText editTextName, editTextEmail, editTextPassword, editTextPasswordConfirm;
    private TextView error1, error2, error3, logInNow;
    private Button buttonRegister;
    private ProgressBar progressBar;
    private CheckBox agreeTermsCheckBox;


    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;

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

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        editTextName = findViewById(R.id.name);
        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);
        editTextPasswordConfirm = findViewById(R.id.passwordConfirm);
        error1 = findViewById(R.id.error1);
        error2 = findViewById(R.id.error2);
        error3 = findViewById(R.id.error3);
        agreeTermsCheckBox = findViewById(R.id.agree_terms);
        buttonRegister = findViewById(R.id.btn_register);
        progressBar = findViewById(R.id.progressBar);
        logInNow = findViewById(R.id.log_in_now);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();

        error1.setVisibility(View.GONE);
        error2.setVisibility(View.GONE);
        error3.setVisibility(View.GONE);

        /**
         * Listener for the "RegisterActivity" button. Creates a new user account with Firebase Authentication and navigates to the {@link LoginActivity} upon successful account creation.
         */
        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = String.valueOf(editTextName.getText());
                String email = String.valueOf(editTextEmail.getText());
                String password = String.valueOf(editTextPassword.getText());
                String passwordConfirm = String.valueOf(editTextPasswordConfirm.getText());

                progressBar.setVisibility(View.VISIBLE);

                if (TextUtils.isEmpty(name)) {
                    ToastUtils.showCustomToast(RegisterActivity.this, "Please, enter name");
                    progressBar.setVisibility(View.INVISIBLE);

                    return;
                }

                if (TextUtils.isEmpty(email)) {
                    ToastUtils.showCustomToast(RegisterActivity.this, "Please, enter mail");
                    progressBar.setVisibility(View.INVISIBLE);

                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    ToastUtils.showCustomToast(RegisterActivity.this, "Please, enter password");
                    progressBar.setVisibility(View.INVISIBLE);

                    return;
                }

                if (!password.equals(passwordConfirm)) {
                    ToastUtils.showCustomToast(RegisterActivity.this, "Passwords do not match");
                    progressBar.setVisibility(View.INVISIBLE);
                    return;
                }

                // Password validation
                if (!validatePassword(name, email, password)) {
                    progressBar.setVisibility(View.INVISIBLE);
                    return;
                }

                /**
                 * Creates a new user account using Firebase Authentication.
                 *
                 * @param email    the email address entered by the user
                 * @param password the password entered by the user
                 * @return a {@link Task<AuthResult>} indicating the success or failure of the registration attempt
                 */
                firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressBar.setVisibility(View.INVISIBLE);

                        if (task.isSuccessful()) {
                            UserModel userModel = new UserModel(name, email, password);
                            String id = task.getResult().getUser().getUid();

                            firebaseDatabase.getReference().child("Users").child(id).setValue(userModel);

                            ToastUtils.showCustomToast(RegisterActivity.this, "Account created.");

                            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                            startActivity(intent);

                            finish();
                        } else {
                            ToastUtils.showCustomToast(RegisterActivity.this, "Authentication failed.");
                        }
                    }
                });
            }
        });

        /**
         * Listener for the "Sign Up Now" text, which navigates to the {@link LoginActivity} activity.
         */
        logInNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
            }
        });

        styleCheckBoxText();
        addLinksToCheckboxText();

        Intent intent = getIntent();
        if (intent != null && intent.getBooleanExtra("TERMS_ACCEPTED", false)) {
            agreeTermsCheckBox.setChecked(true);
        }
    }

    /**
     * Validates the password based on the following criteria:
     *
     * @param name     The user's name.
     * @param email    The user's email.
     * @param password The password to validate.
     * @return {@code true} if the password meets all criteria; {@code false} otherwise.
     */
    private boolean validatePassword(String name, String email, String password) {
        boolean passedFirstCriteria = passwordLengthIsCorrect(name, email, password);
        boolean passedSecondCriteria = passwordDoesNotHaveNameOrEmail(name, email, password);
        boolean passedThirdCriteria = passwordContainsSymbolAndNumber(name, email, password);

        return passedFirstCriteria && passedSecondCriteria && passedThirdCriteria;
    }

    /**
     * Checks if the password length is correct.
     *
     * @param name     The user's name (not used in this method).
     * @param email    The user's email (not used in this method).
     * @param password The password to check.
     * @return {@code true} if the password length is at least 12 characters; {@code false} otherwise.
     * */
    private boolean passwordLengthIsCorrect(String name, String email, String password) {
        if (password.length() >= 12) {
            showError(error1, true);

            return true;
        }

        showError(error1, false);

        return false;
    }

    /**
     * Checks if the password does not contain the name or email.
     *
     * @param name     The user's name.
     * @param email    The user's email.
     * @param password The password to check.
     * @return {@code true} if the password does not include the name or email; {@code false} otherwise.
     */
    private boolean passwordDoesNotHaveNameOrEmail(String name, String email, String password) {
        if (!password.toLowerCase().contains(name.toLowerCase()) && !password.toLowerCase().contains(email.toLowerCase())) {
            showError(error2, true);

            return true;
        }
        showError(error2, false);

        return false;
    }

    /**
     * Checks if the password contains at least one symbol and one number
     *
     * @param name     The user's name (not used in this method).
     * @param email    The user's email (not used in this method).
     * @param password The password to check.
     * @return {@code true} if the password includes at least one symbol and one number; {@code false} otherwise.
     */
    private boolean passwordContainsSymbolAndNumber(String name, String email, String password) {
        boolean hasSymbol = password.matches(".*[!@#$%^&*(),.?\":{}|<>].*");
        boolean hasNumber = password.matches(".*\\d.*");

        if (hasSymbol && hasNumber) {
            showError(error3, true);

            return true;
        }
        showError(error3, false);

        return false;
    }

    /**
     * Displays an error message and styles the corresponding UI element based on the validation result.
     *
     * @param error  The {@link TextView} to display the error message.
     * @param result The result of the validation check. {@code true} for success; {@code false} for failure.
     */
    private void showError(TextView error, boolean result) {
        if (result) {
            error.setTextColor(ContextCompat.getColor(this, R.color.primary));
            error.setCompoundDrawablesWithIntrinsicBounds(R.drawable.do_check_circle_yes, 0, 0, 0);
        } else {
            error.setTextColor(ContextCompat.getColor(this, R.color.text_dark));
            error.setCompoundDrawablesWithIntrinsicBounds(R.drawable.do_check_circle_no, 0, 0, 0);
        }
        error.setVisibility(View.VISIBLE);
    }

    /**
     * Styles the text of a {@link CheckBox} to emphasize clickable "Terms of Use" and "Privacy Policy" links.
     */
    private void styleCheckBoxText() {
        String textCheckbox = getString(R.string.agree_terms_privacy);
        SpannableString spannableString = new SpannableString(textCheckbox);

        int text1Start = textCheckbox.indexOf("Agree to the ");
        int text1End = text1Start + "Agree to the ".length();
        int termsStart = textCheckbox.indexOf("Terms of Use ");
        int termsEnd = termsStart + "Terms of Use ".length();
        int text2Start = textCheckbox.indexOf("and");
        int text2End = text2Start + "and".length();
        int privacyStart = textCheckbox.indexOf(" Privacy Policy");
        int privacyEnd = privacyStart + " Privacy Policy".length();

        spannableString.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.primary_dark)), text1Start, text1End, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.primary)), termsStart, termsEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.primary_dark)), text2Start, text2End, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.primary)), privacyStart, privacyEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        agreeTermsCheckBox.setText(spannableString);
    }

    /**
     * Adds clickable links to the "Terms of Use" and "Privacy Policy" text within a {@link CheckBox}.
     * These links navigate to the appropriate activity when clicked.
     */
    private void addLinksToCheckboxText() {
        String textCheckbox = getString(R.string.agree_terms_privacy);
        SpannableString spannableString = new SpannableString(textCheckbox);

        int termsStart = textCheckbox.indexOf("Terms of Use");
        int termsEnd = termsStart + "Terms of Use".length();
        int privacyStart = textCheckbox.indexOf("Privacy Policy");
        int privacyEnd = privacyStart + "Privacy Policy".length();

        spannableString.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                Intent intent = new Intent(RegisterActivity.this, TermsPrivacyActivity.class);
                startActivity(intent);
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(ContextCompat.getColor(RegisterActivity.this, R.color.primary));
                ds.setUnderlineText(false);
            }
        }, termsStart, termsEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        spannableString.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                Intent intent = new Intent(RegisterActivity.this, TermsPrivacyActivity.class);
                startActivity(intent);
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(ContextCompat.getColor(RegisterActivity.this, R.color.primary));
                ds.setUnderlineText(false);
            }
        }, privacyStart, privacyEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        agreeTermsCheckBox.setText(spannableString);

        agreeTermsCheckBox.setMovementMethod(LinkMovementMethod.getInstance());

        agreeTermsCheckBox.setOnClickListener(v -> {
            if (v instanceof CheckBox) {
                CheckBox checkBox = (CheckBox) v;

                if (!checkBox.isPressed()) {
                    checkBox.toggle();
                }
            }
        });

    }
}