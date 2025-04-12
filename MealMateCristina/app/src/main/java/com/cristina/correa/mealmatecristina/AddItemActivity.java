package com.cristina.correa.mealmatecristina;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cristina.correa.mealmatecristina.adapters.CategoryAdapter;
import com.cristina.correa.mealmatecristina.models.CategoryModel;
import com.cristina.correa.mealmatecristina.models.ShoppingItemModel;
import com.cristina.correa.mealmatecristina.utils.ToastUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity for adding new items to the shopping list.
 * This class handles user interactions for entering item details, selecting a category,
 * and saving the item to Firebase Realtime Database. It also provides functionality for navigating
 * to other screens and loading available categories from Firestore.
 *
 * @author Cristina Correa
 * @since 1.0
 */
public class AddItemActivity extends AppCompatActivity {

    private TextView toolbarTittleCross;
    private ImageView toolbarBinIcon;
    private EditText ingredientNameEditText, ingredientPriceEditText;
    private FloatingActionButton addItemButton;
    private RecyclerView categoryRecyclerView;

    private DatabaseReference shoppingListRef;
    private CategoryAdapter categoryAdapter;

    /**
     * Initializes the activity, sets up the UI components, and configures Firebase connections.
     *
     * @param savedInstanceState the saved instance state bundle.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_item);

        toolbarTittleCross = findViewById(R.id.toolbar_title);
        toolbarBinIcon = findViewById(R.id.toolbar_menu_icon);
        ingredientNameEditText = findViewById(R.id.ingredient_name);
        ingredientPriceEditText = findViewById(R.id.ingredient_price);
        addItemButton = findViewById(R.id.add_item);

        categoryRecyclerView = findViewById(R.id.category_recycler_view);
        categoryRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadCategoriesFromFirestore();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();

            shoppingListRef = FirebaseDatabase.getInstance()
                    .getReference("ShoppingLists")
                    .child(userId)
                    .child("ingredients");
        } else {
            ToastUtils.showCustomToast(this, "User not logged in. Please log in to continue.");
            finish();

            return;
        }

        toolbarTittleCross.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                startActivity(intent);

                finish();
            }
        });

        toolbarBinIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ListActivity.class);
                startActivity(intent);

                finish();
            }
        });

        addItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewItemToDatabase();
            }
        });
    }

    /**
     * Loads available categories from Firestore and displays them in the RecyclerView.
     */
    private void loadCategoriesFromFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("categories")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<CategoryModel> categories = new ArrayList<>();
                        QuerySnapshot snapshot = task.getResult();

                        if (snapshot != null) {
                            for (DocumentSnapshot doc : snapshot.getDocuments()) {
                                CategoryModel category = doc.toObject(CategoryModel.class);

                                if (category != null) {
                                    categories.add(category);
                                }
                            }

                            categoryAdapter = new CategoryAdapter(categories);
                            categoryRecyclerView.setAdapter(categoryAdapter);
                        }
                    } else {
                        ToastUtils.showCustomToast(this, "Failed to load categories. Please try again");
                    }
                });
    }

    /**
     * Adds a new item to the Firebase Realtime Database with the entered details.
     * Validates the user input and displays appropriate messages in case of errors.
     */
    private void addNewItemToDatabase() {
        String ingredientName = ingredientNameEditText.getText().toString().trim();
        String ingredientPrice = ingredientPriceEditText.getText().toString().trim();
        String selectedCategory = categoryAdapter != null ? categoryAdapter.getSelectedCategory() : null;

        if (TextUtils.isEmpty(ingredientName)) {
            ToastUtils.showCustomToast(this, "Please, enter an ingredient name");
            return;
        }

        if (TextUtils.isEmpty(selectedCategory)) {
            ToastUtils.showCustomToast(this, "Please select a category");
            return;
        }

        String id = shoppingListRef.push().getKey();
        if (id == null) {
            ToastUtils.showCustomToast(this, "Failed to generate item ID. Please try again");
            return;
        }

        ShoppingItemModel newItem = new ShoppingItemModel(id, ingredientName, ingredientPrice, selectedCategory, false);

        shoppingListRef.child(id).setValue(newItem).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                ToastUtils.showCustomToast(this, "Item added successfully!");
                finish();
            } else {
                ToastUtils.showCustomToast(this, "Failed to add item. Please try again");
            }
        });
    }
}