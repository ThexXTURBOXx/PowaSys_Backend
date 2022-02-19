package de.femtopedia.powasysbackend.sql;

import de.femtopedia.database.api.Database;
import de.femtopedia.database.api.SQLConnection;
import de.femtopedia.database.mysql.MySQL;
import de.femtopedia.database.sqlite.SQLite;
import de.femtopedia.powasysbackend.api.DataEntry;
import de.femtopedia.powasysbackend.api.Storage;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class DatabaseStorage implements Storage {

    private final Database database;

    private final SQLConnection connection;

    private final Deque<DataEntry> toStore = new LinkedList<>();

    public DatabaseStorage(String dbLocation) throws SQLException, ClassNotFoundException {
        this(new SQLite(dbLocation));
    }

    public DatabaseStorage(String hostname, String port, String database, String username, String password)
            throws SQLException, ClassNotFoundException {
        this(new MySQL(hostname, port, database, username, password));
    }

    public DatabaseStorage(Database database) throws SQLException, ClassNotFoundException {
        this(database, database.openConnection());
    }

    @Override
    public DataEntry getEntry(int id) throws SQLException {
        try (PreparedStatement stmt = getStmt()) {
            stmt.setInt(1, id);

            ResultSet rs = stmt.executeQuery();

            DataEntry dataEntry = null;
            if (rs.next()) {
                dataEntry = DataEntry.fromResultSet(rs);
            }

            return dataEntry;
        }
    }

    @Override
    public List<DataEntry> getLast24h() throws SQLException {
        List<DataEntry> dataEntries = new ArrayList<>();

        try (PreparedStatement stmt = getLast24hStmt()) {
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                dataEntries.add(DataEntry.fromResultSet(rs));
            }
        }

        return dataEntries;
    }

    @Override
    public void store(DataEntry dataEntry) throws SQLException {
        toStore.offer(dataEntry);
        applyChanges();
    }

    @Override
    public void applyChanges() throws SQLException {
        while (!toStore.isEmpty()) {
            DataEntry dataEntry = toStore.peek();
            try (PreparedStatement stmt = insertStmt()) {
                dataEntry.toStmt(stmt, 1);
                stmt.executeUpdate();
                toStore.poll();
            }
        }
    }

    private PreparedStatement getStmt() throws SQLException {
        return database.prepareStatement("SELECT * FROM entries WHERE id = ?;");
    }

    private PreparedStatement getLast24hStmt() throws SQLException {
        return database.prepareStatement("SELECT * FROM entries "
                + "WHERE time > DATE_SUB(NOW(), INTERVAL 24 HOUR)");
    }

    private PreparedStatement insertStmt() throws SQLException {
        return database.prepareStatement("INSERT INTO entries("
                + "powadorId,state,genVoltage,genCurrent,genPower,netVoltage,netCurrent,netPower,temperature) "
                + "VALUES(?,?,?,?,?,?,?,?,?);");
    }

    @Override
    public void close() throws SQLException {
        database.closeConnection();
    }

}
