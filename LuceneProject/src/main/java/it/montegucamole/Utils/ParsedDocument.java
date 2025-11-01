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

        // Debug: stampa quando l'ID è vuoto
        if (id == null || id.isEmpty()) {
            System.err.println("⚠️ ID non trovato per file: " + file.getFileName());
            System.err.println("Prime 200 caratteri del file:");
            System.err.println(content.substring(0, Math.min(200, content.length())));
            System.err.println("---");
        }
    }

    private String extractField(String content, String fieldName) {
        // Pattern migliorato:
        // - \s* = zero o più spazi/tab dopo il campo
        // - (.+?) = cattura il valore (non-greedy)
        // - (?:\r?\n|$) = termina con newline (Windows o Unix) o fine stringa
        Pattern pattern = Pattern.compile(
                fieldName + "\\s*(.+?)(?:\\r?\\n|$)",
                Pattern.CASE_INSENSITIVE
        );
        Matcher matcher = pattern.matcher(content);

        if (matcher.find()) {
            String value = matcher.group(1).trim();
            return value.isEmpty() ? null : value;
        }
        return null;
    }

    private String extractBody(String content) {
        // Cerca "Bibliografia:" in modo case-insensitive
        Pattern bibPattern = Pattern.compile("Bibliografia:", Pattern.CASE_INSENSITIVE);
        Matcher matcher = bibPattern.matcher(content);

        if (!matcher.find()) {
            return content.trim();
        }

        int bibliografiaIndex = matcher.start();
        int bodyStart = content.indexOf('\n', bibliografiaIndex);

        // Gestisce anche \r\n (Windows)
        if (bodyStart != -1) {
            String body = content.substring(bodyStart + 1).trim();
            // Rimuove eventuali \r residui
            return body.replace("\r", "");
        }

        return "";
    }
}