package com.matchsentinel.ruleengine.messaging;

import com.matchsentinel.ruleengine.dto.TransactionFlaggedEvent;

public interface RuleEngineEventPublisher {
    void publishTransactionFlagged(TransactionFlaggedEvent event);
}
