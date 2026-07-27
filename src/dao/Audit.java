package dao;

import main.Logger;
import model.Utilisateur;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Audit log writer. Designed to never throw — auditing failures must
 * not break business operations.
 *
 * The "current user" is supplied via a static accessor set by the UI
 * layer at login, so the DAO layer remains UI-agnostic.
 */
public final class Audit {

    private static Supplier<Utilisateur> currentUser = () -> null;

    public static void setCurrentUserSupplier(Supplier<Utilisateur> s) {
        if (s != null) currentUser = s;
    }

    public static void log(String action, String targetTable,
                           String targetId, String details) {
        Utilisateur u = currentUser.get();
        String username = u == null ? null : u.getUsername();
        String role = u == null ? null : u.getRole().name();
        String sql = "INSERT INTO audit_log (ts, username, role, action, target_table, target_id, details) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setString(2, username);
            pstmt.setString(3, role);
            pstmt.setString(4, action);
            pstmt.setString(5, targetTable);
            pstmt.setString(6, targetId);
            pstmt.setString(7, details);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            // Never fail the calling operation because of audit
            Logger.warn("Audit log echec : " + e.getMessage());
        }
    }

    public record Entry(long id, LocalDateTime ts, String username, String role,
                        String action, String targetTable, String targetId, String details) {}

    /** Read recent entries for a specific user (used by UserProfileView). */
    public static List<Entry> recentForUser(String username, int limit) {
        List<Entry> out = new ArrayList<>();
        String sql = "SELECT id, ts, username, role, action, target_table, target_id, details " +
                     "FROM audit_log WHERE username = ? ORDER BY ts DESC LIMIT ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setInt(2, limit);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Timestamp ts = rs.getTimestamp("ts");
                    out.add(new Entry(
                        rs.getLong("id"),
                        ts == null ? null : ts.toLocalDateTime(),
                        rs.getString("username"),
                        rs.getString("role"),
                        rs.getString("action"),
                        rs.getString("target_table"),
                        rs.getString("target_id"),
                        rs.getString("details")));
                }
            }
        } catch (SQLException e) {
            Logger.error("Lecture audit_log par utilisateur", e);
        }
        return out;
    }

    /** Count actions performed by a user, grouped by action. */
    public static java.util.Map<String, Integer> countActionsForUser(String username) {
        java.util.Map<String, Integer> out = new java.util.LinkedHashMap<>();
        String sql = "SELECT action, COUNT(*) FROM audit_log WHERE username = ? GROUP BY action";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) out.put(rs.getString(1), rs.getInt(2));
            }
        } catch (SQLException e) {
            Logger.error("Count actions par utilisateur", e);
        }
        return out;
    }

    /** Read recent entries — used by the AuditView. */
    public static List<Entry> recent(int limit) {
        List<Entry> out = new ArrayList<>();
        String sql = "SELECT id, ts, username, role, action, target_table, target_id, details " +
                     "FROM audit_log ORDER BY ts DESC LIMIT ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, limit);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    out.add(new Entry(
                        rs.getLong("id"),
                        rs.getTimestamp("ts").toLocalDateTime(),
                        rs.getString("username"),
                        rs.getString("role"),
                        rs.getString("action"),
                        rs.getString("target_table"),
                        rs.getString("target_id"),
                        rs.getString("details")));
                }
            }
        } catch (SQLException e) {
            Logger.error("Lecture audit_log", e);
        }
        return out;
    }
}
