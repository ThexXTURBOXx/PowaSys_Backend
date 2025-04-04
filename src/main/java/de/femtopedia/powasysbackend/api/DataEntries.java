package de.femtopedia.powasysbackend.api;

import java.util.Iterator;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public record DataEntries(List<DataEntry> latest, List<StrippedEntry> averages, List<StrippedEntry> max,
                          List<DataEntry> data) implements Iterable<DataEntry> {

    @NotNull
    @Override
    public Iterator<DataEntry> iterator() {
        return data.iterator();
    }

}
