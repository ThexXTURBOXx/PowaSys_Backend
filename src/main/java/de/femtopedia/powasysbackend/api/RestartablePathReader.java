package de.femtopedia.powasysbackend.api;

import de.femtopedia.powasysbackend.util.Logger;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class RestartablePathReader extends RestartableReader {

    private static final Logger LOGGER = Logger.forClass(RestartableReader.class);

    private final Path path;

    public RestartablePathReader(File file) throws IOException {
        this(file.toPath());
    }

    public RestartablePathReader(Path path) throws IOException {
        super(Files.newBufferedReader(path));
        this.path = path;
    }

    public void restart() throws IOException {
        try {
            getReader().close();
        } catch (IOException e) {
            LOGGER.warning("Reader could not be closed... This might cause problems!", e);
        }

        setReader(Files.newBufferedReader(path));
    }

}
