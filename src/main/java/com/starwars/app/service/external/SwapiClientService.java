package com.starwars.app.service.external;

import com.starwars.app.dto.swapi.SwapiResponse;
import lombok.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SwapiClientService {

    private final RestTemplate restTemplate;
    private static final Logger logger = LoggerFactory.getLogger(SwapiClientService.class);

    @Value("${swapi.base-url}")
    private String baseUrl;


    public <T> Optional<T> getById(String endpoint, String id, Class<T> responseType) {
        logger.info("Fetching {} with ID: {}", endpoint, id);

        String url = baseUrl + "/" + endpoint + "/" + id;

        try {
            ResponseEntity<T> response = restTemplate.getForEntity(url, responseType);
            logger.info("Successfully fetched {} with ID: {}", endpoint, id);
            return Optional.ofNullable(response.getBody());

        } catch (HttpClientErrorException.NotFound e) {
            logger.info("{} not found with ID: {}", endpoint, id);
            return Optional.empty();

        } catch (RestClientException e) {
            logger.error("Error fetching {} with ID {} from SWAPI", endpoint, id, e);
            return Optional.empty();
        }
    }


    public <T> SwapiResponse<T> getPage(String endpoint, int page, int limit, ParameterizedTypeReference<SwapiResponse<T>> typeRef) {
        logger.info("Fetching {} - page: {}, limit: {}", endpoint, page, limit);

        String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/" + endpoint)
                .queryParam("page", page)
                .queryParam("limit", limit)
                .toUriString();

        try {
            ResponseEntity<SwapiResponse<T>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    typeRef
            );

            SwapiResponse<T> result = response.getBody();
            int resultCount = (result != null && result.getResults() != null) ? result.getResults().size() : 0;
            logger.info("Successfully fetched {} {} items", resultCount, endpoint);

            return result;

        } catch (RestClientException e) {
            logger.error("Error fetching {} from SWAPI", endpoint, e);
            throw new RuntimeException("Failed to fetch " + endpoint + " from Star Wars API", e);
        }
    }

}