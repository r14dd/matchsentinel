package com.matchsentinel.transaction.messaging;

public interface TransactionEventPublisher {
    void publishTransactionCreated(TransactionCreatedEvent event);
}
