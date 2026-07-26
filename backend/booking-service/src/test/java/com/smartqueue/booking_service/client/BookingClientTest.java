package com.smartqueue.booking_service.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

class BookingClientTest {

    @Mock
    private RestTemplate restTemplate;

    private BookingClient bookingClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        bookingClient = new BookingClient(restTemplate);
        bookingClient.setInventoryServiceUrl("http://localhost:8081");
    }

    @Test
    @DisplayName("acquireInventoryLock - Should return true when Inventory Service returns isLocked: true")
    void acquireInventoryLock_Success() {
        String resourceId = UUID.randomUUID().toString();
        String userId = UUID.randomUUID().toString();

        Map<String, Object> lockResponseData = Map.of(
                "resourceId", resourceId,
                "userId", userId,
                "isLocked", true
        );

        Map<String, Object> apiResponseBody = Map.of(
                "success", true,
                "data", lockResponseData
        );

        when(restTemplate.postForEntity(anyString(), any(), eq(Map.class)))
                .thenReturn(new ResponseEntity<>(apiResponseBody, HttpStatus.OK));

        boolean acquired = bookingClient.acquireInventoryLock(resourceId, userId, 300L);

        assertThat(acquired).isTrue();
    }

    @Test
    @DisplayName("acquireInventoryLock - Should return false when Inventory Service returns isLocked: false")
    void acquireInventoryLock_Failure() {
        String resourceId = UUID.randomUUID().toString();
        String userId = UUID.randomUUID().toString();

        Map<String, Object> lockResponseData = Map.of(
                "resourceId", resourceId,
                "userId", userId,
                "isLocked", false
        );

        Map<String, Object> apiResponseBody = Map.of(
                "success", false,
                "data", lockResponseData
        );

        when(restTemplate.postForEntity(anyString(), any(), eq(Map.class)))
                .thenReturn(new ResponseEntity<>(apiResponseBody, HttpStatus.OK));

        boolean acquired = bookingClient.acquireInventoryLock(resourceId, userId, 300L);

        assertThat(acquired).isFalse();
    }
}
