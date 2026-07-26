package com.smartqueue.auth_service.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRegisteredEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID userId;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    @Builder.Default
    private Instant timestamp = Instant.now();
}
