package de.femtopedia.powasysbackend.api;

import java.util.Iterator;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Data
@AllArgsConstructor
public class DataEntries implements Iterable<DataEntry> {

    private final List<DataEntry> latest;
    private final List<AverageEntry> averages;
    private final List<DataEntry> data;

    @NotNull
    @Override
    public Iterator<DataEntry> iterator() {
        return data.iterator();
    }

}
