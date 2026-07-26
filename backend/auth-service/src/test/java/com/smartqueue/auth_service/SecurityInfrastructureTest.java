package com.smartqueue.auth_service;

import com.smartqueue.auth_service.config.SecurityConfig;
import com.smartqueue.auth_service.controller.InfrastructureHealthController;
import com.smartqueue.auth_service.filter.CorrelationIdFilter;
import com.smartqueue.auth_service.filter.JwtAuthenticationFilter;
import com.smartqueue.auth_service.filter.RequestLoggingFilter;
import com.smartqueue.auth_service.security.CustomUserDetailsService;
import com.smartqueue.auth_service.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = InfrastructureHealthController.class)
@Import({SecurityConfig.class, CorrelationIdFilter.class, RequestLoggingFilter.class, JwtAuthenticationFilter.class})
@ActiveProfiles("test")
class SecurityInfrastructureTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CustomUserDetailsService userDetailsService;

    @MockBean
    private StringRedisTemplate redisTemplate;

    @Test
    void passwordEncoder_ShouldHashAndMatchPassword() {
        String rawPassword = "securePassword123";
        String encoded = passwordEncoder.encode(rawPassword);

        assertThat(encoded).isNotEqualTo(rawPassword);
        assertThat(passwordEncoder.matches(rawPassword, encoded)).isTrue();
    }

    @Test
    void publicPingEndpoint_ShouldReturn200AndCorrelationIdHeader() throws Exception {
        mockMvc.perform(get("/api/v1/auth/ping"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Correlation-ID"))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("UP"));
    }

    @Test
    void protectedEndpoint_WithoutToken_ShouldReturn401Unauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/protected/resource"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().exists("X-Correlation-ID"))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }
}
