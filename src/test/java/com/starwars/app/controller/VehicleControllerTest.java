package com.starwars.app.controller;

import com.starwars.app.dto.swapi.VehicleDTO;
import com.starwars.app.dto.swapi.VehicleDetailResponse;
import com.starwars.app.dto.swapi.SwapiResponse;
import com.starwars.app.service.external.VehicleService;
import com.starwars.app.service.JwtService;
import com.starwars.app.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VehicleController.class)
class VehicleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VehicleService vehicleService;

    @MockBean
    private UserService userService;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private JwtService jwtService;

    private SwapiResponse<VehicleDTO> vehiclesResponse;
    private VehicleDetailResponse vehicleDetailResponse;

    @BeforeEach
    void setUp() {
        VehicleDTO vehicle1 = new VehicleDTO("1", "Sand Crawler", "https://www.swapi.tech/api/vehicles/1");
        VehicleDTO vehicle2 = new VehicleDTO("2", "T-16 skyhopper", "https://www.swapi.tech/api/vehicles/2");

        vehiclesResponse = new SwapiResponse<>();
        vehiclesResponse.setMessage("ok");
        vehiclesResponse.setTotal_records(39);
        vehiclesResponse.setTotal_pages(4);
        vehiclesResponse.setResults(Arrays.asList(vehicle1, vehicle2));

        VehicleDetailResponse.VehicleProperties properties = new VehicleDetailResponse.VehicleProperties();
        properties.setName("Sand Crawler");
        properties.setModel("Digger Crawler");
        properties.setManufacturer("Corellia Mining Corporation");
        properties.setVehicleClass("wheeled");

        VehicleDetailResponse.VehicleResult result = new VehicleDetailResponse.VehicleResult();
        result.setProperties(properties);
        result.setUid("1");

        vehicleDetailResponse = new VehicleDetailResponse();
        vehicleDetailResponse.setMessage("ok");
        vehicleDetailResponse.setResult(result);
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void shouldGetVehiclesSuccessfully() throws Exception {
        when(vehicleService.getVehicles(1, 10)).thenReturn(vehiclesResponse);

        mockMvc.perform(get("/api/vehicles"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("ok"))
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results[0].name").value("Sand Crawler"))
                .andExpect(jsonPath("$.results[1].name").value("T-16 skyhopper"));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void shouldGetVehiclesWithCustomPageAndLimit() throws Exception {
        when(vehicleService.getVehicles(2, 5)).thenReturn(vehiclesResponse);

        mockMvc.perform(get("/api/vehicles")
                        .param("page", "2")
                        .param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("ok"));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void shouldReturnNoContentWhenVehiclesListIsEmpty() throws Exception {
        SwapiResponse<VehicleDTO> emptyResponse = new SwapiResponse<>();
        emptyResponse.setResults(Arrays.asList());

        when(vehicleService.getVehicles(anyInt(), anyInt())).thenReturn(emptyResponse);

        mockMvc.perform(get("/api/vehicles"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void shouldReturnNoContentWhenVehiclesListIsNull() throws Exception {
        when(vehicleService.getVehicles(anyInt(), anyInt())).thenReturn(null);

        mockMvc.perform(get("/api/vehicles"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void shouldReturnNoContentWhenVehiclesResultsIsNull() throws Exception {
        SwapiResponse<VehicleDTO> responseWithNullResults = new SwapiResponse<>();
        responseWithNullResults.setResults(null);

        when(vehicleService.getVehicles(anyInt(), anyInt())).thenReturn(responseWithNullResults);

        mockMvc.perform(get("/api/vehicles"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void shouldGetVehicleByIdSuccessfully() throws Exception {
        when(vehicleService.getVehicleById("1")).thenReturn(Optional.of(vehicleDetailResponse));

        mockMvc.perform(get("/api/vehicles/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("Sand Crawler"))
                .andExpect(jsonPath("$.model").value("Digger Crawler"))
                .andExpect(jsonPath("$.manufacturer").value("Corellia Mining Corporation"));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void shouldReturnNotFoundWhenVehicleDoesNotExist() throws Exception {
        when(vehicleService.getVehicleById("999")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/vehicles/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void shouldReturnHealthCheckSuccessfully() throws Exception {
        mockMvc.perform(get("/api/vehicles/health"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/plain;charset=UTF-8"))
                .andExpect(content().string("vehicles service is running"));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void shouldUseDefaultParametersWhenNotProvided() throws Exception {
        when(vehicleService.getVehicles(1, 10)).thenReturn(vehiclesResponse);

        mockMvc.perform(get("/api/vehicles"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void shouldHandleInvalidPageParameter() throws Exception {
        mockMvc.perform(get("/api/vehicles")
                        .param("page", "invalid"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void shouldHandleInvalidLimitParameter() throws Exception {
        mockMvc.perform(get("/api/vehicles")
                        .param("limit", "invalid"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void shouldHandleServiceExceptionInGetVehicles() throws Exception {
        when(vehicleService.getVehicles(anyInt(), anyInt()))
                .thenThrow(new RuntimeException("SWAPI service unavailable"));

        mockMvc.perform(get("/api/vehicles"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void shouldHandleServiceExceptionInGetVehicleById() throws Exception {
        when(vehicleService.getVehicleById(anyString()))
                .thenThrow(new RuntimeException("SWAPI service unavailable"));

        mockMvc.perform(get("/api/vehicles/1"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void shouldRequireAuthenticationForGetVehicles() throws Exception {
        mockMvc.perform(get("/api/vehicles"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRequireAuthenticationForGetVehicleById() throws Exception {
        mockMvc.perform(get("/api/vehicles/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRequireAuthenticationForHealthCheck() throws Exception {
        mockMvc.perform(get("/api/vehicles/health"))
                .andExpect(status().isForbidden());
    }
}