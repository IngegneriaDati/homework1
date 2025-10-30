package it.montegucamole.indici;

import it.montegucamole.Analyzers.AppAnalyzerFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Indexer implements AutoCloseable {
    private final IndexWriter writer;

    public Indexer(Path dir) throws Exception {
        Directory directory = FSDirectory.open(dir);
        Analyzer configuredAnalyzer = AppAnalyzerFactory.createPerFieldAnalyzer("src/main/resources/analyzer_config.json");
        IndexWriterConfig config = new IndexWriterConfig(configuredAnalyzer);
        this.writer = new IndexWriter(directory, config);
    }

    public void addDocument(Path file) throws IOException {
        String nome = file.toFile().getName();
        String content = Files.readString(file);
        Document doc = new Document();
        doc.add(new TextField("titolo", nome, Field.Store.YES));
        doc.add(new TextField("content", content, Field.Store.YES));
        writer.addDocument(doc);
    }

    @Override
    public void close() throws IOException {
        try {
            writer.commit();
        } finally {
            writer.close();
        }
    }
}
