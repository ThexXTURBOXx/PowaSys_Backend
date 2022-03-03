package de.femtopedia.powasysbackend.api;

import com.google.gson.reflect.TypeToken;
import de.femtopedia.database.api.StatementParametrizer;
import java.lang.reflect.Type;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CachedEntry {

    public static final Type CACHED_ENTRY_LIST_TYPE = new TypeToken<List<CachedEntry>>() {
    }.getType();

    private final OffsetDateTime from;
    private final DataEntry entry;

    public void toStmt(PreparedStatement stmt, int startIndex) throws SQLException {
        new StatementParametrizer(stmt, startIndex++)
                .longInt(Duration.between(from, OffsetDateTime.now()).getSeconds());
        entry.toStmt(stmt, startIndex);
    }

}
