package com.cristina.correa.mealmatecristina;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
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
 * Activity for editing an item in the shopping list.
 * Provides a user interface to update item details such as name, price, and category.
 *
 * @author Cristina Correa
 * @since 1.0
 */
public class EditItemActivity extends AppCompatActivity {

    private TextView toolbarTitle;
    private ImageView toolbarBinIcon;
    private EditText ingredientNameEditText, ingredientPriceEditText;
    private FloatingActionButton updateItemButton;
    private RecyclerView categoryRecyclerView;

    private DatabaseReference shoppingListRef;
    private CategoryAdapter categoryAdapter;
    private ShoppingItemModel shoppingItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_item);

        toolbarTitle = findViewById(R.id.toolbar_title);
        toolbarBinIcon = findViewById(R.id.toolbar_menu_icon);
        ingredientNameEditText = findViewById(R.id.ingredient_name);
        ingredientPriceEditText = findViewById(R.id.ingredient_price);
        updateItemButton = findViewById(R.id.add_item);
        categoryRecyclerView = findViewById(R.id.category_recycler_view);
        categoryRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        Intent intent = getIntent();
        shoppingItem = (ShoppingItemModel) intent.getSerializableExtra("shoppingItem");

        ingredientNameEditText.setText(shoppingItem != null ? shoppingItem.getIngredientName() : "");
        ingredientPriceEditText.setText(shoppingItem != null ? shoppingItem.getPrice() : "");

        loadCategoriesFromFirestore();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();

            shoppingListRef = FirebaseDatabase.getInstance()
                    .getReference("ShoppingLists")
                    .child(userId)
                    .child("ingredients");
        } else {
            ToastUtils.showCustomToast(this, "User not logged in. Please log in to continue");
            finish();

            return;
        }

        toolbarTitle.setOnClickListener(view -> {
            Intent homeIntent = new Intent(getApplicationContext(), HomeActivity.class);
            startActivity(homeIntent);

            finish();
        });

        toolbarBinIcon.setOnClickListener(view -> {
            Intent listIntent = new Intent(getApplicationContext(), ListActivity.class);
            startActivity(listIntent);

            finish();
        });

        updateItemButton.setOnClickListener(v -> updateItemInDatabase());
    }

    /**
     * Loads the categories from Firestore to populate the category selection list.
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

                            if (shoppingItem != null && categoryAdapter != null) {
                                categoryAdapter.setSelectedCategory(shoppingItem.getType());
                            }
                        }
                    } else {
                        ToastUtils.showCustomToast(this, "Failed to load categories. Please try again");
                    }
                });
    }

    /**
     * Updates the shopping item in the database with the modified details.
     */
    private void updateItemInDatabase() {
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

        shoppingItem.setIngredientName(ingredientName);
        shoppingItem.setPrice(ingredientPrice);
        shoppingItem.setType(selectedCategory);

        shoppingListRef.child(shoppingItem.getId()).setValue(shoppingItem).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                ToastUtils.showCustomToast(this, "Item updated successfully!");
                finish();
            } else {
                ToastUtils.showCustomToast(this, "Failed to update item. Please try again");
            }
        });
    }
}