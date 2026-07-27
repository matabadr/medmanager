package dao;

import main.Logger;
import model.Utilisateur;
import model.Utilisateur.Role;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

public class UtilisateurDao {

    /**
     * Verify credentials against the utilisateur table.
     * Returns the authenticated user on success; empty otherwise.
     * Updates derniere_connexion on success.
     */
    public Optional<Utilisateur> authenticate(String username, String password) {
        if (username == null || password == null
                || username.isBlank() || password.isBlank()) {
            return Optional.empty();
        }
        String sql = "SELECT username, password_hash, salt, role, nom_complet, actif, patient_id " +
                     "FROM utilisateur WHERE username = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                String storedHash = rs.getString("password_hash");
                String salt = rs.getString("salt");
                boolean actif = rs.getBoolean("actif");
                if (!actif) return Optional.empty();

                String candidate = hash(password, salt);
                if (!constantTimeEquals(candidate, storedHash)) return Optional.empty();

                Role role = Role.valueOf(rs.getString("role"));
                String nom = rs.getString("nom_complet");
                String patientId = rs.getString("patient_id");
                touchLastLogin(username);
                return Optional.of(new Utilisateur(username, role, nom, true,
                        LocalDateTime.now(), patientId));
            }
        } catch (SQLException e) {
            Logger.error("Authentification utilisateur", e);
            return Optional.empty();
        }
    }

    /** Lookup a user by username (without any password check). */
    public Optional<Utilisateur> findByUsername(String username) {
        String sql = "SELECT username, role, nom_complet, actif, derniere_connexion, patient_id " +
                     "FROM utilisateur WHERE username = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                Timestamp t = rs.getTimestamp("derniere_connexion");
                return Optional.of(new Utilisateur(
                        rs.getString("username"),
                        Role.valueOf(rs.getString("role")),
                        rs.getString("nom_complet"),
                        rs.getBoolean("actif"),
                        t == null ? null : t.toLocalDateTime(),
                        rs.getString("patient_id")));
            }
        } catch (SQLException e) {
            Logger.error("Lookup utilisateur", e);
            return Optional.empty();
        }
    }

    private void touchLastLogin(String username) {
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                     "UPDATE utilisateur SET derniere_connexion = ? WHERE username = ?")) {
            pstmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setString(2, username);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            // Non-critical
            Logger.warn("Impossible de mettre à jour derniere_connexion pour " + username);
        }
    }

    /* ------------------ password hashing ------------------ */

    /** Computes Base64(SHA-256(saltBytes || password)). */
    public static String hash(String password, String saltBase64) {
        try {
            byte[] salt = Base64.getDecoder().decode(saltBase64);
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            byte[] out = md.digest(password.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(out);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    public static String generateSalt() {
        byte[] s = new byte[12];
        new SecureRandom().nextBytes(s);
        return Base64.getEncoder().encodeToString(s);
    }

    /** Constant-time string compare to mitigate timing attacks. */
    private static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null || a.length() != b.length()) return false;
        int diff = 0;
        for (int i = 0; i < a.length(); i++) diff |= a.charAt(i) ^ b.charAt(i);
        return diff == 0;
    }
}
