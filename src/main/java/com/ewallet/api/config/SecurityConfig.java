package com.ewallet.api.config;

import com.ewallet.api.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AuthenticationProvider authenticationProvider;


    private static final String[] WHITE_LIST_URL = {
            "/api/v0/auth/**",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   @Qualifier("handlerExceptionResolver")HandlerExceptionResolver handlerExceptionResolver)
            throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(req -> req
                        .requestMatchers(WHITE_LIST_URL).permitAll()
                        .requestMatchers("/api/v0/admin/**").hasAnyAuthority("ADMIN", "SUPPORT")
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authenticationProvider(authenticationProvider)

                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                .exceptionHandling(exceptions -> exceptions
                        /*
                        Custom access denied handler to bridge Spring security and Spring MVC exception handling
                        By default security filter exceptions are handled outside the MVC context,
                        causing them to bypass @ControllerAdvice/GlobalExceptionHandler.
                         */
                        .accessDeniedHandler(((request, response, accessDeniedException) ->
                                /*
                                HandlerExceptionResolver is manually invoked to forward the security exception
                                to the GlobalExceptionHandler. This ensures that 403 errors
                                return the standardized ApiError JSON format instead of an empty response
                                 */
                                handlerExceptionResolver.resolveException(request , response , null , accessDeniedException))));

        return http.build();
    }
}