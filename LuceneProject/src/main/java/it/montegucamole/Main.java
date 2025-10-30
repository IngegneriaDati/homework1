package it.montegucamole;

import it.montegucamole.Utils.AppConfig;
import it.montegucamole.Utils.FileScanner;
import it.montegucamole.indici.Indexer;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Main {

    private static final String CONFIG_PATH = "src/main/resources/config.json";

    public static void main(String[] args) throws Exception {

        // 1. Carica configurazione JSON
        AppConfig.load(CONFIG_PATH);
        AppConfig.Config cfg = AppConfig.get();

        Path dataPath = Paths.get(cfg.data_dir);

        // 2. Trova tutti i file .txt
        List<Path> filesToProcess = FileScanner.findTxtFiles(dataPath);

        if (filesToProcess.isEmpty()) {
            System.out.println("Nessun file .txt trovato nella directory: " + dataPath);
            return;
        }

        System.out.println("Trovati " + filesToProcess.size() + " file da indicizzare.");

        long startTime = System.currentTimeMillis();

        // 3. Indicizzazione con AutoCloseable
        try (Indexer indexer = new Indexer(Paths.get(cfg.index_dir),cfg.analyzer_config)) {
            for (Path file : filesToProcess) {
                indexer.addDocument(file);
            }
        }

        long endTime = System.currentTimeMillis();

        System.out.println("Indicizzazione completata.");
        System.out.println("Numero di file indicizzati: " + filesToProcess.size());
        System.out.println("Tempo di indicizzazione totale: " + (endTime - startTime) + " ms");
    }
}
