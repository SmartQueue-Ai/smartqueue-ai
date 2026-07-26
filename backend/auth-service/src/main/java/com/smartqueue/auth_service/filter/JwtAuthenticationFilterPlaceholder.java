package com.smartqueue.auth_service.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Placeholder JWT Authentication Filter for Auth Service Infrastructure.
 * Full JWT parsing and token verification logic will be injected in future auth tickets.
 */
@Slf4j
@Component
public class JwtAuthenticationFilterPlaceholder extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        log.trace("JWT Authentication Filter Infrastructure Placeholder executing for request: {}", request.getRequestURI());

        // Token parsing and SecurityContextHolder authentication population placeholder
        
        filterChain.doFilter(request, response);
    }
}
