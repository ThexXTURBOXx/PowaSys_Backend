package de.femtopedia.powasysbackend.api;

import de.femtopedia.powasysbackend.util.Util;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DataEntry {

    public static int DATA_ENTRIES = 10;
    public static int FALLBACK = -1;
    public static double FALLBACK_D = -1D;

    private final int powadorId;
    private final String unused;
    private final String time;
    private final int state;
    private final double genVoltage;
    private final double genCurrent;
    private final int genPower;
    private final double netVoltage;
    private final double netCurrent;
    private final int netPower;
    private final int temperature;

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

    public void toStmt(int startIndex, PreparedStatement stmt) throws SQLException {
        stmt.setInt(startIndex, powadorId);
        stmt.setInt(++startIndex, state);
        stmt.setDouble(++startIndex, genVoltage);
        stmt.setDouble(++startIndex, genCurrent);
        stmt.setInt(++startIndex, genPower);
        stmt.setDouble(++startIndex, netVoltage);
        stmt.setDouble(++startIndex, netCurrent);
        stmt.setInt(++startIndex, netPower);
        stmt.setInt(++startIndex, temperature);
    }

}
