package de.femtopedia.powasysbackend.service;

import de.femtopedia.powasysbackend.api.DataEntry;
import de.femtopedia.powasysbackend.api.SerialPort;
import de.femtopedia.powasysbackend.api.Storage;
import de.femtopedia.powasysbackend.util.Logger;
import de.femtopedia.powasysbackend.util.Util;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class SerialReader {

    private static final Logger LOGGER = Logger.forClass(SerialReader.class);

    private final Storage storage;

    private final List<Thread> threads = new ArrayList<>();

    private volatile boolean running = true;

    public void startListening(SerialPort serialPort) throws IOException {
        startListening(serialPort.getPowadorId(), serialPort.getSerialPort());
    }

    public void startListening(int powadorId, String serialPort) throws IOException {
        startListening(powadorId, Path.of(serialPort));
    }

    public void startListening(int powadorId, Path path) throws IOException {
        startListening(powadorId, Files.newBufferedReader(path));
    }

    public void startListening(int powadorId, BufferedReader reader) {
        Thread thread = new Thread(() -> {
            while (running) {
                String line = null;

                try {
                    line = reader.readLine();
                } catch (IOException e) {
                    LOGGER.error("Error when reading serial port", e);
                }

                if (Util.isBlank(line)) {
                    continue;
                }

                DataEntry entry = DataEntry.fromString(powadorId, line.trim());
                if (entry == null) {
                    continue;
                }

                try {
                    storage.store(entry);
                } catch (Exception e) {
                    LOGGER.error("Error when storing data", e);
                }
            }
        });

        thread.start();

        threads.add(thread);
    }

    public void close() {
        running = false;

        threads.forEach(t -> {
            try {
                t.stop();
                t.join();
            } catch (InterruptedException e) {
                LOGGER.error("Error when stopping serial reader", e);
            }
        });
    }

}
