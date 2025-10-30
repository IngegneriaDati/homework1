package it.montegucamole.Utils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Struttura dei dati Parsati
public class ParsedDocument {
    public String id;
    public String autore;
    public String bibliografia;
    public String titolo; // Il titolo dell'articolo, non il nome del file
    public String corpo;

    public ParsedDocument(Path file) throws IOException {
        //ParsedDocument doc = new ParsedDocument();

        // Leggi il contenuto del file
        String content = Files.readString(file);

        // Estrai il titolo dal nome del file
        titolo = file.getFileName().toString().replaceAll("\\.txt$", "");

        // Estrai i metadati usando regex
        id = extractField(content, "ID:");
        autore = extractField(content, "Autore:");
        bibliografia = extractField(content, "Bibliografia:");

        // Estrai il corpo (tutto dopo "Bibliografia:")
        corpo = extractBody(content);
    }

    private String extractField(String content, String fieldName) {
        Pattern pattern = Pattern.compile(fieldName + "\\s*(.+?)\\n", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(content);
        return matcher.find() ? matcher.group(1).trim() : "";
    }

    private String extractBody(String content) {
        int bibliografiaIndex = content.indexOf("Bibliografia:");
        if (bibliografiaIndex == -1) {
            return content.trim();
        }

        int bodyStart = content.indexOf('\n', bibliografiaIndex);
        return bodyStart != -1 ? content.substring(bodyStart + 1).trim() : "";
    }
}