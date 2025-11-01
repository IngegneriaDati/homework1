package it.montegucamole.indici;

import it.montegucamole.Analyzers.AppAnalyzerFactory;
import it.montegucamole.Utils.ParsedDocument;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.codecs.simpletext.SimpleTextCodec;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public class Indexer implements AutoCloseable {
    private final IndexWriter writer;
    private final Set<String> indexedIds = new HashSet<>();

    public Indexer(Path dir, String analyzerConf, boolean debugger) throws Exception {
        Directory directory = FSDirectory.open(dir);
        Analyzer configuredAnalyzer = AppAnalyzerFactory.createPerFieldAnalyzer(analyzerConf).analyzer();
        IndexWriterConfig config = new IndexWriterConfig(configuredAnalyzer);

        // Importante: usa CREATE per evitare lock su indici esistenti
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);

        if (debugger) {
            config.setCodec(new SimpleTextCodec());
        }

        this.writer = new IndexWriter(directory, config);
    }

    public Indexer(Path dir, String analyzerConf) throws Exception {
        this(dir, analyzerConf, false);
    }

    public void addDocument(Path file) throws IOException {
        ParsedDocument filepar = new ParsedDocument(file);

        // Controlla duplicati
        if (indexedIds.contains(filepar.id)) {
            System.err.println("⚠️ Documento duplicato ignorato: " + filepar.id);
            return;
        }

        indexedIds.add(filepar.id);

        Document doc = new Document();

        // Campi stored e indexed
        doc.add(new StringField("doc_id", filepar.id, Field.Store.YES));
        doc.add(new TextField("titolo", filepar.titolo, Field.Store.YES));
        doc.add(new TextField("content", filepar.corpo, Field.Store.YES));

        // Aggiungi campi mancanti per migliorare la ricerca
        if (filepar.autore != null && !filepar.autore.isEmpty()) {
            doc.add(new TextField("autore", filepar.autore, Field.Store.YES));
        }

        if (filepar.bibliografia != null && !filepar.bibliografia.isEmpty()) {
            doc.add(new TextField("bibliografia", filepar.bibliografia, Field.Store.YES));
        }

        // Usa updateDocument invece di addDocument per evitare duplicati anche in caso di re-indicizzazione
        writer.updateDocument(new Term("doc_id", filepar.id), doc);
    }

    @Override
    public void close() throws IOException {
        try {
            writer.commit();
            System.out.println("📝 Indice salvato con " + indexedIds.size() + " documenti unici");
        } finally {
            writer.close();
        }
    }
}