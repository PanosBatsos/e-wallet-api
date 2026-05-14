package com.ewallet.api.filter;

import com.ewallet.api.entity.IdempotencyKey;
import com.ewallet.api.entity.User;
import com.ewallet.api.entity.UserRole;
import com.ewallet.api.repository.IdempotencyKeyRepository;
import com.ewallet.api.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class IdempotencyFilterTest {
    @Mock
    private IdempotencyKeyRepository idempotencyKeyRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private IdempotencyFilter idempotencyFilter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private User mockUser;

    @BeforeEach
    void setUp() {
        // Initiate mock HTTP request and response
        request = new MockHttpServletRequest("POST" , "/api/v0/wallets/transfer");
        response = new MockHttpServletResponse();

        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("test@test.com");
        mockUser.setUserRole(UserRole.USER);
        mockUser.setFirstName("test");
        mockUser.setLastName("test");
        mockUser.setPassword("123456");
        mockUser.setCreatedAt(LocalDateTime.now());
        mockUser.setBirthDate(LocalDate.of(2005 , 2 , 28));

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(mockUser.getEmail() , mockUser.getPassword()));
        SecurityContextHolder.setContext(context);
    }


    @Test
    void shouldPassThroughIfNoIdempotencyKeyHeader() throws ServletException, IOException {
        // Execute filter without adding the idempotency key header to the mock request
        // This simulates a normal request that does not require idempotency protection
        idempotencyFilter.doFilterInternal(request , response , filterChain);

        // Verify that the request was successfully passed down the filter chain to the next target
        verify(filterChain , times(1)).doFilter(request , response);

        // Verify that the filter immediately bypassed the logic and did not attempt to interact with the database
        // This ensures no performance waste on queries for unprotected endpoints
        verifyNoInteractions(idempotencyKeyRepository);
    }

    @Test
    void shouldBlockConcurrentRequestWithConflict() throws ServletException , IOException {
        // Setup mock request with a specific idempotency key header
        // UserRepo is also mocked to return the mock user when searched
        request.addHeader("Idempotency-Key" , "Idem-123");
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(mockUser));

        // Simulate an in-progress request by creating an IdempotencyKey entity
        // that exists in the database but has a null response status
        // This indicates that another thread is currently processing this transaction
        IdempotencyKey existingIdempotencyKey = IdempotencyKey.builder()
                .user(mockUser)
                .idempotencyKey("Idem-123")
                .responseStatus(null)
                .build();

        // Instruct the mock repository to return the in-progress key
        when(idempotencyKeyRepository.findByUserIdAndIdempotencyKey(1L , "Idem-123"))
                .thenReturn(Optional.of(existingIdempotencyKey));

        // Invoke the filter's logic with the mocked request and response objects
        idempotencyFilter.doFilterInternal(request , response , filterChain);

        // Check that the filter detected the concurrent request and returned a conflict status
        assertThat(response.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
        assertThat(response.getContentAsString()).contains("Request is already processing");

        // Verify that the filter chain was not continued
        verifyNoInteractions(filterChain);
    }

    @Test
    void shouldReturnCachedResponseIfAlreadyProcessed() throws ServletException , IOException{
        // Setup mock request with a specific idempotency key header
        // UserRepo is also mocked to return the mock user when searched
        request.addHeader("Idempotency-Key" , "Idem-123");
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(mockUser));


        // Set up key with 201 status
        IdempotencyKey cachedKey = IdempotencyKey.builder()
                .user(mockUser)
                .idempotencyKey("Idem-123")
                .responseStatus(201)
                .responseBody("{\"message\": \"Transfer successful\"}")
                .build();

        when(idempotencyKeyRepository.findByUserIdAndIdempotencyKey(1L , "Idem-123"))
                .thenReturn(Optional.of(cachedKey));

        // Invoke the filter's logic with the mocked request and response objects
        idempotencyFilter.doFilterInternal(request , response , filterChain);

        // Check that the filter detected successful transaction and returned a 201 status
        assertThat(response.getStatus()).isEqualTo(201);
        assertThat(response.getContentAsString()).isEqualTo("{\"message\": \"Transfer successful\"}");

        // Verify that the filter chain was not continued
        verifyNoInteractions(filterChain);

    }

    @Test
    void shouldHandleRaceConditionWhenTwoRequestsInsertSimultaneously() throws ServletException, IOException {
        // Setup mock request with a specific idempotency key header
        // UserRepo is also mocked to return the mock user when searched
        request.addHeader("Idempotency-Key", "Idem-123");
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(mockUser));

        // Both threads see that the key does not exist in the database yet
        when(idempotencyKeyRepository.findByUserIdAndIdempotencyKey(anyLong() , anyString()))
                .thenReturn(Optional.empty());

        // When this thread tries to save the new key the database throws a unique constraint violation
        // because another concurrent thread just managed to insert it first
        when(idempotencyKeyRepository.save(any(IdempotencyKey.class)))
                .thenThrow(new DataIntegrityViolationException("Unique constraint violation"));

        idempotencyFilter.doFilterInternal(request, response, filterChain);

        // Verify the filter handles the database exception gracefully and returns 409
        assertThat(response.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
        assertThat(response.getContentAsString()).contains("Request is already processing");

        verifyNoInteractions(filterChain);
    }
}
