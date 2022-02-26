package de.femtopedia.powasysbackend.api;

import de.femtopedia.database.api.StatementParametrizer;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Duration;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CachedEntry {

    private final OffsetDateTime from;
    private final DataEntry entry;

    public void toStmt(PreparedStatement stmt, int startIndex) throws SQLException {
        new StatementParametrizer(stmt, startIndex++)
                .longInt(Duration.between(from, OffsetDateTime.now()).getSeconds());
        entry.toStmt(stmt, startIndex);
    }

}
