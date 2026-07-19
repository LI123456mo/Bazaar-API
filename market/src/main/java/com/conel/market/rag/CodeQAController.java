package com.conel.market.rag;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/rag")
@RequiredArgsConstructor
public class CodeQAController {

    private final CodeIndexerService indexerService;
    private final CodeQAService qaService;

    // Only admins should be able to re-index the source tree; a caller-supplied path would expose the filesystem.
    @PostMapping("/index")
    @PreAuthorize("hasAuthority('admin:access')")
    public ResponseEntity<String> index() throws IOException {
        String result = indexerService.indexCodebase();
        return ResponseEntity.ok(result);
    }

    // Call this anytime to ask questions about your code
    @GetMapping("/ask")
    public ResponseEntity<String> ask(@RequestParam String question) {
        String answer = qaService.ask(question);
        return ResponseEntity.ok(answer);
    }
}