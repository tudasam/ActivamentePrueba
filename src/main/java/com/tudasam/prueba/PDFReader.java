package com.tudasam.prueba;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class PDFReader {

    @Value("classpath:referenceDocument.pdf")
    private Resource pdfResource;

    @Autowired
    private VectorStore vectorStoreBean;

    private static final String VECTOR_STORE_FILE = "vectorstore.json";

    @PostConstruct
    public void load() {
        File vectorFile = new File(VECTOR_STORE_FILE);

        if (vectorFile.exists()) {
            System.out.println("Loading vector store from file...");
            ((SimpleVectorStore) vectorStoreBean).load(vectorFile);
        } else {
            System.out.println("Processing PDF and saving vectors...");
            var config = PdfDocumentReaderConfig.builder()
                    .withPageExtractedTextFormatter(new ExtractedTextFormatter.Builder().build())
                    .build();

            var pdfReader = new PagePdfDocumentReader(String.valueOf(pdfResource), config);
            var textSplitter = new TokenTextSplitter();
            vectorStoreBean.accept(textSplitter.apply(pdfReader.get()));
            ((SimpleVectorStore) vectorStoreBean).save(vectorFile);
        }
    }
}