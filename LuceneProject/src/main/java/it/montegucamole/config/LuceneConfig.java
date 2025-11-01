package it.montegucamole.config;

import it.montegucamole.Utils.AppConfig;
import it.montegucamole.searcher.Searcher;
import jakarta.annotation.PreDestroy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class LuceneConfig {

    private static final String CONFIG_PATH = "src/main/resources/config.json";
    private Searcher searcher;

    @Bean
    @Lazy // Importante: crea il bean solo quando richiesto
    public Searcher searcher() throws Exception {
        // Carica configurazione se non già caricata
        try {
            AppConfig.get();
        } catch (IllegalStateException e) {
            AppConfig.load(CONFIG_PATH);
        }

        AppConfig.Config cfg = AppConfig.get();
        Path indexPath = Paths.get(cfg.index_dir);

        // Verifica che l'indice esista
        if (!Files.exists(indexPath) || !Files.list(indexPath).findAny().isPresent()) {
            throw new IllegalStateException(
                    "Indice non trovato. L'indicizzazione dovrebbe essere completata prima.");
        }

        this.searcher = new Searcher(indexPath, cfg.analyzer_config);
        System.out.println("🔍 Searcher bean creato con " + searcher.numDocs() + " documenti");
        return searcher;
    }

    @PreDestroy
    public void cleanup() throws IOException {
        if (searcher != null) {
            System.out.println("🛑 Chiusura Searcher...");
            searcher.close();
        }
    }
}