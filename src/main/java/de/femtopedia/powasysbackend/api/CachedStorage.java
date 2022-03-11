package de.femtopedia.powasysbackend.api;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public interface CachedStorage extends Storage {

    Path DEFAULT_QUEUE_FILE = Path.of("queue.json");

    void applyChanges() throws Exception;

    List<CachedEntry> getQueue();

    void clearQueue();

    default void loadQueue() throws IOException {
        createQueueFile();
        try (BufferedReader br = Files.newBufferedReader(DEFAULT_QUEUE_FILE)) {
            loadQueue(br);
        }
    }

    void loadQueue(Reader reader);

    default void dumpQueue() throws IOException {
        createQueueFile();
        try (BufferedWriter bw = Files.newBufferedWriter(DEFAULT_QUEUE_FILE)) {
            dumpQueue(bw);
        }
    }

    void dumpQueue(Appendable writer);

    default void createQueueFile() throws IOException {
        if (!Files.isRegularFile(DEFAULT_QUEUE_FILE)) {
            Files.createFile(DEFAULT_QUEUE_FILE);
        }
    }

}
