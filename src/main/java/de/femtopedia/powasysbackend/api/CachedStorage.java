package de.femtopedia.powasysbackend.api;

import java.util.Deque;

public interface CachedStorage extends Storage {

    void applyChanges() throws Exception;

    Deque<DataEntry> getQueue();

    void clearQueue();

}
