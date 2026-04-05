package com.botoni.flow.ui.helpers;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.FileUtils;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileHelper {

    private FileHelper() {
    }

    @NonNull
    public static Uri getUri(@NonNull Context context, @NonNull File file) {
        return FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);
    }

    @NonNull
    @RequiresApi(api = Build.VERSION_CODES.Q)
    public static Uri salvar(@NonNull Context context, @NonNull File file, @NonNull String mimeType, @NonNull String pasta) throws IOException {
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, file.getName());
        values.put(MediaStore.MediaColumns.MIME_TYPE, mimeType);
        values.put(MediaStore.MediaColumns.RELATIVE_PATH, pasta);

        ContentResolver resolver = context.getContentResolver();
        Uri uri = resolver.insert(MediaStore.Files.getContentUri("external"), values);
        if (uri == null) throw new IOException("Falha ao registrar arquivo.");

        try (InputStream in = new FileInputStream(file);
             OutputStream out = resolver.openOutputStream(uri)) {
            if (out == null) throw new IOException("Falha ao abrir stream.");
            FileUtils.copy(in, out);
        }
        return uri;
    }

    public static void compartilhar(@NonNull Activity activity, @NonNull File file, @NonNull String mimeType, @NonNull String titulo) {
        Uri uri = getUri(activity, file);
        Intent intent = new Intent(Intent.ACTION_SEND)
                .setType(mimeType)
                .putExtra(Intent.EXTRA_STREAM, uri)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        activity.startActivity(Intent.createChooser(intent, titulo));
    }
}
