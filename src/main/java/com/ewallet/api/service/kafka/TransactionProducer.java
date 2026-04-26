package com.ewallet.api.service.kafka;

import com.ewallet.api.dto.kafka.TransactionEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

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
        log.info("Sending event to Kafka: {}" , event);

        // Pushes the message to the specified topic
        // It's a non-blocking operation
        // Spring will auto-form the event to JSON
        this.kafkaTemplate.send(TOPIC , event);
    }
}
