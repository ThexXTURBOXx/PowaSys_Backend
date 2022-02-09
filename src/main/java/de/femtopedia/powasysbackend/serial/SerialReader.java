package de.femtopedia.powasysbackend.serial;

import de.femtopedia.powasysbackend.api.DataEntry;
import de.femtopedia.powasysbackend.api.SerialPort;
import de.femtopedia.powasysbackend.api.Storage;
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
                    e.printStackTrace();
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
                    e.printStackTrace();
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
                e.printStackTrace();
            }
        });
    }

}
