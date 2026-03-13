package com.botoni.flow.ui.helpers;

import android.content.Context;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.PluralsRes;
import androidx.annotation.StringRes;

import java.math.BigDecimal;
import java.util.Arrays;

/**
 * Helper utilitário para extração e definição segura de dados em componentes de UI (Views).
 * <p>
 * Esta classe atua como uma ponte entre a interface do usuário e o {@link NumberHelper},
 * cuidando de verificações de nulidade e extração de texto antes da conversão.
 * </p>
 */
public class ViewHelper {

    /**
     * Extrai o texto de um {@link TextView} de forma segura.
     *
     * @param view O componente de texto.
     * @return O texto sem espaços, ou {@code ""} se a view ou o texto for nulo.
     */
    @NonNull
    public static String getTexto(@Nullable TextView view) {
        if (view == null || view.getText() == null) return "";
        return view.getText().toString().trim();
    }

    /**
     * Extrai um valor Inteiro de um EditText.
     * Utiliza {@link NumberHelper#getInt(String)} para conversão segura.
     *
     * @param view O campo de edição.
     * @return O valor inteiro, ou 0 em caso de erro ou campo vazio.
     */
    @NonNull
    public static Integer getInt(@Nullable EditText view) {
        return NumberHelper.getInt(getTexto(view));
    }

    /**
     * Extrai um valor Double de um EditText.
     * Utiliza {@link NumberHelper#getDouble(String)} para conversão segura.
     *
     * @param view O campo de edição.
     * @return O valor double, ou 0.0 em caso de erro ou campo vazio.
     */
    @NonNull
    public static Double getDouble(@Nullable EditText view) {
        return NumberHelper.getDouble(getTexto(view));
    }

    /**
     * Extrai um valor BigDecimal de um EditText (ideal para valores monetários).
     * Utiliza {@link NumberHelper#getDecimal(String)} para conversão segura.
     *
     * @param view O campo de edição.
     * @return O valor BigDecimal, ou ZERO em caso de erro ou campo vazio.
     */
    @NonNull
    public static BigDecimal getBigDecimal(@Nullable EditText view) {
        return NumberHelper.getDecimal(getTexto(view));
    }

    /**
     * Define um texto diretamente em um {@link TextView}.
     * Aplica trim no texto antes de definir.
     *
     * @param textView O componente de texto.
     * @param text     O texto a ser exibido, ou {@code null} para limpar o campo.
     */
    public static void setText(@NonNull TextView textView, @Nullable String text) {
        textView.setText(text != null ? text.trim() : "");
    }

    /**
     * Define um texto plural em um {@link TextView} a partir de uma plurals resource.
     * <p>
     * Utiliza a quantidade para escolher a forma correta do plural (zero, one, other).
     * Se a quantidade for {@code null}, o campo é limpo sem lançar exceção.
     * </p>
     *
     * @param textView O componente de texto.
     * @param context  O contexto para acesso ao {@link android.content.res.Resources}.
     * @param resId    O identificador da plurals resource.
     * @param quantity A quantidade usada para selecionar o plural e interpolada na string.
     */
    public static void setPluralText(@NonNull TextView textView, @NonNull Context context, @PluralsRes int resId, @Nullable Integer quantity) {
        if (quantity == null) {
            textView.setText("");
            return;
        }
        textView.setText(context.getResources().getQuantityString(resId, quantity, quantity));
    }

    /**
     * Define um texto formatado em um {@link TextView} a partir de uma string resource.
     * <p>
     * Se qualquer argumento for {@code null}, o campo é limpo sem lançar exceção.
     * </p>
     *
     * @param textView O componente de texto.
     * @param context  O contexto para acesso ao {@link android.content.res.Resources}.
     * @param resId    O identificador da string resource com placeholders (ex: {@code %1$d}).
     * @param args     Os argumentos a serem interpolados na string resource.
     * @param <T>      O tipo dos argumentos.
     */
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
}