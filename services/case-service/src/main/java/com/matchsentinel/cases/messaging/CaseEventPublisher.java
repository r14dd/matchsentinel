package com.matchsentinel.cases.messaging;

import com.matchsentinel.cases.dto.CaseCreatedEvent;

public interface CaseEventPublisher {
    void publishCaseCreated(CaseCreatedEvent event);
}
