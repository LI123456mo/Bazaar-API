package com.conel.market.rag;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CodeIndexerService {

    // VectorStore = Spring AI's interface to ChromaDB
    private final VectorStore vectorStore;
    @Value("${app.rag.project-root:${user.dir}}")
    private String projectRoot;

    public String indexCodebase() throws IOException {
        List<Document> documents = new ArrayList<>();
        Path rootPath = Paths.get(projectRoot).toAbsolutePath().normalize();

        if (!Files.exists(rootPath) || !Files.isDirectory(rootPath)) {
            throw new IllegalArgumentException("Configured project root does not exist or is not a directory: " + rootPath);
        }

        // Do not accept client-supplied paths here; indexing must stay inside the configured project root.
        try (var paths = Files.walk(rootPath)) {
            paths.filter(p -> p.toString().endsWith(".java"))
                    .forEach(path -> {
                        try {
                            String content = Files.readString(path);
                            String relativePath = rootPath.relativize(path).toString();

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
        }

        // TokenTextSplitter breaks large files into smaller chunks
        // so ChromaDB can find the EXACT relevant part, not the whole file
        var splitter = new TokenTextSplitter();
        vectorStore.add(splitter.apply(documents));

        return "Indexed " + documents.size() + " Java files from " + rootPath;
    }
}