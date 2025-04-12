package com.cristina.correa.mealmatecristina.utils;

import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * A utility class for managing user-related actions, such as retrieving user data from Firebase and displaying the user's profile image.
 * This class provides methods to get the user's name, email, and profile image from Firebase.
 *
 * @author Cristina Correa
 * @since 1.0
 */
public class UserUtils {

    /**
     * Retrieves the current user's name from Firebase Realtime Database.
     *
     * @param callback the callback that is invoked with the user's name when it is retrieved.
     * If no name is found, the callback will be invoked with {@code null}.
     */
    public static void getUserName(UserNameCallback callback) {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(currentUserId);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.hasChild("name")) {
                    String name = dataSnapshot.child("name").getValue(String.class);

                    callback.onNameRetrieved(name);
                } else {
                    callback.onNameRetrieved(null);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onNameRetrieved(null);
            }
        });
    }

    /**
     * Retrieves the current user's email from Firebase Authentication.
     *
     * @return the current user's email, or {@code null} if the user is not logged in.
     */
    public static String getUserEmail() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        return (currentUser != null) ? currentUser.getEmail() : null;
    }

    /**
     * Loads the user profile image from Firebase Storage and displays it in the provided {@link ImageView}.
     *
     * @param imageView the {@link ImageView} where the profile image will be loaded.
     */
    public static void loadUserProfileImage(ImageView imageView) {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(currentUserId);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.hasChild("image")) {
                    String imageUrl = dataSnapshot.child("image").getValue(String.class);

                    Glide.with(imageView.getContext())
                            .load(imageUrl)
                            .into(imageView);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                ToastUtils.showCustomToast(imageView.getContext(), "Failed to load user profile image");
            }
        });
    }

    /**
     * Callback interface used to retrieve the user's name from Firebase.
     */
    public interface UserNameCallback {
        void onNameRetrieved(String name);
    }
}