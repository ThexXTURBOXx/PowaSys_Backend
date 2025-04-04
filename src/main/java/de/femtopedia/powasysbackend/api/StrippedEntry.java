package de.femtopedia.powasysbackend.api;

import de.femtopedia.database.api.StatementParametrizer;
import de.femtopedia.powasysbackend.util.Util;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public record StrippedEntry(int powadorId, double genVoltage, double genCurrent, double genPower, double netVoltage,
                            double netCurrent, double netPower, double temperature) {

    public static int DATA_ENTRIES = 7;
    public static double FALLBACK_D = -1D;

    public static StrippedEntry fromResultSet(ResultSet rs) throws SQLException {
        return new StrippedEntry(
                rs.getInt("powadorId"),
                rs.getDouble("genVoltage"),
                rs.getDouble("genCurrent"),
                rs.getDouble("genPower"),
                rs.getDouble("netVoltage"),
                rs.getDouble("netCurrent"),
                rs.getDouble("netPower"),
                rs.getDouble("temperature")
        );
    }

    public static StrippedEntry fromString(int powadorId, String data) {
        String[] split = data.split("\\s+");
        return fromStringData(powadorId, split);
    }

    public static StrippedEntry fromStringData(int powadorId, String[] data) {
        if (data.length != DATA_ENTRIES) {
            return null;
        }

        double genVoltage = Util.parseDouble(data[0]).orElse(FALLBACK_D);
        double genCurrent = Util.parseDouble(data[1]).orElse(FALLBACK_D);
        double genPower = Util.parseDouble(data[2]).orElse(FALLBACK_D);
        double netVoltage = Util.parseDouble(data[3]).orElse(FALLBACK_D);
        double netCurrent = Util.parseDouble(data[4]).orElse(FALLBACK_D);
        double netPower = Util.parseDouble(data[5]).orElse(FALLBACK_D);
        double temperature = Util.parseDouble(data[6]).orElse(FALLBACK_D);

        return new StrippedEntry(
                powadorId,
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
                .doublePrec(genVoltage)
                .doublePrec(genCurrent)
                .doublePrec(genPower)
                .doublePrec(netVoltage)
                .doublePrec(netCurrent)
                .doublePrec(netPower)
                .doublePrec(temperature);
    }

}
