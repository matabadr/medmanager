package dao;

import main.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Read-only aggregate queries used by the dashboard.
 * Single connection per call — small numbers, infrequent refresh.
 */
public final class Stats {

    private Stats() {}

    public static int countTable(String table) {
        String sql = "SELECT COUNT(*) FROM " + table;
        try (Connection c = Database.getConnection();
             PreparedStatement p = c.prepareStatement(sql);
             ResultSet rs = p.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            Logger.error("countTable " + table, e);
            return -1;
        }
    }

    public static int countUpcomingRdv() {
        String sql = "SELECT COUNT(*) FROM rendez_vous WHERE date_heure >= NOW()";
        try (Connection c = Database.getConnection();
             PreparedStatement p = c.prepareStatement(sql);
             ResultSet rs = p.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            Logger.error("countUpcomingRdv", e);
            return -1;
        }
    }

    public static int countPatientsWithService() {
        String sql = "SELECT COUNT(*) FROM patient WHERE service_nom IS NOT NULL";
        try (Connection c = Database.getConnection();
             PreparedStatement p = c.prepareStatement(sql);
             ResultSet rs = p.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            Logger.error("countPatientsWithService", e);
            return -1;
        }
    }

    /** Patient counts grouped by blood group, ordered by canonical sequence. */
    public static Map<String, Integer> patientsByBloodGroup() {
        String sql = "SELECT groupe_sanguin, COUNT(*) FROM patient " +
                     "WHERE groupe_sanguin IS NOT NULL GROUP BY groupe_sanguin";
        Map<String, Integer> out = new LinkedHashMap<>();
        // Pre-seed canonical order so the pie has consistent colors
        for (String g : new String[]{"A+","A-","B+","B-","AB+","AB-","O+","O-"}) out.put(g, 0);
        try (Connection c = Database.getConnection();
             PreparedStatement p = c.prepareStatement(sql);
             ResultSet rs = p.executeQuery()) {
            while (rs.next()) out.put(rs.getString(1), rs.getInt(2));
        } catch (SQLException e) {
            Logger.error("patientsByBloodGroup", e);
        }
        return out;
    }

    /** Medecin counts per spécialité. */
    public static Map<String, Integer> medecinsBySpecialite() {
        String sql = "SELECT specialite, COUNT(*) FROM medecin GROUP BY specialite";
        Map<String, Integer> out = new LinkedHashMap<>();
        try (Connection c = Database.getConnection();
             PreparedStatement p = c.prepareStatement(sql);
             ResultSet rs = p.executeQuery()) {
            while (rs.next()) out.put(rs.getString(1), rs.getInt(2));
        } catch (SQLException e) {
            Logger.error("medecinsBySpecialite", e);
        }
        return out;
    }

    /** Patient counts per service (NULL excluded). */
    public static Map<String, Integer> patientsByService() {
        String sql = "SELECT service_nom, COUNT(*) FROM patient " +
                     "WHERE service_nom IS NOT NULL GROUP BY service_nom ORDER BY 1";
        Map<String, Integer> out = new LinkedHashMap<>();
        try (Connection c = Database.getConnection();
             PreparedStatement p = c.prepareStatement(sql);
             ResultSet rs = p.executeQuery()) {
            while (rs.next()) out.put(rs.getString(1), rs.getInt(2));
        } catch (SQLException e) {
            Logger.error("patientsByService", e);
        }
        return out;
    }

    /** Lightweight liveness probe — returns ms or -1 on failure. */
    public static long ping() {
        long t0 = System.currentTimeMillis();
        try (Connection c = Database.getConnection();
             PreparedStatement p = c.prepareStatement("SELECT 1");
             ResultSet rs = p.executeQuery()) {
            rs.next();
            return System.currentTimeMillis() - t0;
        } catch (SQLException e) {
            return -1;
        }
    }
}
