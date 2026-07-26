package com.smartqueue.booking_service.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.Map;

@Slf4j
@Component
public class BookingClient {

    private final RestTemplate restTemplate;

    @Value("${inventory.service.url:http://localhost:8081}")
    private String inventoryServiceUrl;

    @Autowired
    public BookingClient(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofSeconds(3))
                .setReadTimeout(Duration.ofSeconds(5))
                .build();
    }

    public BookingClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void setInventoryServiceUrl(String inventoryServiceUrl) {
        this.inventoryServiceUrl = inventoryServiceUrl;
    }

    public boolean acquireInventoryLock(String resourceId, String userId, Long ttlSeconds) {
        String url = inventoryServiceUrl + "/api/v1/inventory/lock/" + resourceId;
        log.info("Calling Inventory Service at {} to acquire lock for resourceId: {}, userId: {}", url, resourceId, userId);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> requestBody = Map.of(
                    "userId", userId,
                    "ttlSeconds", ttlSeconds != null ? ttlSeconds : 300L
            );

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(url, requestEntity, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<?, ?> responseBody = response.getBody();
                Boolean success = (Boolean) responseBody.get("success");
                Map<?, ?> data = (Map<?, ?>) responseBody.get("data");

                if (Boolean.TRUE.equals(success) && data != null) {
                    Object isLocked = data.get("isLocked");
                    if (isLocked == null) {
                        isLocked = data.get("locked");
                    }
                    return Boolean.TRUE.equals(isLocked);
                }
            }
            log.warn("Inventory lock call returned unsuccessful response or payload for resourceId: {}", resourceId);
            return false;
        } catch (Exception e) {
            log.error("Failed to acquire inventory lock from Inventory Service for resourceId {}: {}", resourceId, e.getMessage());
            return false;
        }
    }
}
