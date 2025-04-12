package com.cristina.correa.mealmatecristina.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cristina.correa.mealmatecristina.ListActivity;
import com.cristina.correa.mealmatecristina.R;
import com.cristina.correa.mealmatecristina.models.ShoppingItemModel;
import com.google.firebase.database.DatabaseReference;

import java.util.List;

/**
 * Adapter for managing and displaying a shopping list in a RecyclerView.
 * Each item displays an ingredient name, price, and checkbox for purchase status.
 * 
 * @author Cristina Correa
 * @since 1.0
 */
public class ShoppingListAdapter extends RecyclerView.Adapter<ShoppingListAdapter.ViewHolder> {

    private Context context;
    private List<ShoppingItemModel> shoppingItems;
    private DatabaseReference databaseReference;

    public ShoppingListAdapter(Context context, List<ShoppingItemModel> shoppingItems, DatabaseReference databaseReference) {
        this.context = context;
        this.shoppingItems = shoppingItems;
        this.databaseReference = databaseReference;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.shopping_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ShoppingItemModel item = shoppingItems.get(position);

        holder.ingredientName.setText(item.getIngredientName());
        holder.priceTextView.setText(String.format("£%s", item.getPrice()));

        int drawableRes = getDrawableForType(item.getType());

        if (drawableRes != 0) {
            holder.ingredientName.setCompoundDrawablesWithIntrinsicBounds(drawableRes, 0, 0, 0);
        }

        holder.checkBox.setChecked(item.getIsChecked());

        holder.priceTextView.setOnClickListener(v -> {
            holder.priceTextView.setVisibility(View.GONE);
            holder.priceEditText.setVisibility(View.VISIBLE);
            holder.priceEditText.requestFocus();
        });

        holder.priceEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String newPrice = holder.priceEditText.getText().toString().trim();

                if (!newPrice.isEmpty() && !newPrice.equals(item.getPrice())) {
                    item.setPrice(newPrice);

                    databaseReference.child(item.getId()).child("price").setValue(newPrice);
                }

                holder.priceEditText.setVisibility(View.GONE);
                holder.priceTextView.setText(String.format("£%s", newPrice));
                holder.priceTextView.setVisibility(View.VISIBLE);
            }
        });

        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setChecked(item.getIsChecked());

        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            item.setIsChecked(isChecked);
            databaseReference.child(item.getId()).child("isChecked").setValue(isChecked);

            if (context instanceof ListActivity) {
                ListActivity activity = (ListActivity) context;

                if (isChecked) {
                    activity.moveItemToBoughtList(item);
                } else {
                    activity.moveItemToBuyList(item);
                }
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            int currentPosition = holder.getBindingAdapterPosition();

            if (currentPosition != RecyclerView.NO_POSITION) {
                databaseReference.child(item.getId()).removeValue();
                shoppingItems.remove(currentPosition);

                notifyItemRemoved(currentPosition);
            }

            return true;
        });
    }

    @Override
    public int getItemCount() {
        return shoppingItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView ingredientName;
        TextView priceTextView;
        EditText priceEditText;
        CheckBox checkBox;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            ingredientName = itemView.findViewById(R.id.ingredientName);
            priceTextView = itemView.findViewById(R.id.priceTextView);
            priceEditText = itemView.findViewById(R.id.priceEditText);
            checkBox = itemView.findViewById(R.id.isChecked);
        }
    }

    private int getDrawableForType(String type) {
        switch (type.toLowerCase()) {
            case "beverages":
                return R.drawable.category_beverages;
            case "dairy alternatives":
                return R.drawable.category_dairy_alternatives;
            case "frozen foods":
                return R.drawable.category_frozen_foods;
            case "fruits":
                return R.drawable.category_fruits;
            case "grains & starches":
                return R.drawable.category_grains_starches;
            case "nuts, seeds & legumes":
                return R.drawable.category_nuts_seeds_legumes;
            case "oils & fats":
                return R.drawable.category_oils_fats;
            case "proteins":
                return R.drawable.category_proteins;
            case "snacks & ready to eat":
                return R.drawable.category_snacks_ready_to_eat;
            case "spices & seasonings":
                return R.drawable.category_spices_seassonings;
            case "sweeteners":
                return R.drawable.category_sweeteners;
            case "vegetables":
                return R.drawable.category_vegetables;
            default:
                return R.drawable.category_miscellaneous;
        }
    }
}
