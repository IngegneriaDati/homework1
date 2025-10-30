package it.montegucamole.Utils;

import com.google.gson.Gson;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;

public class AppConfig {

    public static class Config {
        public String index_dir;
        public String data_dir;
        public String analyzer_config;
    }

    private static Config config;

    public static void load(String configPath) throws Exception {
        Gson gson = new Gson();
        try (Reader reader = Files.newBufferedReader(Paths.get(configPath))) {
            config = gson.fromJson(reader, Config.class);
        }

        if (config.index_dir == null || config.data_dir == null || config.analyzer_config == null) {
            throw new IllegalStateException("Configurazione incompleta nel file JSON: " + configPath);
        }
    }

    public static Config get() {
        if (config == null) throw new IllegalStateException("Configurazione non ancora caricata");
        return config;
    }
}

