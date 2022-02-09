package de.femtopedia.powasysbackend.sql;

import de.femtopedia.mysql.MySQL;
import de.femtopedia.mysql.SQLConnection;
import de.femtopedia.powasysbackend.api.DataEntry;
import de.femtopedia.powasysbackend.api.Storage;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class DatabaseStorage implements Storage {

    private final MySQL mySQL;

    private final SQLConnection connection;

    public DatabaseStorage(String hostname, String port, String database, String username, String password)
            throws SQLException, ClassNotFoundException {
        this(new MySQL(hostname, port, database, username, password));
    }

    public DatabaseStorage(MySQL mySQL) throws SQLException, ClassNotFoundException {
        this(mySQL, mySQL.openConnection());
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
        try (PreparedStatement stmt = insertStmt()) {
            dataEntry.toStmt(1, stmt);
            stmt.executeUpdate();
        }
    }

    private PreparedStatement getStmt() throws SQLException {
        return connection.prepareStatement("SELECT * FROM entries WHERE id = ?;");
    }

    private PreparedStatement getLast24hStmt() throws SQLException {
        return connection.prepareStatement("SELECT * FROM entries "
                + "WHERE time > DATE_SUB(NOW(), INTERVAL 24 HOUR)");
    }

    private PreparedStatement insertStmt() throws SQLException {
        return connection.prepareStatement("INSERT INTO entries("
                + "powadorId,state,genVoltage,genCurrent,genPower,netVoltage,netCurrent,netPower,temperature) "
                + "VALUES(?,?,?,?,?,?,?,?,?);");
    }

    @Override
    public void close() throws SQLException {
        mySQL.closeConnection();
    }

}
