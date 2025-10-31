package it.montegucamole.Analyzers;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class AppAnalyzerFactory {

    private static class AnalyzerConfig {
        String default_analyzer;
        List<FieldAnalyzerMapping> field_analyzers;
    }

    private static class FieldAnalyzerMapping {
        String field_name;
        String analyzer_class;
        List<String> args; // 👈 supporto argomenti
    }

    // 🔹 Nuovo tipo di ritorno
    public static class Result {
        private final Analyzer analyzer;
        private final List<String> fieldNames;

        public Result(Analyzer analyzer, List<String> fieldNames) {
            this.analyzer = analyzer;
            this.fieldNames = fieldNames;
        }

        public Analyzer analyzer() { return analyzer; }
        public List<String> fieldNames() { return fieldNames; }
    }

    // 🔹 Metodo principale modificato
    public static Result createPerFieldAnalyzer(String configFilePath) throws Exception {
        Gson gson = new Gson();
        AnalyzerConfig config;

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

        Analyzer defaultAnalyzer = instantiateAnalyzer(config.default_analyzer, Collections.emptyList());

        Map<String, Analyzer> perFieldAnalyzers = new HashMap<>();
        List<String> fieldNames = new ArrayList<>();

        if (config.field_analyzers != null) {
            for (FieldAnalyzerMapping mapping : config.field_analyzers) {
                if (mapping.field_name == null || mapping.analyzer_class == null) {
                    System.err.println("⚠️ Campo ignorato per configurazione incompleta: " + mapping);
                    continue;
                }
                Analyzer specificAnalyzer = instantiateAnalyzer(
                        mapping.analyzer_class,
                        mapping.args != null ? mapping.args : Collections.emptyList()
                );
                perFieldAnalyzers.put(mapping.field_name, specificAnalyzer);
                fieldNames.add(mapping.field_name);
            }
        }

        Analyzer perFieldWrapper = new PerFieldAnalyzerWrapper(defaultAnalyzer, perFieldAnalyzers);

        return new Result(perFieldWrapper, fieldNames);
    }

    // 🔹 Metodo per istanziare dinamicamente un analyzer con argomenti
    private static Analyzer instantiateAnalyzer(String className, List<String> args) throws Exception {
        String fullClassName = className.contains(".")
                ? className
                : "org.apache.lucene.analysis.standard." + className;

        Class<?> clazz = Class.forName(fullClassName);

        if (args.isEmpty()) {
            return (Analyzer) clazz.getDeclaredConstructor().newInstance();
        } else {
            // per ora assumiamo che gli argomenti siano String, ma puoi adattare
            return (Analyzer) clazz.getDeclaredConstructor(String.class).newInstance(args.get(0));
        }
    }
}
