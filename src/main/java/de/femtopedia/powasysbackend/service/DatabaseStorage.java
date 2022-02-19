package de.femtopedia.powasysbackend.service;

import de.femtopedia.database.api.Database;
import de.femtopedia.database.api.SQLConnection;
import de.femtopedia.database.mysql.MySQL;
import de.femtopedia.database.sqlite.SQLite;
import de.femtopedia.powasysbackend.api.CachedStorage;
import de.femtopedia.powasysbackend.api.DataEntry;
import de.femtopedia.powasysbackend.util.Logger;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class DatabaseStorage implements CachedStorage {

    private static final Logger LOGGER = Logger.forClass(DatabaseStorage.class);

    private final Database database;

    private final SQLConnection connection;

    private final Deque<DataEntry> queue = new LinkedList<>();

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
    public void store(DataEntry dataEntry) {
        queue.offer(dataEntry);
    }

    @Override
    public void applyChanges() {
        while (!queue.isEmpty()) {
            DataEntry dataEntry = queue.peek();
            try (PreparedStatement stmt = insertStmt()) {
                dataEntry.toStmt(stmt, 1);
                stmt.executeUpdate();
                queue.poll();
            } catch (SQLException e) {
                LOGGER.error("Error when applying changes", e);
            }
        }
    }

    @Override
    public void clearQueue() {
        queue.clear();
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
        applyTask.cancel();
        timer.cancel();
    }

}
