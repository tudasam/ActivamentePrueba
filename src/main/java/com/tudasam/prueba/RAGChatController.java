package com.tudasam.prueba;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@CrossOrigin
public class RAGChatController {

    private final ChatClient baseChatClient;
    private final DocumentService documentService;

    public RAGChatController(ChatClient.Builder chatClientBuilder, DocumentService documentService) {
        this.baseChatClient = chatClientBuilder.build();
        this.documentService = documentService;
    }

    /** Upload one PDF; it becomes the ONLY active document. */
    @PostMapping(value = "/document", consumes = "multipart/form-data", produces = "application/json")
    public UploadResult upload(@RequestPart("file") MultipartFile file) {
        if (file.isEmpty()) throw new IllegalArgumentException("Empty file");
        String name = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase();
        if (!name.endsWith(".pdf")) throw new IllegalArgumentException("Only PDF files are supported");

        documentService.ingestSingle(file);
        return new UploadResult("ready");
    }

    /** Ask against the single active document. */
    @GetMapping(value = "/ragclient", produces = "application/json")
    public String ask(@RequestParam("query") String query) {
        VectorStore activeStore = documentService.getActive();
        var advisor = new QuestionAnswerAdvisor(activeStore);

        return baseChatClient
                .prompt()
                .advisors(a -> a.advisors(advisor))
                .user("Use the following context to answer the question.\n" + query)
                .call()
                .content();
    }

    public record UploadResult(String status) {}
}

