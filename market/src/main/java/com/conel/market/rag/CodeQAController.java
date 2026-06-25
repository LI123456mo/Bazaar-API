package com.conel.market.rag;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/rag")
@RequiredArgsConstructor
public class CodeQAController {

    private final CodeIndexerService indexerService;
    private final CodeQAService qaService;

    // Call this ONCE to index  project into ChromaDB
    @PostMapping("/index")
    public ResponseEntity<String> index(@RequestParam String path) throws IOException {
        String result = indexerService.indexCodebase(path);
        return ResponseEntity.ok(result);
    }

    // Call this anytime to ask questions about your code
    @GetMapping("/ask")
    public ResponseEntity<String> ask(@RequestParam String question) {
        String answer = qaService.ask(question);
        return ResponseEntity.ok(answer);
    }
}