package it.montegucamole.Utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileScanner {

    /**
     * Scansiona ricorsivamente una directory e restituisce tutti i file .txt.
     * @param directoryPath La directory da cui iniziare la scansione.
     * @return Una lista di Path che puntano solo ai file .txt.
     * @throws IOException Se si verifica un errore di I/O durante la scansione.
     */
    public static List<Path> findTxtFiles(Path directoryPath) throws IOException {
        // Files.walk() attraversa ricorsivamente l'albero di directory.
        try (Stream<Path> walk = Files.walk(directoryPath)) {
            return walk
                    .filter(Files::isRegularFile) // Filtra solo i file, escludendo le directory
                    .filter(p -> p.toString().endsWith(".txt")) // Filtra solo quelli con estensione .txt
                    .collect(Collectors.toList());
        }
    }
}