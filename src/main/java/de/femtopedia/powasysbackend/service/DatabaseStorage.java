package de.femtopedia.powasysbackend.service;

import de.femtopedia.database.api.Database;
import de.femtopedia.database.api.SQLConnection;
import de.femtopedia.database.api.StatementParametrizer;
import de.femtopedia.database.mysql.MySQL;
import de.femtopedia.database.sqlite.SQLite;
import de.femtopedia.powasysbackend.api.CachedEntry;
import de.femtopedia.powasysbackend.api.CachedStorage;
import de.femtopedia.powasysbackend.api.DataEntries;
import de.femtopedia.powasysbackend.api.DataEntry;
import de.femtopedia.powasysbackend.api.StrippedEntry;
import de.femtopedia.powasysbackend.util.Logger;
import de.femtopedia.powasysbackend.util.Util;
import java.io.IOException;
import java.io.Reader;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class DatabaseStorage implements CachedStorage {

    private static final Logger LOGGER = Logger.forClass(DatabaseStorage.class);

    private final Database database;

    private final SQLConnection connection;

    private final Timer timer = new Timer();

    private final TimerTask applyTask = new TimerTask() {
        @Override
        public void run() {
            applyChanges();
        }
    };

    {
        timer.schedule(applyTask, 10000, 10000);
    }

    private List<CachedEntry> queue = new CopyOnWriteArrayList<>();

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
        checkConnection();

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
    public DataEntries getLast24h(double minDiv) throws SQLException {
        checkConnection();

        boolean showAll = minDiv <= 0;

        List<DataEntry> latest = new ArrayList<>();
        try (PreparedStatement stmt = getLatestStatement()) {
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                latest.add(DataEntry.fromResultSet(rs));
            }
        }

        List<StrippedEntry> averages = new ArrayList<>();
        try (PreparedStatement stmt = average24hStatement()) {
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                averages.add(StrippedEntry.fromResultSet(rs));
            }
        }

        List<StrippedEntry> max = new ArrayList<>();
        try (PreparedStatement stmt = max24hStatement()) {
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                max.add(StrippedEntry.fromResultSet(rs));
            }
        }

        List<DataEntry> dataEntries = new ArrayList<>();
        try (PreparedStatement stmt = getLast24hStmt(showAll)) {
            StatementParametrizer parametrizer = new StatementParametrizer(stmt);
            if (!showAll) {
                parametrizer.doublePrec(minDiv);
            }
            ResultSet rs = parametrizer.toStatement().executeQuery();

            while (rs.next()) {
                dataEntries.add(DataEntry.fromResultSet(rs));
            }
        }

        return new DataEntries(
                latest.stream().sorted(Comparator.comparingInt(DataEntry::getPowadorId)).collect(Collectors.toList()),
                averages.stream().sorted(Comparator.comparingInt(StrippedEntry::getPowadorId)).collect(Collectors.toList()),
                max.stream().sorted(Comparator.comparingInt(StrippedEntry::getPowadorId)).collect(Collectors.toList()),
                dataEntries
        );
    }

    @Override
    public void store(DataEntry dataEntry) {
        queue.add(new CachedEntry(OffsetDateTime.now(), dataEntry));
    }

    @Override
    public void applyChanges() {
        try {
            checkConnection();
        } catch (SQLException e) {
            LOGGER.error("Could not re-establish connection", e);
            return;
        }

        List<SQLException> errors = new ArrayList<>();
        for (CachedEntry entry : queue) {
            try (PreparedStatement stmt = insertStmt()) {
                entry.toStmt(stmt, 1);
                stmt.executeUpdate();
                queue.remove(entry);
            } catch (SQLException e) {
                if (errors.stream().noneMatch(
                        ex -> ex.getErrorCode() == e.getErrorCode() && ex.getSQLState().equals(e.getSQLState()))) {
                    errors.add(e);
                }
            }
        }

        errors.forEach(e -> LOGGER.error("Error when applying changes", e));

        try {
            dumpQueue();
        } catch (IOException e) {
            LOGGER.error("Error dumping queue", e);
        }
    }

    @Override
    public void clearQueue() {
        queue.clear();
    }

    private void checkConnection() throws SQLException {
        if (!database.isConnected()) {
            try {
                database.openConnection();
            } catch (ClassNotFoundException ignored) {
                // Should not happen as this has been checked at start already
            }
        }
    }

    private PreparedStatement getStmt() throws SQLException {
        return database.prepareStatement("SELECT * FROM entries WHERE id = ? ORDER BY time;");
    }

    private PreparedStatement getLast24hStmt(boolean showAll) throws SQLException {
        return database.prepareStatement("SELECT * FROM entries "
                + "WHERE time > DATE_SUB(NOW(), INTERVAL 24 HOUR) "
                + (showAll ? "" : "GROUP BY powadorId, DATE(time), HOUR(time), MINUTE(time) DIV ? ")
                + "ORDER BY time;");
    }

    private PreparedStatement getLatestStatement() throws SQLException {
        return database.prepareStatement("SELECT * FROM entries WHERE (powadorId,time) IN "
                + "(SELECT powadorId, MAX(time) FROM entries GROUP BY powadorId);");
    }

    private PreparedStatement average24hStatement() throws SQLException {
        return database.prepareStatement("SELECT powadorId, "
                + "AVG(genVoltage) AS genVoltage, AVG(genCurrent) AS genCurrent, AVG(genPower) AS genPower, "
                + "AVG(netVoltage) AS netVoltage, AVG(netCurrent) AS netCurrent, AVG(netPower) AS netPower, "
                + "AVG(temperature) AS temperature FROM entries "
                + "WHERE time > DATE_SUB(NOW(), INTERVAL 24 HOUR) "
                + "GROUP BY powadorId;");
    }

    private PreparedStatement max24hStatement() throws SQLException {
        return database.prepareStatement("SELECT powadorId, "
                + "MAX(genVoltage) AS genVoltage, MAX(genCurrent) AS genCurrent, MAX(genPower) AS genPower, "
                + "MAX(netVoltage) AS netVoltage, MAX(netCurrent) AS netCurrent, MAX(netPower) AS netPower, "
                + "MAX(temperature) AS temperature FROM entries "
                + "WHERE time > DATE_SUB(NOW(), INTERVAL 24 HOUR) "
                + "GROUP BY powadorId;");
    }

    private PreparedStatement insertStmt() throws SQLException {
        return database.prepareStatement("INSERT INTO entries("
                + "time,powadorId,state,genVoltage,genCurrent,genPower,netVoltage,netCurrent,netPower,temperature) "
                + "VALUES(DATE_SUB(NOW(), INTERVAL ? DAY_SECOND),?,?,?,?,?,?,?,?,?);");
    }

    @Override
    public void loadQueue(Reader reader) {
        List<CachedEntry> queue = Util.GSON.fromJson(reader, CachedEntry.CACHED_ENTRY_LIST_TYPE);
        if (queue != null) {
            this.queue = new CopyOnWriteArrayList<>(queue);
        }
    }

    @Override
    public void dumpQueue(Appendable writer) {
        Util.GSON.toJson(queue, writer);
    }

    @Override
    public void close() throws SQLException {
        database.closeConnection();
        applyTask.cancel();
        timer.cancel();
    }

}
