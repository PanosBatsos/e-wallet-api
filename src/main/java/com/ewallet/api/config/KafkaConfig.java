package com.ewallet.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.BackOff;
import org.springframework.util.backoff.ExponentialBackOff;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaConfig {

    @Bean
    public DefaultErrorHandler errorHandler(KafkaOperations<String , Object> template) {
        // If a message fails after all retries this recoverer will automatically
        // publish it to DLT. This prevents the queue from blocking and ensures no data is lost
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(template);

        // Configures the retry policy using Exponential Backoff
        ExponentialBackOff exponentialBackOff = new ExponentialBackOff(2000L, 2.0);
        exponentialBackOff.setMaxAttempts(3);

        return new DefaultErrorHandler(recoverer , exponentialBackOff);
    }
}
