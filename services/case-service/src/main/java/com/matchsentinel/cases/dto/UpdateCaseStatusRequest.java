package com.matchsentinel.cases.dto;

import com.matchsentinel.cases.domain.CaseStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateCaseStatusRequest(@NotNull CaseStatus status) {
}
