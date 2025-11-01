package it.montegucamole.searcher;

import it.montegucamole.Utils.AppConfig;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.nio.file.Path;

@RestController
@RequestMapping("/search")
public class SearchController {

    private Searcher searcher;

    // Inizializzazione lazy - crea il Searcher solo al primo uso
    private Searcher getSearcher() throws Exception {
        if (searcher == null) {
            synchronized (this) {
                if (searcher == null) {
                    AppConfig.Config cfg = AppConfig.get();
                    searcher = new Searcher(Path.of(cfg.index_dir), cfg.analyzer_config);
                    System.out.println("🔍 Searcher inizializzato con " + searcher.numDocs() + " documenti");
                }
            }
        }
        return searcher;
    }

    @GetMapping("/query")
    public String search(@RequestParam String q, @RequestParam(defaultValue = "100") int top) throws Exception {
        Searcher s = getSearcher();
        TopDocs results = s.search(q, top);

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Trovati %d risultati per: %s\n\n", results.totalHits.value, q));

        for (int i = 0; i < results.scoreDocs.length; i++) {
            ScoreDoc sd = results.scoreDocs[i];
            Document doc = s.doc(sd.doc);
            sb.append(String.format("%d. DOC_ID: %s\n", i + 1, doc.get("doc_id")));
            sb.append(String.format("   Titolo: %s\n", doc.get("titolo")));
            sb.append(String.format("   Score: %.4f\n\n", sd.score));
        }

        return sb.toString();
    }

    @PostMapping("/trec")
    public String generateTrecRun(@RequestParam String queryId,
                                  @RequestParam String runName,
                                  @RequestParam String queryText) throws Exception {

        Searcher s = getSearcher();
        TopDocs results = s.search(queryText, 1000);
        StringBuilder sb = new StringBuilder();
        ScoreDoc[] hits = results.scoreDocs;

        for (int rank = 0; rank < hits.length; rank++) {
            ScoreDoc sd = hits[rank];
            Document doc = s.doc(sd.doc);
            String docId = doc.get("doc_id");

            sb.append(String.format("%s Q0 %s %d %.6f %s\n",
                    queryId, docId, rank + 1, sd.score, runName));
        }

        return sb.toString();
    }

    @GetMapping("/stats")
    public String getStats() throws Exception {
        Searcher s = getSearcher();
        return String.format("Documenti indicizzati: %d", s.numDocs());
    }

    @PreDestroy
    public void cleanup() throws IOException {
        if (searcher != null) {
            System.out.println("🛑 Chiusura Searcher...");
            searcher.close();
        }
    }
}