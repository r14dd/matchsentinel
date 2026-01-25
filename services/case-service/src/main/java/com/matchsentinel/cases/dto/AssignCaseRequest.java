package com.matchsentinel.cases.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AssignCaseRequest(@NotNull UUID analystId) {
}
