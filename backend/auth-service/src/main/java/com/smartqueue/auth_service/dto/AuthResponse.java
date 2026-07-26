package com.smartqueue.auth_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response payload containing issued JWT tokens and user profile")
public class AuthResponse {

    @Schema(description = "JWT Access Token used for authenticating requests", example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huX2RvZSIs...")
    private String accessToken;

    @Schema(description = "Refresh Token used to generate new access tokens", example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huX2RvZSIs...")
    private String refreshToken;

    @Schema(description = "Token Type scheme", example = "Bearer")
    @Builder.Default
    private String tokenType = "Bearer";

    @Schema(description = "Access token expiration duration in milliseconds", example = "900000")
    private long expiresInMs;

    @Schema(description = "Authenticated user profile summary")
    private UserResponse user;
}
