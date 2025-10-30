package it.montegucamole;

import it.montegucamole.dbUtils.FileScanner;
import it.montegucamole.indici.Indexer;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Main {

    private static final String INDEX_DIR_PATH = "./dir_index";
    private static final String DATA_DIR_PATH = "../dataset_titolato"; // <-- Directory con i file .txt

    public static void main(String[] args) throws Exception {

        Path dataPath = Paths.get(DATA_DIR_PATH);
        Path indexPath = Paths.get(INDEX_DIR_PATH);

        // 1. Trova tutti i file .txt nella directory dei dati
        List<Path> filesToProcess = FileScanner.findTxtFiles(dataPath);

        if (filesToProcess.isEmpty()) {
            System.out.println("Nessun file .txt trovato nella directory: " + dataPath);
            return;
        }

        System.out.println("Trovati " + filesToProcess.size() + " file da indicizzare.");

        long startTime = System.currentTimeMillis();

        // 2. Avvia l'indicizzazione con AutoCloseable
        try (Indexer indexer = new Indexer(indexPath)) {
            for (Path file : filesToProcess) {
                indexer.addDocument(file);
            }
        } // <-- qui chiude automaticamente writer.commit() + writer.close()

        long endTime = System.currentTimeMillis();

        // 3. Output finale
        System.out.println("Indicizzazione completata.");
        System.out.println("Numero di file indicizzati: " + filesToProcess.size());
        System.out.println("Tempo di indicizzazione totale: " + (endTime - startTime) + " ms");

        // 4. (Fase di ricerca da implementare)
    }
}
