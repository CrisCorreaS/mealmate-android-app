package com.cristina.correa.mealmatecristina;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cristina.correa.mealmatecristina.adapters.ShoppingListAdapter;
import com.cristina.correa.mealmatecristina.models.ShoppingItemModel;
import com.cristina.correa.mealmatecristina.utils.ToastUtils;
import com.cristina.correa.mealmatecristina.utils.UserUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * This activity allows users to view their shopping lists, move items between
 * "To Buy" and "Already Bought" lists, edit, delete, and share items. It also
 * includes functionality for detecting shake gestures to clear the list.
 *
 * @author Cristina Correa
 * @version 1.0
 * @since 1.0
 */
public class ListActivity extends AppCompatActivity implements SensorEventListener {

    private static final float SHAKE_THRESHOLD = 12.0f;
    private static final int SHAKE_WAIT_TIME_MS = 500;

    private ImageView userProfilePictureImageView, toolbarMenuIcon, boughtOptions;

    private RecyclerView itemsToBuyRecyclerView, itemsAlreadyBoughtRecyclerView;
    private List<ShoppingItemModel> itemsToBuy;
    private List<ShoppingItemModel> itemsAlreadyBought;
    private ShoppingListAdapter itemsToBuyAdapter, itemsAlreadyBoughtAdapter;

    private DatabaseReference shoppingListRef;

    private FloatingActionButton addItemButton;

    private final Handler handler = new Handler();

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private long lastShakeTime = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_list);

        userProfilePictureImageView = findViewById(R.id.user_profile_picture);

        itemsToBuyRecyclerView = findViewById(R.id.items_to_buy);
        itemsAlreadyBoughtRecyclerView = findViewById(R.id.items_already_bought);

        itemsToBuy = new ArrayList<>();
        itemsAlreadyBought = new ArrayList<>();

        toolbarMenuIcon = findViewById(R.id.toolbar_menu_icon);
        boughtOptions = findViewById(R.id.bought_options);
        addItemButton = findViewById(R.id.add_item);

        UserUtils.loadUserProfileImage(userProfilePictureImageView);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.bottom_list);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            shoppingListRef = FirebaseDatabase.getInstance().getReference("ShoppingLists").child(uid).child("ingredients");

            loadShoppingList();
        } else {
            ToastUtils.showCustomToast(this, "You need to be logged to see your shopping list.");
        }

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        itemsToBuyRecyclerView.setLayoutManager(layoutManager);
        LinearLayoutManager layoutManagerAlreadyBought = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        itemsAlreadyBoughtRecyclerView.setLayoutManager(layoutManagerAlreadyBought);

        itemsToBuyAdapter = new ShoppingListAdapter(this, itemsToBuy, shoppingListRef);
        itemsAlreadyBoughtAdapter = new ShoppingListAdapter(this, itemsAlreadyBought, shoppingListRef);

        itemsToBuyRecyclerView.setAdapter(itemsToBuyAdapter);
        itemsAlreadyBoughtRecyclerView.setAdapter(itemsAlreadyBoughtAdapter);

        userProfilePictureImageView.setOnClickListener(v -> {
            Intent intent = new Intent(ListActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.bottom_home) {
                startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                finish();

                return true;
            } else if (itemId == R.id.bottom_discover) {
                startActivity(new Intent(getApplicationContext(), DiscoverActivity.class));
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                finish();

                return true;
            } else if (itemId == R.id.bottom_list) {
                return true;
            } else if (itemId == R.id.bottom_profile) {
                startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();

                return true;
            }
            return false;
        });

        /**
         * Shows the toolbar menu when the menu icon is clicked
         */
        toolbarMenuIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showToolbarMenu(view);
            }
        });

        /**
         * Shows the "Bought Options" menu when the "Bought Options" text is clicked
         */
        boughtOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showBoughtOptionsMenu(view);
            }
        });

        /**
         * Starts the AddItemActivity when the "+" button is clicked
         */
        addItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ListActivity.this, AddItemActivity.class);
                startActivity(intent);
            }
        });

        setupSwipeHandlers();

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }
    }

    /**
     * Loads the shopping list from Firebase and updates the adapter accordingly.
     */
    private void loadShoppingList() {
        shoppingListRef.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                itemsToBuy.clear();
                itemsAlreadyBought.clear();

                for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                    ShoppingItemModel item = itemSnapshot.getValue(ShoppingItemModel.class);
                    assert item != null;

                    item.setId(itemSnapshot.getKey());

                    if (item.getIsChecked()) {
                        itemsAlreadyBought.add(item);
                    } else {
                        itemsToBuy.add(item);
                    }
                }

                handler.post(() -> {
                    itemsToBuyAdapter.notifyDataSetChanged();
                    itemsAlreadyBoughtAdapter.notifyDataSetChanged();
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                ToastUtils.showCustomToast(ListActivity.this, "Error loading the shopping list: " + error.getMessage());
            }
        });
    }

    /**
     * Displays the toolbar menu when the toolbar menu icon is clicked.
     *
     * @param view the view that triggered the event.
     */
    private void showToolbarMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        MenuInflater inflater = popupMenu.getMenuInflater();

        inflater.inflate(R.menu.toolbar_options_menu, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                return handleToolbarMenuClick(item);
            }
        });

        popupMenu.show();
    }

    /**
     * Handles click events for items in the toolbar menu.
     *
     * @param item the selected menu item.
     * @return true if the event was handled successfully.
     */
    private boolean handleToolbarMenuClick(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.share_list) {
            if (shareShoppingList()) {
                ToastUtils.showCustomToast(this, "List shared");
                return true;
            }
        } else if (itemId == R.id.clear_list) {
            clearList();

            ToastUtils.showCustomToast(this, "List cleared");
            return true;
        } else if (itemId == R.id.delete_list) {
            deleteList();

            ToastUtils.showCustomToast(this, "List deleted");
            return true;
        }

        return false;
    }

    /**
     * Displays the "Bought Options" menu.
     *
     * @param view the view that triggered the event.
     */
    private void showBoughtOptionsMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        MenuInflater inflater = popupMenu.getMenuInflater();

        inflater.inflate(R.menu.bought_options_menu, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                return handleBoughtOptionsClick(item);
            }
        });
        popupMenu.show();
    }

    /**
     * Handles click events for items in the "Bought Options" menu.
     *
     * @param item the selected menu item.
     * @return true if the event was handled successfully.
     */
    private boolean handleBoughtOptionsClick(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.remove_all) {
            removeAllBoughtItems();

            ToastUtils.showCustomToast(this, "All bought items removed");
            return true;
        } else if (itemId == R.id.add_all) {
            addAllBoughtItemsToBuy();

            ToastUtils.showCustomToast(this, "All bought items added to the grocery list");
            return true;
        }

        return false;
    }

    /**
     * Moves an item from the "To Buy" list to the "Already Bought" list.
     *
     * @param item the shopping item to move.
     */
    public void moveItemToBoughtList(ShoppingItemModel item) {
        handler.post(() -> {
            if (itemsToBuy.remove(item)) {
                itemsAlreadyBought.add(item);
                itemsToBuyAdapter.notifyDataSetChanged();
                itemsAlreadyBoughtAdapter.notifyDataSetChanged();
            }
        });
    }

    /**
     * Moves an item from the "Already Bought" list to the "To Buy" list.
     *
     * @param item the shopping item to move.
     */
    public void moveItemToBuyList(ShoppingItemModel item) {
        handler.post(() -> {
            if (itemsAlreadyBought.remove(item)) {
                itemsToBuy.add(item);
                itemsToBuyAdapter.notifyDataSetChanged();
                itemsAlreadyBoughtAdapter.notifyDataSetChanged();
            }
        });
    }

    /**
     * Shares the shopping list with others via SMS.
     *
     * @return true if the sharing operation was successful.
     */
    private boolean shareShoppingList() {
        if (itemsToBuy.isEmpty()) {
            ToastUtils.showCustomToast(this, "Your shopping list is empty!");
            return false;
        }

        StringBuilder messageBuilder = new StringBuilder("Hi! Here's my shopping list:\n");

        for (ShoppingItemModel item : itemsToBuy) {
            if (!item.getIsChecked()) {
                String price = item.getPrice().trim();

                if (!price.isEmpty()) {
                    messageBuilder.append("\n-")
                            .append(item.getIngredientName())
                            .append(" (£")
                            .append(price)
                            .append(")");
                } else {
                    messageBuilder.append("\n-").append(item.getIngredientName());
                }
            }
        }

        if (messageBuilder.length() > 0 && messageBuilder.charAt(messageBuilder.length() - 2) == ',') {
            messageBuilder.setLength(messageBuilder.length() - 2);
        }

        String message = messageBuilder.toString().trim();
        if (message.isEmpty()) {
            ToastUtils.showCustomToast(this, "There are no items to share");
            return false;
        }

        Intent smsIntent = new Intent(Intent.ACTION_SENDTO);
        smsIntent.setData(Uri.parse("smsto:"));
        smsIntent.putExtra("sms_body", message);

        try {
            startActivity(smsIntent);
        } catch (ActivityNotFoundException e) {
            ToastUtils.showCustomToast(this, "No SMS app found to share the list");
        } catch (Exception e) {
            ToastUtils.showCustomToast(this, "Unexpected error occurred: " + e.getMessage());
        }

        return true;
    }

    /**
     * Clears the "To Buy" list by moving all items to the "Already Bought" list.
     */
    private void clearList() {
        handler.post(() -> {
            for (ShoppingItemModel shoppingItemModel : new ArrayList<>(itemsToBuy)) {
                shoppingItemModel.setIsChecked(true);

                shoppingListRef.child(shoppingItemModel.getId()).child("isChecked").setValue(true);
                itemsAlreadyBought.add(shoppingItemModel);
            }

            itemsToBuy.clear();
            itemsToBuyAdapter.notifyDataSetChanged();
            itemsAlreadyBoughtAdapter.notifyDataSetChanged();
        });
    }

    /**
     * Deletes all items from both lists and removes them from Firebase.
     */
    private void deleteList() {
        handler.post(() -> {
            for (ShoppingItemModel shoppingItemModel : new ArrayList<>(itemsToBuy)) {
                shoppingListRef.child(shoppingItemModel.getId()).removeValue();
            }

            for (ShoppingItemModel shoppingItemModel : new ArrayList<>(itemsAlreadyBought)) {
                shoppingListRef.child(shoppingItemModel.getId()).removeValue();
            }

            itemsToBuy.clear();
            itemsAlreadyBought.clear();
            itemsToBuyAdapter.notifyDataSetChanged();
            itemsAlreadyBoughtAdapter.notifyDataSetChanged();
        });
    }

    /**
     * Removes all items from the "Already Bought" list and deletes them from Firebase.
     */
    private void removeAllBoughtItems() {
        handler.post(() -> {
            for (ShoppingItemModel shoppingItemModel : new ArrayList<>(itemsAlreadyBought)) {
                shoppingListRef.child(shoppingItemModel.getId()).removeValue();
            }

            itemsAlreadyBought.clear();
            itemsAlreadyBoughtAdapter.notifyDataSetChanged();
        });
    }

    /**
     * Moves all items from the "Already Bought" list to the "To Buy" list
     */
    private void addAllBoughtItemsToBuy() {
        handler.post(() -> {
            for (ShoppingItemModel shoppingItemModel : new ArrayList<>(itemsAlreadyBought)) {
                shoppingListRef.child(shoppingItemModel.getId()).child("isChecked").setValue(false);

                shoppingItemModel.setIsChecked(false);
                itemsToBuy.add(shoppingItemModel);
            }

            itemsAlreadyBought.clear();
            itemsToBuyAdapter.notifyDataSetChanged();
            itemsAlreadyBoughtAdapter.notifyDataSetChanged();
        });
    }

    /**
     * Sets up swipe handlers for the "To Buy" list items.
     * Handles left swipe to delete and right swipe to edit.
     */
    private void setupSwipeHandlers() {
        ItemTouchHelper.SimpleCallback swipeCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();

                if (position >= 0 && position < itemsToBuy.size()) {
                    ShoppingItemModel item = itemsToBuy.get(position);

                    if (direction == ItemTouchHelper.LEFT) {
                        deleteItem(item, position);
                    } else if (direction == ItemTouchHelper.RIGHT) {
                        editItem(item, position);
                    }

                } else {
                    itemsToBuyAdapter.notifyDataSetChanged();
                    ToastUtils.showCustomToast(ListActivity.this, "Invalid swipe action");
                }
            }

            /**
             * Draws the swipe background and text on the RecyclerView item
             *
             * @param c                 The canvas which RecyclerView is drawing its children
             * @param recyclerView      The RecyclerView to which ItemTouchHelper is attached to
             * @param viewHolder        The ViewHolder which is being interacted by the User
             * @param dX                The amount of horizontal displacement caused by user's action
             * @param dY                The amount of vertical displacement caused by user's action
             * @param actionState       The type of interaction on the View
             * @param isCurrentlyActive True if this view is currently being controlled by the user
             */
            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                Paint paint = new Paint();
                View itemView = viewHolder.itemView;

                if (dX > 0) {
                    paint.setColor(Color.BLUE);
                    c.drawRect((float) itemView.getLeft(), (float) itemView.getTop(), dX, (float) itemView.getBottom(), paint);

                    drawTextWithEmoji(c, "✏️ Edit", itemView.getLeft() + 50, itemView.getTop() + (itemView.getHeight() / 2), Color.WHITE);
                } else {
                    paint.setColor(Color.RED);
                    c.drawRect((float) itemView.getRight() + dX, (float) itemView.getTop(), (float) itemView.getRight(), (float) itemView.getBottom(), paint);

                    drawTextWithEmoji(c, "🗑️ Delete", itemView.getRight() - 300, itemView.getTop() + (itemView.getHeight() / 2), Color.WHITE);
                }

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }

            /**
             * Draws text with an emoji on the canvas
             *
             * @param canvas     The canvas to draw on
             * @param text       The text to draw
             * @param x          The x-coordinate of the text
             * @param y          The y-coordinate of the text
             * @param textColor  The color of the text
             */
            private void drawTextWithEmoji(Canvas canvas, String text, float x, float y, int textColor) {
                Paint textPaint = new Paint();

                textPaint.setColor(textColor);
                textPaint.setTextSize(48);
                textPaint.setTextAlign(Paint.Align.LEFT);
                textPaint.setTypeface(Typeface.DEFAULT_BOLD);

                Typeface customFont = ResourcesCompat.getFont(getApplicationContext(), R.font.calistoga_regular);

                if (customFont != null) {
                    textPaint.setTypeface(customFont);
                }

                canvas.drawText(text, x, y, textPaint);
            }
        };

        new ItemTouchHelper(swipeCallback).attachToRecyclerView(itemsToBuyRecyclerView);
    }

    /**
     * Deletes an item from the "To Buy" list.
     *
     * @param item     the shopping item to delete.
     * @param position the position of the item in the list.
     */
    private void deleteItem(ShoppingItemModel item, int position) {
        if (position >= 0 && position < itemsToBuy.size()) {
            shoppingListRef.child(item.getId()).removeValue().addOnSuccessListener(unused -> {
                itemsToBuy.remove(position);
                itemsToBuyAdapter.notifyItemRemoved(position);

                if (position < itemsToBuy.size()) {
                    itemsToBuyAdapter.notifyItemRangeChanged(position, itemsToBuy.size());
                }

                ToastUtils.showCustomToast(this, "Item deleted");
            }).addOnFailureListener(e -> ToastUtils.showCustomToast(this, "Failed to delete item"));
        } else {
            ToastUtils.showCustomToast(this, "Item not found");
        }
    }

    /**
     * Edits an item in the "To Buy" list.
     *
     * @param item     the shopping item to edit.
     * @param position the position of the item in the list.
     */
    private void editItem(ShoppingItemModel item, int position) {
        Intent intent = new Intent(ListActivity.this, EditItemActivity.class);
        intent.putExtra("shoppingItem", item);
        startActivity(intent);

        itemsToBuyAdapter.notifyItemChanged(position);
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    /**
     * Called when the accelerometer sensor detects a change.
     *
     * @param event the sensor event.
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            float acceleration = (float) Math.sqrt(x * x + y * y + z * z) - SensorManager.GRAVITY_EARTH;

            if (acceleration > SHAKE_THRESHOLD) {
                long currentTime = System.currentTimeMillis();

                if (currentTime - lastShakeTime > SHAKE_WAIT_TIME_MS) {
                    lastShakeTime = currentTime;

                    clearList();
                    ToastUtils.showCustomToast(this, "All items cleared");
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

}