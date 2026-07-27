package dao;

import main.Logger;
import model.Patient;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PatientDao {

    public String genererProchainId() {
        String sql = "SELECT id FROM patient ORDER BY id DESC LIMIT 1";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                String lastId = rs.getString("id"); // ex: "P005"
                int number = Integer.parseInt(lastId.substring(1));
                return String.format("P%03d", number + 1);
            }
        } catch (SQLException e) {
            Logger.error("Génération ID patient", e);
        }
        return "P001";
    }

    public java.util.Optional<Patient> findById(String id) {
        String sql = "SELECT * FROM patient WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (!rs.next()) return java.util.Optional.empty();
                Patient p = new Patient(rs.getString("id"), rs.getString("nom"),
                        rs.getString("prenom"), rs.getDate("date_naissance").toLocalDate());
                p.setTelephone(rs.getString("telephone"));
                String gs = rs.getString("groupe_sanguin");
                if (gs != null) {
                    try { p.setGroupeSanguin(enums.GroupeSanguin.fromLabel(gs)); }
                    catch (Exception ignore) {}
                }
                p.setServiceAdmission(rs.getString("service_nom"));
                return java.util.Optional.of(p);
            }
        } catch (SQLException e) {
            Logger.error("Lookup patient " + id, e);
            return java.util.Optional.empty();
        }
    }

    public void ajouterPatient(Patient p) {
        String sql = "INSERT INTO patient (id, nom, prenom, date_naissance, groupe_sanguin, telephone, service_nom) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, p.getId());
            pstmt.setString(2, p.getNom());
            pstmt.setString(3, p.getPrenom());
            pstmt.setDate(4, Date.valueOf(p.getDateNaissance()));

            if (p.getGroupeSanguin() != null) {
                pstmt.setString(5, p.getGroupeSanguin().getLabel());
            } else {
                pstmt.setNull(5, java.sql.Types.VARCHAR);
            }

            pstmt.setString(6, p.getTelephone());

            if (p.getServiceAdmission() != null) {
                pstmt.setString(7, p.getServiceAdmission());
            } else {
                pstmt.setNull(7, java.sql.Types.VARCHAR);
            }

            pstmt.executeUpdate();
            Logger.info("Patient " + p.getId() + " ajouté");
            Audit.log("CREATE", "patient", p.getId(), p.getNom() + " " + p.getPrenom());

        } catch (SQLException e) {
            Logger.error("Ajout du patient", e);
        }
    }

    public List<Patient> listerPatients() {
        List<Patient> liste = new ArrayList<>();
        String sql = "SELECT * FROM patient ORDER BY id";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                Patient p = new Patient(rs.getString("id"), rs.getString("nom"), rs.getString("prenom"),
                                        rs.getDate("date_naissance").toLocalDate());
                p.setTelephone(rs.getString("telephone"));
                String gs = rs.getString("groupe_sanguin");
                if (gs != null) {
                    try { p.setGroupeSanguin(enums.GroupeSanguin.fromLabel(gs)); }
                    catch (Exception ignore) { /* tolerate legacy bad rows */ }
                }
                p.setServiceAdmission(rs.getString("service_nom"));
                liste.add(p);
            }
        } catch (SQLException e) {
            Logger.error("Liste des patients", e);
        }
        return liste;
    }

    public void modifierPatient(Patient p) {
        String sql = "UPDATE patient SET nom=?, prenom=?, date_naissance=?, groupe_sanguin=?, telephone=?, service_nom=? WHERE id=?";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, p.getNom());
            pstmt.setString(2, p.getPrenom());
            pstmt.setDate(3, Date.valueOf(p.getDateNaissance()));
            if (p.getGroupeSanguin() != null) pstmt.setString(4, p.getGroupeSanguin().getLabel());
            else pstmt.setNull(4, java.sql.Types.VARCHAR);
            pstmt.setString(5, p.getTelephone());
            if (p.getServiceAdmission() != null) pstmt.setString(6, p.getServiceAdmission());
            else pstmt.setNull(6, java.sql.Types.VARCHAR);
            pstmt.setString(7, p.getId());
            pstmt.executeUpdate();
            Logger.info("Patient " + p.getId() + " modifié");
            Audit.log("UPDATE", "patient", p.getId(), p.getNom() + " " + p.getPrenom());
        } catch (SQLException e) {
            Logger.error("Modification du patient", e);
        }
    }

    public void supprimerPatient(String id) {
        String sql = "DELETE FROM patient WHERE id=?";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.executeUpdate();
            Logger.info("Patient " + id + " supprimé");
            Audit.log("DELETE", "patient", id, null);
        } catch (SQLException e) {
            Logger.error("Suppression du patient", e);
        }
    }

    public void affecterService(String idPatient, String nomService) {
        String sql = "UPDATE patient SET service_nom = ? WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nomService);
            pstmt.setString(2, idPatient);
            pstmt.executeUpdate();
            Logger.info("Patient " + idPatient + " affecté à " + nomService);
            Audit.log("UPDATE", "patient", idPatient, "service: " + nomService);
        } catch (SQLException e) {
            Logger.error("Affectation patient au service", e);
        }
    }
}
