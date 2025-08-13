package com.tudasam.prueba;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RAGChatController {

    private final ChatClient chatClient;



    private final String prompt = "Use the following context to answer the question:\n";

    public RAGChatController(ChatClient.Builder builder, VectorStore vectorStoreBean) {
        this.chatClient = builder
                .defaultAdvisors(new QuestionAnswerAdvisor(vectorStoreBean))
                .build();
    }

    @GetMapping(value = "/ragclient", consumes = "application/json", produces = "application/json")
    public String chat(@RequestParam("query") String query) {
        String response = chatClient.prompt()
                .user(prompt + query)
                .call()
                .content();
        return response;
    }
}
