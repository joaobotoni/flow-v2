package com.botoni.flow.data.source.network;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;

import com.botoni.flow.R;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import dagger.hilt.android.qualifiers.ApplicationContext;

/**
 * Fonte de dados responsável por realizar o cálculo de rotas
 * e gerenciar a comunicação com a Routes API do Google Maps.
 */
public class RoutesDataSource {
    private static final String URL = "https://routes.googleapis.com/directions/v2:computeRoutes";
    private static final String MASK = "routes.distanceMeters";
    private final String apiKey;

    /** Inicializa a fonte de dados carregando a chave da API do manifesto. */
    public RoutesDataSource(@ApplicationContext Context context) {
        this.apiKey = load(context);
    }

    /** Calcula a rota entre origem e destino e retorna o JSON bruto da resposta. */
    public String compute(@NonNull LatLng origin, @NonNull LatLng destination) throws Exception {
        String body = build(origin, destination);
        return fetch(body);
    }

    /** Extrai a distância em metros do JSON de resposta. */
    public int parse(String json) throws Exception {
        return new JSONObject(json)
                .getJSONArray("routes")
                .getJSONObject(0)
                .getInt("distanceMeters");
    }

    /** Lê a chave da API do metadado declarado no AndroidManifest. */
    private static String load(@NonNull Context context) {
        try {
            ApplicationInfo info = context.getPackageManager()
                    .getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            String key = info.metaData.getString(context.getString(R.string.api_key_google_maps));
            if (key == null || key.isEmpty())
                throw new IllegalStateException(context.getString(R.string.erro_chave_api_ausente));
            return key;
        } catch (PackageManager.NameNotFoundException e) {
            throw new IllegalStateException(context.getString(R.string.erro_chave_api_ausente));
        }
    }

    /** Monta o corpo JSON da requisição com origem, destino e preferências de rota. */
    private static String build(@NonNull LatLng origin, @NonNull LatLng destination) throws Exception {
        return new JSONObject()
                .put("origin", toWaypoint(origin))
                .put("destination", toWaypoint(destination))
                .put("travelMode", "DRIVE")
                .put("routingPreference", "TRAFFIC_AWARE_OPTIMAL")
                .put("units", "METRIC")
                .toString();
    }

    /** Realiza a requisição POST para a Routes API e retorna o corpo da resposta. */
    private String fetch(@NonNull String body) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(URL).openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("X-Goog-Api-Key", apiKey);
        conn.setRequestProperty("X-Goog-FieldMask", MASK);
        conn.setDoOutput(true);
        conn.getOutputStream().write(body.getBytes());

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
            return sb.toString();
        } finally {
            conn.disconnect();
        }
    }

    /** Converte um LatLng para o formato de waypoint esperado pela Routes API. */
    private static JSONObject toWaypoint(@NonNull LatLng latLng) throws Exception {
        JSONObject json = new JSONObject()
                .put("latitude", latLng.latitude)
                .put("longitude", latLng.longitude);

        JSONObject location = new JSONObject()
                .put("latLng", json);

        return new JSONObject()
                .put("location", location);
    }
}