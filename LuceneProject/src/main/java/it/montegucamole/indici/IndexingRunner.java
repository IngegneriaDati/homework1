package it.montegucamole.indici;

import it.montegucamole.Utils.AppConfig;
import it.montegucamole.Utils.FileScanner;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Component
@Order(1)
public class IndexingRunner implements ApplicationRunner {

    private static final String CONFIG_PATH = "src/main/resources/config.json";

    @Override
    public void run(ApplicationArguments args) throws Exception {
        System.out.println("=== Avvio Indicizzazione ===");

        // Carica configurazione
        AppConfig.load(CONFIG_PATH);
        AppConfig.Config cfg = AppConfig.get();

        Path indexPath = Paths.get(cfg.index_dir);
        Path dataPath = Paths.get(cfg.data_dir);

        // Controlla se l'indice esiste già
        if (Files.exists(indexPath) && Files.list(indexPath).findAny().isPresent()) {
            System.out.println("⚠️ Indice già esistente in: " + indexPath);
            System.out.println("Elimino l'indice precedente per evitare duplicati...");
            deleteDirectory(indexPath);
        }

        // Crea directory indice
        Files.createDirectories(indexPath);

        // Trova file da indicizzare
        List<Path> filesToProcess = FileScanner.findTxtFiles(dataPath);

        if (filesToProcess.isEmpty()) {
            System.out.println("❌ Nessun file .txt trovato in: " + dataPath);
            return;
        }

        System.out.println("📁 Trovati " + filesToProcess.size() + " file da indicizzare");

        long startTime = System.currentTimeMillis();

        // Indicizzazione con AutoCloseable
        try (Indexer indexer = new Indexer(indexPath, cfg.analyzer_config)) {
            for (Path file : filesToProcess) {
                indexer.addDocument(file);
            }
        }

        long endTime = System.currentTimeMillis();

        System.out.println("✅ Indicizzazione completata!");
        System.out.println("📊 File indicizzati: " + filesToProcess.size());
        System.out.println("⏱️ Tempo totale: " + (endTime - startTime) + " ms");
        System.out.println("=== Pronto per ricevere richieste ===\n");
    }

    private void deleteDirectory(Path path) throws Exception {
        if (Files.exists(path)) {
            Files.walk(path)
                    .sorted((a, b) -> -a.compareTo(b))
                    .forEach(p -> {
                        try {
                            Files.delete(p);
                        } catch (Exception e) {
                            System.err.println("Errore eliminazione: " + p);
                        }
                    });
        }
    }
}