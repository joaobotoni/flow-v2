package com.botoni.flow.data.repositories;

import android.Manifest;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

import androidx.annotation.RequiresPermission;

import com.botoni.flow.data.source.network.RoutesDataSource;
import com.botoni.flow.ui.helpers.PermissionHelper;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import javax.inject.Inject;

import dagger.hilt.android.qualifiers.ApplicationContext;

/**
 * Repositório responsável pelo gerenciamento de geolocalização e pelo processamento de endereços.
 *
 * <p>Fornece funcionalidades para obter a localização atual do usuário e realizar buscas
 * de cidades e estados, filtrando os resultados com base no país do usuário.</p>
 */
public class LocalizacaoRepository {
    private final Context context;
    private String code;
    private final Geocoder geocoder;
    private final FusedLocationProviderClient client;
    private final RoutesDataSource source;

    @Inject
    public LocalizacaoRepository(@ApplicationContext Context context) {
        this.context = context;
        this.client = LocationServices.getFusedLocationProviderClient(context);
        this.geocoder = new Geocoder(context, Locale.getDefault());
        this.source = new RoutesDataSource(context);
    }

    public double parseDistance(String response) {
        try {
            return source.parse(response) / 1000.0;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Calcula a rota entre dois endereços e retorna a resposta da API.
     *
     * @param origin      endereço de origem
     * @param destination endereço de destino
     * @return JSON com os dados da rota
     */
    public String fetchRoute(Address origin, Address destination) {
        LatLng originLatLng = new LatLng(origin.getLatitude(), origin.getLongitude());
        LatLng destinationLatLng = new LatLng(destination.getLatitude(), destination.getLongitude());
        try {
            return source.compute(originLatLng, destinationLatLng);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Realiza a busca de cidades e estados com base em uma consulta textual,
     * filtrando os resultados pelo país do usuário quando a permissão de localização está disponível.
     *
     * <p>Caso as permissões {@link Manifest.permission#ACCESS_FINE_LOCATION} ou
     * {@link Manifest.permission#ACCESS_COARSE_LOCATION} sejam concedidas, a localização atual
     * do usuário é utilizada para determinar o país e restringir os resultados.</p>
     *
     * @param query texto utilizado como critério de busca (nome de cidade, estado, etc.)
     * @return lista de {@link Address} correspondentes à consulta, filtrada pelo país do usuário;
     * retorna uma lista vazia caso nenhum resultado seja encontrado
     * @throws RuntimeException se ocorrer algum erro durante a geocodificação
     */
    @RequiresPermission(allOf = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    })
    public List<Address> searchCityAndState(String query) {
        if (hasLocationPermission()) searchCountryCode();
        try {
            return searchCityAndStateFiltered(query);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Obtém a localização atual do dispositivo e atualiza o código do país do usuário.
     */
    @RequiresPermission(anyOf = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    })
    private void searchCountryCode() {
        client.getLastLocation().addOnSuccessListener(location -> {
            if (location == null) return;
            try {
                List<Address> addresses = geocoder.getFromLocation(
                        location.getLatitude(), location.getLongitude(), 1);
                if (addresses != null && !addresses.isEmpty())
                    code = addresses.get(0).getCountryCode();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Busca endereços a partir de uma consulta textual e filtra pelo país do usuário.
     *
     * @param query texto utilizado como critério de ambuscade
     * @return lista de {@link Address} filtrada pelo país do usuário
     * @throws IOException se ocorrer erro na geocodificação
     */
    private List<Address> searchCityAndStateFiltered(String query) throws IOException {
        List<Address> addresses = geocoder.getFromLocationName(query, 10);
        if (addresses == null) return Collections.emptyList();
        return addresses.stream()
                .filter(this::filterCountryCode)
                .collect(Collectors.toList());
    }

    /**
     * Verifica se ao menos uma permissão de localização foi concedida.
     *
     * @return {@code true} se {@link Manifest.permission#ACCESS_FINE_LOCATION} ou
     * {@link Manifest.permission#ACCESS_COARSE_LOCATION} estiver concedida
     */
    private boolean hasLocationPermission() {
        return PermissionHelper.hasPermissions(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        );
    }

    /**
     * Verifica se o endereço fornecido pertence ao mesmo país do usuário.
     *
     * @param address endereço a ser verificado
     * @return {@code true} se o código do país do endereço for nulo ou corresponder ao código do país do usuário;
     * {@code false} caso contrário
     */
    private boolean filterCountryCode(Address address) {
        return code == null || address.getCountryCode() != null && address.getCountryCode().equalsIgnoreCase(code);
    }
}