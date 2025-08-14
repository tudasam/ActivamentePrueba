package com.tudasam.prueba;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class DocumentService {

    private static final Path DATA_DIR = Paths.get("data");
    private static final Path VECTOR_FILE = DATA_DIR.resolve("vectorstore.json");

    private final EmbeddingModel embeddingModel;
    private final AtomicReference<VectorStore> currentStore = new AtomicReference<>(null);

    public DocumentService(EmbeddingModel embeddingModel) throws IOException {
        this.embeddingModel = embeddingModel;
        Files.createDirectories(DATA_DIR);
    }

    /** Ingest a single PDF; it becomes the ONLY active document (overwrites any previous). */
    public synchronized void ingestSingle(MultipartFile pdf) {
        try {
            var config = PdfDocumentReaderConfig.builder()
                    .withPageExtractedTextFormatter(new ExtractedTextFormatter.Builder().build())
                    .build();

            var reader = new PagePdfDocumentReader(new InputStreamResource(pdf.getInputStream()), config);
            List<Document> pages = reader.get();

            var splitter = new TokenTextSplitter();
            List<Document> chunks = splitter.apply(pages);

            // Build a fresh store using the injected EmbeddingModel
            SimpleVectorStore vs = SimpleVectorStore.builder(embeddingModel).build();
            vs.accept(chunks);

            // Persist and set as current
            vs.save(VECTOR_FILE.toFile());
            currentStore.set(vs);

        } catch (Exception e) {
            throw new RuntimeException("Failed to ingest PDF", e);
        }
    }

    /** Get the active store; lazy-load from disk if necessary. */
    public synchronized VectorStore getActive() {
        VectorStore vs = currentStore.get();
        if (vs != null) return vs;

        if (!Files.exists(VECTOR_FILE)) {
            throw new IllegalStateException("No document is loaded. Upload a PDF first.");
        }
        SimpleVectorStore loaded = SimpleVectorStore.builder(embeddingModel).build();
        loaded.load(VECTOR_FILE.toFile());
        currentStore.set(loaded);
        return loaded;
    }

    /** Optional: clear current store and delete the saved file. */
    public synchronized void clear() throws IOException {
        currentStore.set(null);
        Files.deleteIfExists(VECTOR_FILE);
    }
}

