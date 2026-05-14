package com.ewallet.api.config;

import com.ewallet.api.filter.IdempotencyFilter;
import com.ewallet.api.repository.IdempotencyKeyRepository;
import com.ewallet.api.repository.UserRepository;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    /**
     * Registers the IdempotencyFilter
     * Not using @Component on the filter itself preventing spring
     * from automatically applying it to all endpoints
     */

    @Bean
    public FilterRegistrationBean<IdempotencyFilter> idempotencyFilterFilterRegistrationBean(
            IdempotencyKeyRepository idempotencyKeyRepository,
            UserRepository userRepository
    ) {
        FilterRegistrationBean<IdempotencyFilter> registrationBean = new FilterRegistrationBean<>();

        // Manually instantiate the filter and provide the required dependencies
        IdempotencyFilter filter = new IdempotencyFilter(idempotencyKeyRepository , userRepository);
        registrationBean.setFilter(filter);

        // Define the exact URL patterns where this filter should be active
        registrationBean.addUrlPatterns("/api/v0/wallets/*");

        registrationBean.setOrder(1);

        return registrationBean;
    }
}
