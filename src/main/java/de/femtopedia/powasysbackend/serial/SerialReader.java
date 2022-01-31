package de.femtopedia.powasysbackend.serial;

import de.femtopedia.powasysbackend.api.DataEntry;
import de.femtopedia.powasysbackend.sql.DatabaseStorage;
import de.femtopedia.powasysbackend.util.Util;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class SerialReader {

    private final DatabaseStorage storage;

    private final List<Thread> threads = new ArrayList<>();

    public void startListening(String serialPort) throws IOException {
        startListening(Path.of(serialPort));
    }

    public void startListening(Path path) throws IOException {
        startListening(Files.newBufferedReader(path));
    }

    public void startListening(BufferedReader reader) {
        Thread thread = new Thread(() -> {
            while (true) {
                String line = null;

                try {
                    line = reader.readLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (Util.isBlank(line)) {
                    continue;
                }

                DataEntry entry = DataEntry.fromString(line.trim());
                if (entry == null) {
                    continue;
                }

                try {
                    entry.insertIntoDatabase(storage);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();

        threads.add(thread);
    }

    public void shutdown() {
    }

}
