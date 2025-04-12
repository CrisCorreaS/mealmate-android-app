package com.cristina.correa.mealmatecristina.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cristina.correa.mealmatecristina.R;
import com.cristina.correa.mealmatecristina.models.CategoryModel;

import java.util.List;

/**
 * Adapter for displaying a list of categories in a RecyclerView.
 * Each category item consists of a name and a radio button for selection.
 *
 * @author Cristina Correa
 * @since 1.0
 */
public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private final List<CategoryModel> categories;
    private int selectedPosition = -1;
    private String selectedCategory;

    public CategoryAdapter(List<CategoryModel> categories) {
        this.categories = categories;
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView categoryName;
        RadioButton categoryRadioButton;

        CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryName = itemView.findViewById(R.id.category_name);
            categoryRadioButton = itemView.findViewById(R.id.category_radio_button);
        }
    }

    /**
     * Creates a new ViewHolder for a category item.
     *
     * @param parent   The parent ViewGroup into which the new View will be added.
     * @param viewType The view type of the new View.
     * @return A new instance of {@link CategoryViewHolder}.
     */
    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    /**
     * Binds a category item to the given ViewHolder.
     *
     * @param holder   The ViewHolder to bind data to.
     * @param position The position of the item in the dataset.
     */
    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        CategoryModel category = categories.get(position);
        holder.categoryName.setText(category.getName());

        int drawableResId = holder.itemView.getContext().getResources()
                .getIdentifier(category.getDrawableRes(), "drawable", holder.itemView.getContext().getPackageName());

        holder.categoryName.setCompoundDrawablesWithIntrinsicBounds(drawableResId, 0, 0, 0);
        holder.categoryRadioButton.setChecked(position == selectedPosition);

        holder.itemView.setOnClickListener(v -> {
            selectPosition(position);
        });

        holder.categoryRadioButton.setOnClickListener(v -> {
            selectPosition(position);
        });
    }

    /**
     * Returns the total number of category items in the dataset.
     *
     * @return The size of the category list.
     */
    @Override
    public int getItemCount() {
        return categories.size();
    }

    /**
     * Updates the selected position and refreshes the RecyclerView.
     *
     * @param position The new selected position.
     */
    private void selectPosition(int position) {
        selectedPosition = position;

        notifyDataSetChanged();
    }

    /**
     * Sets the currently selected category by name and updates the RecyclerView.
     *
     * @param category The name of the category to select.
     */
    public void setSelectedCategory(String category) {
        this.selectedCategory = category;

        for (int i = 0; i < categories.size(); i++) {
            if (categories.get(i).getName().equals(category)) {
                selectedPosition = i;

                break;
            }
        }

        notifyDataSetChanged();
    }

    /**
     * Gets the name of the currently selected category.
     *
     * @return The name of the selected category, or {@code null} if no category is selected.
     */
    public String getSelectedCategory() {
        return selectedPosition != -1 ? categories.get(selectedPosition).getName() : null;
    }
}