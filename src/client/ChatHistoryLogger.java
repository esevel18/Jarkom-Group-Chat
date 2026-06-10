package client;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ChatHistoryLogger {
    private static final Path HISTORY_FILE = Path.of("chat_history.csv");
    private static final String HEADER = "timestamp,username,room,message";
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void logMessage(String username, String room, String message) {
        try {
            if (Files.notExists(HISTORY_FILE)) {
                Files.createFile(HISTORY_FILE);
                try (BufferedWriter writer = Files.newBufferedWriter(HISTORY_FILE, StandardCharsets.UTF_8, StandardOpenOption.APPEND)) {
                    writer.write(HEADER);
                    writer.newLine();
                }
            }

            String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
            String csvLine = escapeCsv(timestamp) + "," + escapeCsv(username) + "," + escapeCsv(room) + "," + escapeCsv(message);

            try (BufferedWriter writer = Files.newBufferedWriter(HISTORY_FILE, StandardCharsets.UTF_8, StandardOpenOption.APPEND)) {
                writer.write(csvLine);
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Gagal mencatat riwayat chat: " + e.getMessage());
        }
    }

    private static String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        String escaped = value.replace("\"", "\"\"");
        return "\"" + escaped + "\"";
    }
}
