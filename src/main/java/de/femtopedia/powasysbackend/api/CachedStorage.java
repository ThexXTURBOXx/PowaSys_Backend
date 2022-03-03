package de.femtopedia.powasysbackend.api;

import java.io.Reader;
import java.util.List;

public interface CachedStorage extends Storage {

    void applyChanges() throws Exception;

    List<CachedEntry> getQueue();

    void clearQueue();

    void loadQueue(Reader reader);

    void dumpQueue(Appendable writer);

}
