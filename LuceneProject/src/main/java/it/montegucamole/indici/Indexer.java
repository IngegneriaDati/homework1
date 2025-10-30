package it.montegucamole.indici;

import it.montegucamole.Analyzers.AppAnalyzerFactory;
import it.montegucamole.Utils.ParsedDocument;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Indexer implements AutoCloseable {
    private final IndexWriter writer;

    public Indexer(Path dir,String AnalizerConf) throws Exception {
        Directory directory = FSDirectory.open(dir);
        Analyzer configuredAnalyzer = AppAnalyzerFactory.createPerFieldAnalyzer(AnalizerConf);
        IndexWriterConfig config = new IndexWriterConfig(configuredAnalyzer);
        this.writer = new IndexWriter(directory, config);
    }

    public void addDocument(Path file) throws IOException {
        ParsedDocument filepar = new ParsedDocument(file);
        Document doc = new Document();
        doc.add(new StringField("doc_id", filepar.id, Field.Store.YES));
        doc.add(new TextField("titolo", filepar.titolo, Field.Store.YES));
        doc.add(new TextField("content", filepar.corpo, Field.Store.YES));
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
