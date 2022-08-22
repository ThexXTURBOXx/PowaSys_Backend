package de.femtopedia.powasysbackend.api;

import java.io.BufferedReader;
import java.io.IOException;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public abstract class RestartableReader {

    private BufferedReader reader;

    private RestartableReader() {
        throw new UnsupportedOperationException();
    }

    public abstract void restart() throws IOException;

}
