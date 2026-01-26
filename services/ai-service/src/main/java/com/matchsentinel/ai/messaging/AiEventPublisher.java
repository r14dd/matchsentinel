package com.matchsentinel.ai.messaging;

import com.matchsentinel.ai.dto.TransactionScoredEvent;

public interface AiEventPublisher {
    void publishTransactionScored(TransactionScoredEvent event);
}
