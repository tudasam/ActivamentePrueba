package com.tudasam.prueba;

import jakarta.annotation.PostConstruct;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;

@Component
public class PDFReader {

    @Value("classpath:referenceDocument.pdf")
    private Resource pdf;

    private final VectorStore vectorStore;
    private static final String VECTOR_FILE = "vectorstore.json";

    public PDFReader(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @PostConstruct
    public void init() {
        File f = new File(VECTOR_FILE);
        if (f.exists()) {
            ((SimpleVectorStore) vectorStore).load(f);
            System.out.println("Loaded vector store from " + VECTOR_FILE);
            return;
        }

        var config = PdfDocumentReaderConfig.builder()
                .withPageExtractedTextFormatter(new ExtractedTextFormatter.Builder().build())
                .build();

        var reader = new PagePdfDocumentReader(pdf, config);
        var pages = reader.get(); // List<Document>
        var splitter = new TokenTextSplitter();
        List<Document> chunks = splitter.apply(pages);

        vectorStore.accept(chunks);
        ((SimpleVectorStore) vectorStore).save(f);
        System.out.println("Created vector store at " + VECTOR_FILE);
    }
}
