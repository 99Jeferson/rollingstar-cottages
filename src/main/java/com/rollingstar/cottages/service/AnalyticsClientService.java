package com.rollingstar.cottages.service;

import com.rollingstar.cottages.model.ForecastRequestDTO;
import com.rollingstar.cottages.model.ForecastResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class AnalyticsClientService {

    private final RestTemplate restTemplate;
    
    // Developer B's independent predictive Python service local network address
    private final String PYTHON_SERVICE_URL = "http://localhost:8000/api/v1/forecast";

    @Autowired
    public AnalyticsClientService() {
        // Instantiate a standard Spring web template client executor
        this.restTemplate = new RestTemplate();
    }

    /**
     * Dispatches packaged business history metrics to the Python microservice
     * and returns the upcoming profit/demand predictive analytics arrays.
     */
    public ForecastResponseDTO fetchBusinessForecast(ForecastRequestDTO requestPayload) {
        try {
            // Setup explicit JSON headers for microservice payload communication
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Wrap data contract and headers together
            HttpEntity<ForecastRequestDTO> entityRequest = new HttpEntity<>(requestPayload, headers);

            System.out.println(">>> Dispatched independent payload to Python service at " + PYTHON_SERVICE_URL);
            
            // Execute the outbound network POST operation over the local network bridge
            ForecastResponseDTO response = restTemplate.postForObject(PYTHON_SERVICE_URL, entityRequest, ForecastResponseDTO.class);
            
            if (response != null) {
                System.out.println(">>> Analytics reply received from Python node. Status: " + response.getStatus());
            }
            return response;

        } catch (Exception e) {
            System.err.println(">>> Critical error: Failed to bridge communication with the Python Microservice: " + e.getMessage());
            
            // Return a safe fallback container object so your application doesn't completely crash if Developer B's node is offline
            ForecastResponseDTO fallback = new ForecastResponseDTO();
            fallback.setStatus("ERROR_SERVICE_UNAVAILABLE");
            return fallback;
        }
    }
}