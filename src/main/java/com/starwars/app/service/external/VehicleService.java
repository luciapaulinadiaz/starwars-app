package com.starwars.app.service.external;

import com.starwars.app.dto.swapi.*;
import lombok.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VehicleService {

    private final SwapiClientService swapiClient;
    private static final Logger logger = LoggerFactory.getLogger(VehicleService.class);

    private static final String ENDPOINT = "vehicles";


    public SwapiResponse<VehicleDTO> getVehicles(int page, int limit) {
        return swapiClient.getPage(
                ENDPOINT,
                page,
                limit,
                new ParameterizedTypeReference<SwapiResponse<VehicleDTO>>() {}
        );
    }


    public Optional<VehicleDetailResponse> getVehicleById(String id) {
        return swapiClient.getById(ENDPOINT, id, VehicleDetailResponse.class);
    }

}