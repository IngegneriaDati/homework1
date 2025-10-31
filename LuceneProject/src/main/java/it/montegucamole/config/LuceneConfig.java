package it.montegucamole.config;

import it.montegucamole.Utils.AppConfig;
import it.montegucamole.Utils.FileScanner;
import it.montegucamole.indici.Indexer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;
import java.util.List;

@Configuration
public class LuceneConfig {

    private static final String CONFIG_PATH = "src/main/resources/config.json";

    @Bean
    public Indexer indexer() throws Exception {
        // 1. Carica configurazione JSON
        AppConfig.load(CONFIG_PATH);
        var cfg = AppConfig.get();

        Path dataPath = Path.of(cfg.data_dir);

        // 2. Trova tutti i file .txt
        List<Path> filesToProcess = FileScanner.findTxtFiles(dataPath);

        if (filesToProcess.isEmpty()) {
            System.out.println("Nessun file .txt trovato nella directory: " + dataPath);
        } else {
            System.out.println("Trovati " + filesToProcess.size() + " file da indicizzare.");
        }

        long startTime = System.currentTimeMillis();

        // 3. Indicizzazione
        Indexer indexer = new Indexer(Path.of(cfg.index_dir), cfg.analyzer_config);
        for (Path file : filesToProcess) {
            indexer.addDocument(file);
        }

        long endTime = System.currentTimeMillis();

        System.out.println("Indicizzazione completata.");
        System.out.println("Numero di file indicizzati: " + filesToProcess.size());
        System.out.println("Tempo di indicizzazione totale: " + (endTime - startTime) + " ms");

        return indexer; // il bean viene mantenuto per tutto il servizio
    }
}
