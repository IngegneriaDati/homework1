package it.montegucamole.searcher;

import it.montegucamole.Analyzers.AppAnalyzerFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Searcher implements Closeable {
    private final IndexSearcher searcher;
    private final Analyzer analyzer;
    private final List<String> fields;
    private final IndexReader reader;

    public Searcher(Path dir, String analyzerConfPath) throws Exception {
        Directory directory = FSDirectory.open(dir);
        this.reader = DirectoryReader.open(directory);
        this.searcher = new IndexSearcher(reader);

        // Carica analyzer e campi dal JSON
        AppAnalyzerFactory.Result result = AppAnalyzerFactory.createPerFieldAnalyzer(analyzerConfPath);
        this.analyzer = result.analyzer();
        this.fields = result.fieldNames();
    }

    private Query buildQuery(String searchText) throws Exception {
        // Usa MultiFieldQueryParser con boosting per campi importanti
        Map<String, Float> boosts = new HashMap<>();
        boosts.put("titolo", 3.0f);      // Titolo molto importante
        boosts.put("content", 1.0f);     // Contenuto peso normale
        boosts.put("autore", 0.5f);      // Autore meno importante
        boosts.put("bibliografia", 0.3f); // Bibliografia ancora meno

        String[] searchFields = fields.toArray(new String[0]);

        // Crea boost array
        Map<String, Float> boostMap = new HashMap<>();
        for (String field : searchFields) {
            boostMap.put(field, boosts.getOrDefault(field, 1.0f));
        }

        MultiFieldQueryParser parser = new MultiFieldQueryParser(searchFields, analyzer, boostMap);

        // Imposta operatore di default a OR per ricerca più permissiva
        parser.setDefaultOperator(QueryParser.Operator.OR);

        return parser.parse(searchText);
    }

    public TopDocs search(String queryText, int maxHits) {
        try {
            Query query = buildQuery(queryText);
            System.out.println("🔍 Query: " + query.toString());
            return searcher.search(query, maxHits);
        } catch (Exception e) {
            throw new RuntimeException("Errore durante la ricerca: " + e.getMessage(), e);
        }
    }

    public Document doc(int docId) throws IOException {
        return searcher.storedFields().document(docId);
    }

    public int numDocs() {
        return reader.numDocs();
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }
}