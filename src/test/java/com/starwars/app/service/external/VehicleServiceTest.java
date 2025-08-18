package com.starwars.app.service.external;

import com.starwars.app.dto.swapi.VehicleDTO;
import com.starwars.app.dto.swapi.VehicleDetailResponse;
import com.starwars.app.dto.swapi.SwapiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;

import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

    @Mock
    private SwapiClientService swapiClient;

    @InjectMocks
    private VehicleService vehicleService;

    private SwapiResponse<VehicleDTO> mockVehiclesResponse;
    private VehicleDetailResponse mockVehicleDetailResponse;

    @BeforeEach
    void setUp() {
        VehicleDTO vehicle1 = new VehicleDTO("1", "Sand Crawler", "https://www.swapi.tech/api/vehicles/1");
        VehicleDTO vehicle2 = new VehicleDTO("2", "T-16 skyhopper", "https://www.swapi.tech/api/vehicles/2");

        mockVehiclesResponse = new SwapiResponse<>();
        mockVehiclesResponse.setMessage("ok");
        mockVehiclesResponse.setTotal_records(39);
        mockVehiclesResponse.setTotal_pages(4);
        mockVehiclesResponse.setResults(Arrays.asList(vehicle1, vehicle2));

        VehicleDetailResponse.VehicleProperties properties = new VehicleDetailResponse.VehicleProperties();
        properties.setName("Sand Crawler");
        properties.setModel("Digger Crawler");
        properties.setManufacturer("Corellia Mining Corporation");
        properties.setVehicleClass("wheeled");

        VehicleDetailResponse.VehicleResult result = new VehicleDetailResponse.VehicleResult();
        result.setProperties(properties);
        result.setUid("1");

        mockVehicleDetailResponse = new VehicleDetailResponse();
        mockVehicleDetailResponse.setMessage("ok");
        mockVehicleDetailResponse.setResult(result);
    }

    @Test
    void shouldGetVehiclesSuccessfully() {
        int page = 1;
        int limit = 10;
        when(swapiClient.getPage(eq("vehicles"), eq(page), eq(limit), any(ParameterizedTypeReference.class)))
                .thenReturn(mockVehiclesResponse);

        SwapiResponse<VehicleDTO> result = vehicleService.getVehicles(page, limit);

        assertThat(result).isNotNull();
        assertThat(result.getMessage()).isEqualTo("ok");
        assertThat(result.getResults()).hasSize(2);
        assertThat(result.getResults().get(0).getName()).isEqualTo("Sand Crawler");
        assertThat(result.getResults().get(1).getName()).isEqualTo("T-16 skyhopper");

        verify(swapiClient).getPage(eq("vehicles"), eq(page), eq(limit), any(ParameterizedTypeReference.class));
    }

    @Test
    void shouldGetVehicleByIdSuccessfully() {
        String vehicleId = "1";
        when(swapiClient.getById("vehicles", vehicleId, VehicleDetailResponse.class))
                .thenReturn(Optional.of(mockVehicleDetailResponse));

        Optional<VehicleDetailResponse> result = vehicleService.getVehicleById(vehicleId);

        assertThat(result).isPresent();
        assertThat(result.get().getMessage()).isEqualTo("ok");
        assertThat(result.get().getResult().getProperties().getName()).isEqualTo("Sand Crawler");
        assertThat(result.get().getResult().getProperties().getModel()).isEqualTo("Digger Crawler");

        verify(swapiClient).getById("vehicles", vehicleId, VehicleDetailResponse.class);
    }

    @Test
    void shouldReturnEmptyWhenVehicleNotFound() {
        String vehicleId = "999";
        when(swapiClient.getById("vehicles", vehicleId, VehicleDetailResponse.class))
                .thenReturn(Optional.empty());

        Optional<VehicleDetailResponse> result = vehicleService.getVehicleById(vehicleId);

        assertThat(result).isEmpty();

        verify(swapiClient).getById("vehicles", vehicleId, VehicleDetailResponse.class);
    }

    @Test
    void shouldHandleEmptyVehiclesResponse() {
        int page = 99;
        int limit = 10;
        SwapiResponse<VehicleDTO> emptyResponse = new SwapiResponse<>();
        emptyResponse.setMessage("ok");
        emptyResponse.setResults(Arrays.asList());

        when(swapiClient.getPage(eq("vehicles"), eq(page), eq(limit), any(ParameterizedTypeReference.class)))
                .thenReturn(emptyResponse);

        SwapiResponse<VehicleDTO> result = vehicleService.getVehicles(page, limit);

        assertThat(result).isNotNull();
        assertThat(result.getResults()).isEmpty();

        verify(swapiClient).getPage(eq("vehicles"), eq(page), eq(limit), any(ParameterizedTypeReference.class));
    }
}