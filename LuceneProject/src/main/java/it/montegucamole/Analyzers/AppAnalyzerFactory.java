package it.montegucamole.Analyzers;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppAnalyzerFactory {

    // -----------------------------------------------------
    // CLASSI INTERNE PER LA MAPPATURA DEL JSON
    // -----------------------------------------------------

    private static class AnalyzerConfig {
        String default_analyzer;
        List<FieldAnalyzerMapping> field_analyzers;
    }

    private static class FieldAnalyzerMapping {
        String field_name;
        String analyzer_class;
    }

    // -----------------------------------------------------
    // METODO PUBBLICO PRINCIPALE PER CREARE L'ANALYZER
    // -----------------------------------------------------

    public static Analyzer createPerFieldAnalyzer(String configFilePath) throws Exception {
        Gson gson = new Gson();
        AnalyzerConfig config;

        // 1. Legge il file JSON in modo sicuro
        try (Reader reader = Files.newBufferedReader(Paths.get(configFilePath))) {
            config = gson.fromJson(reader, AnalyzerConfig.class);
        } catch (IOException e) {
            throw new IOException("Errore nella lettura del file di configurazione: " + configFilePath, e);
        } catch (JsonSyntaxException e) {
            throw new IllegalArgumentException("Formato JSON non valido nel file: " + configFilePath, e);
        }

        if (config == null || config.default_analyzer == null) {
            throw new IllegalStateException("Configurazione JSON mancante o incompleta (default_analyzer richiesto).");
        }

        // 2. Crea l'analyzer di default
        Analyzer defaultAnalyzer = instantiateAnalyzer(config.default_analyzer);

        // 3. Mappa gli analyzer per campo
        Map<String, Analyzer> perFieldAnalyzers = new HashMap<>();

        if (config.field_analyzers != null) {
            for (FieldAnalyzerMapping mapping : config.field_analyzers) {
                if (mapping.field_name == null || mapping.analyzer_class == null) {
                    System.err.println("⚠️ Campo ignorato per configurazione incompleta: " + mapping);
                    continue;
                }
                Analyzer specificAnalyzer = instantiateAnalyzer(mapping.analyzer_class);
                perFieldAnalyzers.put(mapping.field_name, specificAnalyzer);
            }
        }

        // 4. Restituisce il wrapper
        return new PerFieldAnalyzerWrapper(defaultAnalyzer, perFieldAnalyzers);
    }

    // -----------------------------------------------------
    // METODO DI SUPPORTO PER CREARE GLI ANALYZER DINAMICAMENTE
    // -----------------------------------------------------

    private static Analyzer instantiateAnalyzer(String className) throws Exception {
        String fullClassName = className.contains(".")
                ? className
                : "org.apache.lucene.analysis.standard." + className;

        try {
            Class<?> clazz = Class.forName(fullClassName);
            Object instance = clazz.getDeclaredConstructor().newInstance();
            if (!(instance instanceof Analyzer)) {
                throw new IllegalArgumentException("La classe non è un Analyzer valido: " + fullClassName);
            }
            return (Analyzer) instance;
        } catch (ClassNotFoundException e) {
            throw new ClassNotFoundException("Analyzer non trovato: " + fullClassName, e);
        }
    }
}
