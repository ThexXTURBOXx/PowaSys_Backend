package de.femtopedia.powasysbackend.sql;

import de.femtopedia.mysql.MySQL;
import de.femtopedia.mysql.SQLConnection;
import de.femtopedia.powasysbackend.api.DataEntry;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class DatabaseStorage {

    private final MySQL mySQL;

    private final SQLConnection connection;

    public DatabaseStorage(String hostname, String port, String database, String username, String password)
            throws SQLException, ClassNotFoundException {
        this(new MySQL(hostname, port, database, username, password));
    }

    public DatabaseStorage(MySQL mySQL) throws SQLException, ClassNotFoundException {
        this(mySQL, mySQL.openConnection());
    }

    public void insert(DataEntry dataEntry) throws SQLException {
        insert(dataEntry.getState(),
                dataEntry.getGenVoltage(), dataEntry.getGenCurrent(), dataEntry.getGenPower(),
                dataEntry.getNetVoltage(), dataEntry.getNetCurrent(), dataEntry.getNetPower(),
                dataEntry.getTemperature());
    }

    public void insert(int state, double genVoltage, double genCurrent, int genPower,
                       double netVoltage, double netCurrent, int netPower, int temperature) throws SQLException {
        try (PreparedStatement stmt = insertStmt()) {
            stmt.setInt(1, state);
            stmt.setDouble(2, genVoltage);
            stmt.setDouble(3, genCurrent);
            stmt.setInt(4, genPower);
            stmt.setDouble(5, netVoltage);
            stmt.setDouble(6, netCurrent);
            stmt.setInt(7, netPower);
            stmt.setInt(8, temperature);
            stmt.executeUpdate();
        }
    }

    public PreparedStatement insertStmt() throws SQLException {
        return connection.prepareStatement("INSERT INTO entries("
                + "state,genVoltage,genCurrent,genPower,netVoltage,netCurrent,netPower,temperature) "
                + "VALUES(?,?,?,?,?,?,?,?);");
    }

    public void shutdown() throws SQLException {
        mySQL.closeConnection();
    }

}
