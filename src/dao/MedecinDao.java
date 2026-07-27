package dao;

import enums.Specialite;
import main.Logger;
import model.Medecin;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MedecinDao {

    public String genererProchainId() {
        String sql = "SELECT id FROM medecin ORDER BY id DESC LIMIT 1";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                String lastId = rs.getString("id");
                int number = Integer.parseInt(lastId.substring(1));
                return String.format("M%03d", number + 1);
            }
        } catch (SQLException e) {
            Logger.error("Génération ID médecin", e);
        }
        return "M001";
    }

    public void ajouterMedecin(Medecin m) {
        String sql = "INSERT INTO medecin (id, nom, prenom, date_naissance, specialite, matricule, telephone, service_nom) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, m.getId());
            pstmt.setString(2, m.getNom());
            pstmt.setString(3, m.getPrenom());
            pstmt.setDate(4, Date.valueOf(m.getDateNaissance()));
            pstmt.setString(5, m.getSpecialite() == null ? null : m.getSpecialite().name());
            pstmt.setString(6, m.getMatricule());
            pstmt.setString(7, m.getTelephone());
            pstmt.setString(8, m.getServiceNom());

            pstmt.executeUpdate();
            Logger.info("Médecin " + m.getId() + " ajouté");
            Audit.log("CREATE", "medecin", m.getId(), m.getNom() + " " + m.getPrenom());

        } catch (SQLException e) {
            Logger.error("Ajout du médecin", e);
        }
    }

    public List<Medecin> listerMedecins() {
        List<Medecin> liste = new ArrayList<>();
        String sql = "SELECT * FROM medecin ORDER BY id";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                Specialite spe = null;
                String s = rs.getString("specialite");
                if (s != null) {
                    try { spe = Specialite.valueOf(s); } catch (Exception ignore) { /* tolerate */ }
                }
                Medecin m = new Medecin(
                    rs.getString("id"), rs.getString("nom"), rs.getString("prenom"),
                    rs.getDate("date_naissance").toLocalDate(),
                    spe,
                    rs.getString("matricule")
                );
                m.setTelephone(rs.getString("telephone"));
                m.setServiceNom(rs.getString("service_nom"));
                liste.add(m);
            }
        } catch (SQLException e) {
            Logger.error("Liste des médecins", e);
        }
        return liste;
    }

    public void modifierMedecin(Medecin m) {
        String sql = "UPDATE medecin SET nom=?, prenom=?, date_naissance=?, specialite=?, matricule=?, telephone=?, service_nom=? WHERE id=?";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, m.getNom());
            pstmt.setString(2, m.getPrenom());
            pstmt.setDate(3, Date.valueOf(m.getDateNaissance()));
            pstmt.setString(4, m.getSpecialite() == null ? null : m.getSpecialite().name());
            pstmt.setString(5, m.getMatricule());
            pstmt.setString(6, m.getTelephone());
            pstmt.setString(7, m.getServiceNom());
            pstmt.setString(8, m.getId());
            pstmt.executeUpdate();
            Logger.info("Médecin " + m.getId() + " modifié");
            Audit.log("UPDATE", "medecin", m.getId(), m.getNom() + " " + m.getPrenom());
        } catch (SQLException e) {
            Logger.error("Modification du médecin", e);
        }
    }

    public void supprimerMedecin(String id) {
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("DELETE FROM medecin WHERE id=?")) {
            pstmt.setString(1, id);
            pstmt.executeUpdate();
            Logger.info("Médecin " + id + " supprimé");
            Audit.log("DELETE", "medecin", id, null);
        } catch (SQLException e) {
            Logger.error("Suppression du médecin", e);
        }
    }

    public void affecterService(String idMedecin, String nomService) {
        String sql = "UPDATE medecin SET service_nom = ? WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nomService);
            pstmt.setString(2, idMedecin);
            pstmt.executeUpdate();
            Logger.info("Médecin " + idMedecin + " affecté à " + nomService);
            Audit.log("UPDATE", "medecin", idMedecin, "service: " + nomService);
        } catch (SQLException e) {
            Logger.error("Affectation médecin au service", e);
        }
    }
}
