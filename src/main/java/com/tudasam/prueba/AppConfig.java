package com.tudasam.prueba;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.SimpleVectorStore;

@Configuration
public class AppConfig {
    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {
        // In-memory + save/load to JSON file
        return SimpleVectorStore.builder(embeddingModel).build();
    }

}
