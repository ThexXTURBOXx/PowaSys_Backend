package de.femtopedia.powasysbackend.api;

public interface Storage extends AutoCloseable {

    DataEntry getEntry(int id) throws Exception;

    DataEntries getLast24h() throws Exception;

    void store(DataEntry dataEntry) throws Exception;

}
