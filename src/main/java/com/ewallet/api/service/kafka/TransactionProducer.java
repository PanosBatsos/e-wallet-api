package com.ewallet.api.service.kafka;

import com.ewallet.api.dto.kafka.TransactionEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionProducer {


    private final KafkaTemplate<String , Object> kafkaTemplate;
    // The topic name where all transaction-related messages will be published
    private static final String TOPIC = "wallet-transactions";

    /**
     *  Publishes a transaction event to the broker
     *  This allows other services to react to wallet activities asynchronously
     * @param event The details of the transaction to be sent
     */
    public void sendTransactionEvent(TransactionEvent event) {
        try {
            // Waits maximum 5 seconds to receive response for kafka
            kafkaTemplate.send(TOPIC , event).get(5 , TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Cannot send the event to Kafka: " + event.getTransactionId() , e);
        }
    }
}
