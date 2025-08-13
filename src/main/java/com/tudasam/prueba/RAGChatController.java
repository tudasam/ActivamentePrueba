package com.tudasam.prueba;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RAGChatController {

    private final ChatClient chatClient;

    public RAGChatController(ChatClient.Builder chatClientBuilder, VectorStore vectorStore) {
        this.chatClient = chatClientBuilder
                .defaultAdvisors(advisors -> advisors.advisors(new QuestionAnswerAdvisor(vectorStore)))
                .build();
    }

    @GetMapping(value = "/ragclient", produces = "application/json")
    public String ask(@RequestParam("query") String query) {
        return chatClient
                .prompt()
                .user("Use the following context to answer the question.\n" + query)
                .call()
                .content();
    }
}
