package com.cristina.correa.mealmatecristina.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cristina.correa.mealmatecristina.MealDetailsActivity;
import com.cristina.correa.mealmatecristina.R;
import com.cristina.correa.mealmatecristina.models.MealModel;
import com.cristina.correa.mealmatecristina.utils.DescriptionUtils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Adapter for displaying a list of meals in a RecyclerView.
 * Each meal item includes an image, title, description, duration, calories, and type.
 *
 * @author Cristina Correa
 * @since 1.0
 */
public class CustomMealAdapter extends RecyclerView.Adapter<CustomMealAdapter.ViewHolder> {

    private List<MealModel> meals;

    public CustomMealAdapter() {
        this.meals = new ArrayList<>();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView meal_image_view, meal_type_image;
        TextView meal_calories, meal_title, meal_description, meal_duration;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            meal_image_view = itemView.findViewById(R.id.meal_image_view);
            meal_type_image = itemView.findViewById(R.id.meal_type_image);
            meal_title = itemView.findViewById(R.id.meal_title);
            meal_duration = itemView.findViewById(R.id.meal_duration);
            meal_description = itemView.findViewById(R.id.meal_description);
            meal_calories = itemView.findViewById(R.id.meal_calories);
        }
    }

    /**
     * Updates the meal list with new data and refreshes the RecyclerView.
     *
     * @param newMeals List of {@link MealModel} objects representing the new meals.
     */
    public void updateData(List<MealModel> newMeals) {
        this.meals.clear();
        this.meals.addAll(newMeals);

        notifyDataSetChanged();
    }

    /**
     * Creates a new ViewHolder for a meal item.
     *
     * @param parent   The parent ViewGroup into which the new View will be added.
     * @param viewType The view type of the new View.
     * @return A new instance of {@link ViewHolder}.
     */
    @NonNull
    @Override
    public CustomMealAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.meal_item, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Binds a meal item to the given ViewHolder.
     *
     * @param holder   The ViewHolder to bind data to.
     * @param position The position of the item in the dataset.
     */
    @Override
    public void onBindViewHolder(@NonNull CustomMealAdapter.ViewHolder holder, int position) {
        MealModel mealModel = meals.get(position);

        Picasso.get().load(mealModel.getImg_url())
                .placeholder(R.drawable.ic_launcher_background)
                .into(holder.meal_image_view);

        holder.meal_title.setText(mealModel.getTitle());
        holder.meal_duration.setText(String.valueOf(mealModel.getDuration() + " min"));

        String truncatedDescription = DescriptionUtils.truncateDescription(mealModel.getDescription(), 60, 9);
        holder.meal_description.setText(truncatedDescription);

        holder.meal_calories.setText(String.valueOf(mealModel.getCalories() + " Kcal"));

        Map<String, Integer> mealTypeIcons = new HashMap<>();
        mealTypeIcons.put("breakfast", R.drawable.meal_types_breakfast);
        mealTypeIcons.put("lunch", R.drawable.meal_types_lunch);
        mealTypeIcons.put("snack", R.drawable.meal_types_snack);
        mealTypeIcons.put("dinner", R.drawable.meal_types_dinner);

        int mealTypeImageRes = mealTypeIcons.getOrDefault(mealModel.getMealType().toLowerCase(), R.drawable.meal_types_lunch);
        holder.meal_type_image.setImageResource(mealTypeImageRes);

        holder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent(view.getContext(), MealDetailsActivity.class);
            intent.putExtra("meal_id", mealModel.getMeal_id());
            view.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return meals.size();
    }

}
