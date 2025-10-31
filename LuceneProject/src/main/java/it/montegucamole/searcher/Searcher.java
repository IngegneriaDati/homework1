package it.montegucamole.searcher;

import it.montegucamole.Analyzers.AppAnalyzerFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class Searcher implements Closeable {
    private final IndexSearcher searcher;
    private final Analyzer analyzer;
    private final List<String> fields;
    private final IndexReader reader;

    public Searcher(Path dir, String analyzerConfPath) throws Exception {
        Directory directory = FSDirectory.open(dir);
        this.reader = DirectoryReader.open(directory);
        this.searcher = new IndexSearcher(reader);

        // Carica analyzer e campi dal JSON solo una volta
        AppAnalyzerFactory.Result result = AppAnalyzerFactory.createPerFieldAnalyzer(analyzerConfPath);
        this.analyzer = result.analyzer();
        this.fields = result.fieldNames();
    }

    private Query buildQuery(String searchText) throws Exception {
        BooleanQuery.Builder builder = new BooleanQuery.Builder();

        for (String field : fields) {
            QueryParser parser = new QueryParser(field, analyzer);
            Query fieldQuery = parser.parse(searchText);
            builder.add(fieldQuery, BooleanClause.Occur.SHOULD);
        }

        return builder.build();
    }

    public TopDocs search(String queryText, int maxHits) {
        try {
            Query query = buildQuery(queryText);
            return searcher.search(query, maxHits);
        } catch (Exception e) {
            throw new RuntimeException("Errore durante la ricerca", e);
        }
    }

    public Document doc(int docId) throws IOException {
        return searcher.storedFields().document(docId);
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }
}
