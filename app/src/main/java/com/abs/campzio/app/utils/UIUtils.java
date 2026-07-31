package com.abs.campzio.app.utils;

import android.app.Activity;
import android.content.Context;
import android.util.Patterns;
import android.view.View;
import android.widget.ProgressBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.abs.campzio.app.R;

public class UIUtils {

    public static void setupToolbar(AppCompatActivity activity, String title, boolean showHome) {
        Toolbar toolbar = activity.findViewById(R.id.adminToolbar); // General fallback or pass ID
        if (toolbar == null) toolbar = activity.findViewById(R.id.facultyContactToolbar);
        if (toolbar == null) toolbar = activity.findViewById(R.id.addTimetableToolbar);
        
        if (toolbar != null) {
            activity.setSupportActionBar(toolbar);
            if (activity.getSupportActionBar() != null) {
                activity.getSupportActionBar().setTitle(title);
                activity.getSupportActionBar().setDisplayHomeAsUpEnabled(showHome);
            }
        }
    }

    public static void toggleProgress(ProgressBar progressBar, View button, boolean show) {
        if (progressBar != null) progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        if (button != null) button.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    public static boolean isValidEmail(String email) {
        return !email.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static boolean isValidPassword(String password) {
        return !password.isEmpty() && password.length() >= 8;
    }
}
