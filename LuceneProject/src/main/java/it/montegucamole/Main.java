package it.montegucamole;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Main {
    public static void main(String[] args) {
        // Rimuovi tutta la logica di indicizzazione da qui
        // L'indicizzazione verrà gestita da IndexingRunner
        SpringApplication.run(Main.class, args);
    }
}