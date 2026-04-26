package com.ewallet.api.service.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionProducer {


    private final KafkaTemplate<String , String> kafkaTemplate;
    // The topic name where all transaction-related messages will be published
    private static final String TOPIC = "wallet-transactions";

    /**
     *  Publishes a transaction event to the broker
     *  This allows other services to react to wallet activities asynchronously
     * @param message The details of the transaction to be sent
     */
    public void sendTransactionEvent(String message) {
        log.info("Sending event to Kafka: {}" , message);

        // Pushes the message to the specified topic
        // It's a non-blocking operation
        this.kafkaTemplate.send(TOPIC , message);
    }
}
