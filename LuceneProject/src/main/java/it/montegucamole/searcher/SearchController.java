package it.montegucamole.searcher;

import it.montegucamole.Utils.AppConfig;
import it.montegucamole.searcher.Searcher;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

@RestController
@RequestMapping("/search")
public class SearchController {

    private final Searcher searcher;

    public SearchController() throws Exception {
        // Carica configurazione JSON
        AppConfig.load("src/main/resources/config.json");
        var cfg = AppConfig.get();

        // Inizializza lo Searcher una sola volta
        this.searcher = new Searcher(Path.of(cfg.index_dir), cfg.analyzer_config);
    }

    @GetMapping("/query")
    public String search(@RequestParam String q, @RequestParam(defaultValue = "100") int top) throws IOException {
        TopDocs results = searcher.search(q, top);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < results.scoreDocs.length; i++) {
            ScoreDoc sd = results.scoreDocs[i];
            Document doc = searcher.doc(sd.doc);
            sb.append(String.format("%d: %s (score=%.4f)\n", i+1, doc.get("doc_id"), sd.score));
        }
        return sb.toString();
    }

    @PostMapping("/trec")
    public String generateTrecRun(@RequestParam String queryId,
                                  @RequestParam String runName,
                                  @RequestParam String queryText) throws IOException {

        TopDocs results = searcher.search(queryText, 100);
        StringBuilder sb = new StringBuilder();
        ScoreDoc[] hits = results.scoreDocs;

        for (int rank = 0; rank < hits.length; rank++) {
            ScoreDoc sd = hits[rank];
            Document doc = searcher.doc(sd.doc);
            String docId = doc.get("doc_id");

            sb.append(String.format("%s Q0 %s %d %.4f %s",
                    queryId, docId, rank + 1, sd.score, runName));
            sb.append("\n");
        }

        return sb.toString();
    }
}
