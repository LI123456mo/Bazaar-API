package com.conel.market.rag;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CodeQAService {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    // @Qualifier tells Spring to use openAiChatModel (which is actually Groq)
    // instead of ollamaChatModel since we have both on the classpath now
    public CodeQAService(@Qualifier("openAiChatModel") ChatModel chatModel, VectorStore vectorStore) {
        this.chatClient = ChatClient.builder(chatModel).build();
        this.vectorStore = vectorStore;
    }

    public String ask(String question) {
        List<Document> relevant = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(question)
                        .topK(5)
                        .build()
        );

        String context = relevant.stream()
                .map(doc -> "// File: " + doc.getMetadata().get("file")
                        + "\n" + doc.getText())
                .collect(Collectors.joining("\n\n---\n\n"));

        String prompt = """
                You are a senior Java/Spring Boot code reviewer.
                Review the following source code and suggest SPECIFIC improvements.
                Focus on: performance issues, security vulnerabilities,
                better design patterns, cleaner code, missing validations.
                Be direct and specific — reference exact class names and methods.
                
                SOURCE CODE:
                %s
                
                QUESTION/TASK: %s
                """.formatted(context, question);

        return chatClient.prompt()
                .user(prompt)
                .call()
                .content();
    }
}