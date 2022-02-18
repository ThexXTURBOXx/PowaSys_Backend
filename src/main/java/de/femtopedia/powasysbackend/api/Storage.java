package de.femtopedia.powasysbackend.api;

import java.util.List;

public interface Storage extends AutoCloseable {

    DataEntry getEntry(int id) throws Exception;

    List<DataEntry> getLast24h() throws Exception;

    void store(DataEntry dataEntry) throws Exception;

    void applyChanges() throws Exception;

}
