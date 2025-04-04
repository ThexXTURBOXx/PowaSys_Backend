package de.femtopedia.powasysbackend.api;

import de.femtopedia.database.api.StatementParametrizer;
import de.femtopedia.powasysbackend.util.Util;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public record DataEntry(int powadorId, String unused, String time, int state, double genVoltage, double genCurrent,
                        int genPower, double netVoltage, double netCurrent, int netPower, int temperature) {

    public static int DATA_ENTRIES = 10;
    public static int FALLBACK = -1;
    public static double FALLBACK_D = -1D;

    public static DataEntry fromResultSet(ResultSet rs) throws SQLException {
        return new DataEntry(
                rs.getInt("powadorId"),
                "00.00.0000",
                rs.getString("time"),
                rs.getInt("state"),
                rs.getDouble("genVoltage"),
                rs.getDouble("genCurrent"),
                rs.getInt("genPower"),
                rs.getDouble("netVoltage"),
                rs.getDouble("netCurrent"),
                rs.getInt("netPower"),
                rs.getInt("temperature")
        );
    }

    public static DataEntry fromString(int powadorId, String data) {
        String[] split = data.split("\\s+");
        return fromStringData(powadorId, split);
    }

    public static DataEntry fromStringData(int powadorId, String[] data) {
        if (data.length != DATA_ENTRIES) {
            return null;
        }

        int state = Util.parseInt(data[2]).orElse(FALLBACK);
        double genVoltage = Util.parseDouble(data[3]).orElse(FALLBACK_D);
        double genCurrent = Util.parseDouble(data[4]).orElse(FALLBACK_D);
        int genPower = Util.parseInt(data[5]).orElse(FALLBACK);
        double netVoltage = Util.parseDouble(data[6]).orElse(FALLBACK_D);
        double netCurrent = Util.parseDouble(data[7]).orElse(FALLBACK_D);
        int netPower = Util.parseInt(data[8]).orElse(FALLBACK);
        int temperature = Util.parseInt(data[9]).orElse(FALLBACK);

        return new DataEntry(
                powadorId,
                data[0],
                data[1],
                state,
                genVoltage,
                genCurrent,
                genPower,
                netVoltage,
                netCurrent,
                netPower,
                temperature
        );
    }

    public void toStmt(PreparedStatement stmt, int startIndex) throws SQLException {
        new StatementParametrizer(stmt, startIndex)
                .integer(powadorId)
                .integer(state)
                .doublePrec(genVoltage)
                .doublePrec(genCurrent)
                .integer(genPower)
                .doublePrec(netVoltage)
                .doublePrec(netCurrent)
                .integer(netPower)
                .integer(temperature);
    }

}
