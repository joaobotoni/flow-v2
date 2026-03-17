package com.botoni.flow.ui.helpers;

import android.content.Context;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.PluralsRes;
import androidx.annotation.StringRes;

import java.math.BigDecimal;
import java.util.Arrays;

public class ViewHelper {
    @NonNull
    public static String requireText(@Nullable TextView view) {
        if (view == null || view.getText() == null) return "";
        return view.getText().toString().trim();
    }

    @NonNull
    public static Integer getInt(@Nullable EditText view) {
        return NumberHelper.getInt(requireText(view));
    }

    @NonNull
    public static Double getDouble(@Nullable EditText view) {
        return NumberHelper.getDouble(requireText(view));
    }

    @NonNull
    public static BigDecimal getBigDecimal(@Nullable EditText view) {
        return NumberHelper.getDecimal(requireText(view));
    }

    public static boolean noneMatch(@Nullable String... texts) {
        if (texts == null) return false;
        for (String text : texts) {
            if (text == null || text.trim().isEmpty()) return false;
        }
        return true;
    }

    public static boolean noneMatch(@Nullable TextView... views) {
        if (views == null) return false;
        for (TextView view : views) {
            if (view == null || requireText(view).isEmpty()) return false;
        }
        return true;
    }

    public static void setText(@NonNull TextView textView, @Nullable String text) {
        textView.setText(text != null ? text.trim() : "");
    }

    public static void setPluralText(@NonNull TextView textView, @NonNull Context context, @PluralsRes int resId, @Nullable Integer quantity) {
        if (quantity == null) {
            textView.setText("");
            return;
        }
        textView.setText(context.getResources().getQuantityString(resId, quantity, quantity));
    }

    @SafeVarargs
    public static <T> void setText(@NonNull TextView textView, @NonNull Context context, @StringRes int resId, T... args) {
        for (T arg : args) {
            if (arg == null) {
                textView.setText("");
                return;
            }
        }
        textView.setText(context.getString(resId, Arrays.asList(args).toArray()));
    }

    public static void setVisible(boolean visible, View... views) {
        int visibility = visible ? View.VISIBLE : View.GONE;
        for (View view : views) {
            view.setVisibility(visibility);
        }
    }
}