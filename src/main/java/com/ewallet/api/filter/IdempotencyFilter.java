package com.ewallet.api.filter;


import com.ewallet.api.entity.IdempotencyKey;
import com.ewallet.api.entity.User;
import com.ewallet.api.exception.ResourceNotFoundException;
import com.ewallet.api.repository.IdempotencyKeyRepository;
import com.ewallet.api.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;


@RequiredArgsConstructor
public class IdempotencyFilter extends OncePerRequestFilter {
    private final IdempotencyKeyRepository idempotencyKeyRepository;
    private final UserRepository userRepository;


    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Extract the idempotency key header from the incoming request
        String idempotencyKeyHeader = request.getHeader("Idempotency-Key");

        // If there is no key provided or if it's a safe method
        // skip the idempotency logic and proceed with the normal filter chain
        if (idempotencyKeyHeader == null || request.getMethod().equalsIgnoreCase("GET")) {
            filterChain.doFilter(request , response);
            return;
        }

        // Identify the user making the request
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Check if this specific key has already been used by this user
        Optional<IdempotencyKey> existingKeyOpt = idempotencyKeyRepository.findByUserIdAndIdempotencyKey(
                user.getId() , idempotencyKeyHeader
        );

        if (existingKeyOpt.isPresent()) {
            IdempotencyKey existingKey = existingKeyOpt.get();

            // Double click problem
            // The key exists but has no response status yet
            // This means the original request is still
            // being processed by another thread right now
            // and must reject the duplicate
            if (existingKey.getResponseStatus() == null) {
                response.setStatus(HttpStatus.CONFLICT.value());
                response.getWriter().write("{\"error\": \"Request is already processing. Please wait.\"}");
                response.setContentType("application/json");
                return;
            }

            // Cached Request
            // The request was successfully processed in the past
            // The returned response status and body must be the same with the saved one
            // and controller must not be hit again
            response.setStatus(existingKey.getResponseStatus());
            response.getWriter().write(existingKey.getResponseBody());
            response.setContentType("application/json");
            return;
        }

        // New request
        // Create a new idempotency record to lock this key in the database immediately
        // The response status and body are left null for now
        IdempotencyKey newKey = IdempotencyKey.builder()
                .user(user)
                .idempotencyKey(idempotencyKeyHeader)
                .requestPath(request.getRequestURI())
                .build();
        try {
            // Save to the db
            idempotencyKeyRepository.save(newKey);
        } catch (DataIntegrityViolationException e) {
            // Hard race condition defense
            // If two exact same requests arrive at the exact same millisecond both will pass
            // the isPresent check above
            // However the database UNIQUE constraint will
            // reject the second INSERT attempt throwing this exception.
            response.setStatus(HttpStatus.CONFLICT.value());
            response.getWriter().write("{\"error\": \"Request is already processing. Please wait.\"}");
            response.setContentType("application/json");
            return;
        }


        // This wrapper acts as a spy that allows the read of the response body
        // without consuming the output stream meant for the client
        ContentCachingResponseWrapper contentCachingResponseWrapper = new ContentCachingResponseWrapper(response);
        boolean processedSuccessfully = false;

        try {
            // Pass the request down the chain to the actual Controller to do the business logic
            filterChain.doFilter(request , contentCachingResponseWrapper);
            processedSuccessfully = true;
        } finally {
            if (processedSuccessfully) {
                // The Controller has finished
                // Now the generated status and body has to be extracted
                // from the wrapper
                int status = contentCachingResponseWrapper.getStatus();
                String responseBody = new String(contentCachingResponseWrapper.getContentAsByteArray(),
                        StandardCharsets.UTF_8);

                // Update the locked db record with the actual results for future caching
                newKey.setResponseStatus(status);
                newKey.setResponseBody(responseBody);
                idempotencyKeyRepository.save(newKey);

                // Copy the cached body back to the real response,
                // otherwise the client will receive an empty body
                contentCachingResponseWrapper.copyBodyToResponse();;
            } else {
                // Zombie key prevention
                // If a fatal unhandled exception occurred in the Controller (app crash),
                // the request failed permanently
                // The pending lock must be deleted so the user
                // is not permanently blocked from retrying this transaction
                idempotencyKeyRepository.delete(newKey);
            }
        }
    }
}
