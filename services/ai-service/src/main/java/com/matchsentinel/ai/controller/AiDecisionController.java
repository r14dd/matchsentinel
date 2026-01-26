package com.matchsentinel.ai.controller;

import com.matchsentinel.ai.dto.AiDecisionResponse;
import com.matchsentinel.ai.dto.ScoreTransactionRequest;
import com.matchsentinel.ai.service.AiDecisionService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
public class AiDecisionController {
    private final AiDecisionService decisionService;

    public AiDecisionController(AiDecisionService decisionService) {
        this.decisionService = decisionService;
    }

    @PostMapping("/score")
    public ResponseEntity<AiDecisionResponse> score(@Valid @RequestBody ScoreTransactionRequest request) {
        return ResponseEntity.ok(decisionService.score(request));
    }

    @GetMapping("/decisions/{id}")
    public ResponseEntity<AiDecisionResponse> getById(@PathVariable UUID id) {
        return decisionService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/decisions")
    public ResponseEntity<AiDecisionResponse> getByTransactionId(@RequestParam UUID transactionId) {
        return decisionService.findByTransactionId(transactionId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
