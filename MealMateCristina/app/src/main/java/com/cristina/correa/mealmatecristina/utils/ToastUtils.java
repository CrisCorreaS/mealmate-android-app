package com.cristina.correa.mealmatecristina.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.cristina.correa.mealmatecristina.R;

/**
 * A utility class for displaying custom toast messages in the application.
 * It provides a method to show a custom-styled toast with a specified message.
 *
 * @author Cristina Correa
 * @since 1.0
 */
public class ToastUtils {

    /**
     * Displays a custom toast message with a specific layout.
     * The message will be displayed in a custom toast layout defined in the {@code custom_toast} XML file.
     *
     * @param context the {@link Context} from which the toast is being called.
     * @param message the message to display in the toast.
     */
    public static void showCustomToast(Context context, String message) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.custom_toast, null);

        TextView text = view.findViewById(R.id.text);
        text.setText(message);

        Toast toast = new Toast(context);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(view);

        toast.show();
    }
}
