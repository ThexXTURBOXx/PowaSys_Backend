package de.femtopedia.powasysbackend.service;

import de.femtopedia.powasysbackend.api.DataEntry;
import de.femtopedia.powasysbackend.api.RestartablePathReader;
import de.femtopedia.powasysbackend.api.RestartableReader;
import de.femtopedia.powasysbackend.api.SerialPort;
import de.femtopedia.powasysbackend.api.Storage;
import de.femtopedia.powasysbackend.util.Logger;
import de.femtopedia.powasysbackend.util.Util;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public record SerialReader(Storage storage, List<Thread> threads, AtomicBoolean running) {

    private static final Logger LOGGER = Logger.forClass(SerialReader.class);

    public SerialReader(Storage storage) {
        this(storage, new ArrayList<>(), new AtomicBoolean(true));
    }

    public void startListening(SerialPort serialPort) throws IOException {
        startListening(serialPort.powadorId(), serialPort.serialPort());
    }

    public void startListening(int powadorId, String serialPort) throws IOException {
        startListening(powadorId, Path.of(serialPort));
    }

    public void startListening(int powadorId, Path path) throws IOException {
        startListening(powadorId, new RestartablePathReader(path));
    }

    public void startListening(int powadorId, RestartableReader reader) {
        Thread thread = new Thread(() -> {
            while (running.get()) {
                String line = null;

                try {
                    line = reader.getReader().readLine();
                } catch (Throwable t) {
                    LOGGER.error("Error when reading serial port, restarting...", t);
                    try {
                        reader.restart();
                    } catch (Throwable th) {
                        LOGGER.error("Error when restarting serial reader, this is fatal!", th);
                    }
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
        running.set(false);

        threads.forEach(t -> {
            try {
                t.interrupt();
                t.join();
            } catch (InterruptedException e) {
                LOGGER.error("Error when stopping serial reader", e);
            }
        });
    }

}
