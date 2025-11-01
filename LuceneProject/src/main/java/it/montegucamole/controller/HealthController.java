package it.montegucamole.controller;

import it.montegucamole.Utils.AppConfig;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
public class HealthController {

    @GetMapping("/")
    public String home() {
        return "🚀 Lucene Search Server is running!\n\n" +
                "Available endpoints:\n" +
                "- GET  /health - Check if index is ready\n" +
                "- GET  /search/query?q=<query>&top=<n> - Search documents\n" +
                "- POST /search/trec - Generate TREC format results\n" +
                "- GET  /search/stats - Index statistics\n";
    }

    @GetMapping("/health")
    public String health() {
        try {
            AppConfig.Config cfg = AppConfig.get();
            Path indexPath = Paths.get(cfg.index_dir);

            boolean indexExists = Files.exists(indexPath) &&
                    Files.list(indexPath).findAny().isPresent();

            if (indexExists) {
                return "✅ Index is ready at: " + indexPath;
            } else {
                return "⏳ Index not ready yet. Please wait for indexing to complete.";
            }
        } catch (Exception e) {
            return "❌ Error: " + e.getMessage();
        }
    }
}