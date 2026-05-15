package com.ewallet.api.service.kafka;

import com.ewallet.api.dto.kafka.TransactionEvent;
import com.ewallet.api.entity.ProcessedEvent;
import com.ewallet.api.repository.ProcessedEventRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionConsumer {
    private final ProcessedEventRepository processedEventRepository;

    /**
     * Listens to the transaction topic and processes transaction events
     * The @Transactional ensures that the idempotency record and the business logic
     * work as an atomic operation
     */
    @KafkaListener(topics = "wallet-transactions" , groupId = "ewallet-main-group")
    @Transactional
    public void consume(TransactionEvent event) {
        log.info("Received transaction event: {}" , event.getTransactionId());

        String eventId = String.valueOf(event.getTransactionId());

        if (processedEventRepository.existsById(eventId)) {
            log.warn("Duplicate event detected: {}" , eventId);
            return;
        }

        processTransactionEventBusinessLogic(event);

        processedEventRepository.save(new ProcessedEvent(eventId , LocalDateTime.now()));
    }

    // This method listens only the failed transactions
    @KafkaListener(topics = "wallet-transactions.DLT" , groupId = "ewallet-dlt-group")
    public void handleDlt(TransactionEvent event) {
        log.error("CRITICAL: Event moved to DLT! Transaction ID: {}" , event.getTransactionId());

    }



    private void processTransactionEventBusinessLogic(TransactionEvent event) {
        // For now only logs
        log.info("Executing async logic for transaction {} of amount {}" ,
                event.getTransactionId(),
                event.getAmount());
    }
}
