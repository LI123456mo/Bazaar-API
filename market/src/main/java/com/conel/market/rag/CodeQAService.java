package com.conel.market.rag;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CodeQAService {

    private final ChatClient.Builder chatClientBuilder;
    private final VectorStore vectorStore;

    public String ask(String question) {
        ChatClient chatClient = chatClientBuilder.build();

        // Step 1: Convert question to vector and find top 5 similar code chunks
        List<Document> relevant = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(question)//Text to use for embedding similarity
                        .topK(5)
                        .build()
        );

        // Step 2: Build context string from retrieved code
        String context = relevant.stream()
                .map(doc -> "// File: " + doc.getMetadata().get("file")
                        + "\n" + doc.getText())
                .collect(Collectors.joining("\n\n---\n\n"));

        // Step 3: Ask llama3 using the retrieved code as context
        String prompt = """
            You are an expert code reviewer for a Spring Boot e-commerce API called Bazaar.
            Use ONLY the following source code to answer the question.
            Be specific about class names, methods, and line-level details.
            
            SOURCE CODE CONTEXT:
            %s
            
            QUESTION: %s
            """.formatted(context, question);

        return chatClient.prompt()
                .user(prompt)
                .call()
                .content();
    }
}