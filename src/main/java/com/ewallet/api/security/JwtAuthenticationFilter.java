package com.ewallet.api.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    /**
     * The core method tha filters incoming HTTP requests
     * @param request The incoming HTTP request
     * @param response The outgoing HTTP response
     * @param filterChain The chain of filters to pass the request to if everything is okay
     */
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        // Check if the Authorization header exists and contains a Bearer token
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        // If there's no token let the request pass to the next filter.
        // Spring security will eventually block it if the endpoint requires authentication.
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request , response);
            return;
        }

        // Extract the token
        jwt = authHeader.substring(7);

        // Extract the email from token
        userEmail = jwtService.extractEmail(jwt);

        // If there is an email and the user is not already authenticated in this session
        if (userEmail != null &&
                SecurityContextHolder.getContext().getAuthentication() == null) {

            // Load the user from the database
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

            // Validate the token
            if (jwtService.isTokenValid(jwt , userDetails)) {

                // Create the authentication token and set it in the security context
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

                // Add details from the current HTTP request (like IP address, session ID)
                authenticationToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // Tell spring security the user is authenticated
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        }
        // Pass the request to the next filter in the chain
        filterChain.doFilter(request , response);
    }
}
