package com.conel.market.rag;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CodeIndexerService {

    // VectorStore = Spring AI's interface to ChromaDB
    private final VectorStore vectorStore;

    public String indexCodebase(String rootPath) throws IOException {
        List<Document> documents = new ArrayList<>();

        // Walk through all files the project
        Files.walk(Paths.get(rootPath))
                .filter(p -> p.toString().endsWith(".java"))
                .forEach(path -> {
                    try {
                        String content = Files.readString(path);
                        String relativePath = Paths.get(rootPath)
                                .relativize(path)
                                .toString();

                        // Each Java file becomes a Document with metadata
                        Document doc = new Document(
                                content,
                                Map.of(
                                        "file", relativePath,
                                        "project", "bazaar-api"
                                )
                        );
                        documents.add(doc);
                    } catch (IOException e) {
                        System.err.println("Could not read: " + path);
                    }
                });

        // TokenTextSplitter breaks large files into smaller chunks
        // so ChromaDB can find the EXACT relevant part, not the whole file
        var splitter = new TokenTextSplitter();
        vectorStore.add(splitter.apply(documents));

        return "Indexed " + documents.size() + " Java files from " + rootPath;
    }
}