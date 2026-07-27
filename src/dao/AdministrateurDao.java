package dao;

import main.Logger;
import model.Administrateur;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AdministrateurDao {

    public String genererProchainId() {
        String sql = "SELECT id FROM administrateur ORDER BY id DESC LIMIT 1";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                String lastId = rs.getString("id");
                int n = Integer.parseInt(lastId.substring(1));
                return String.format("A%03d", n + 1);
            }
        } catch (SQLException e) {
            Logger.error("Génération ID administrateur", e);
        }
        return "A001";
    }

    public void ajouter(Administrateur a) {
        String sql = "INSERT INTO administrateur (id, nom, prenom, date_naissance, departement, poste, telephone) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, a.getId());
            pstmt.setString(2, a.getNom());
            pstmt.setString(3, a.getPrenom());
            pstmt.setDate(4, Date.valueOf(a.getDateNaissance()));
            pstmt.setString(5, a.getDepartement());
            pstmt.setString(6, a.getPoste());
            pstmt.setString(7, a.getTelephone());
            pstmt.executeUpdate();
            Logger.info("Administrateur " + a.getId() + " ajouté");
            Audit.log("CREATE", "administrateur", a.getId(), a.getNom() + " " + a.getPrenom());
        } catch (SQLException e) {
            Logger.error("Ajout de l'administrateur", e);
        }
    }

    public List<Administrateur> lister() {
        List<Administrateur> liste = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM administrateur ORDER BY id");
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                Administrateur a = new Administrateur(
                    rs.getString("id"), rs.getString("nom"), rs.getString("prenom"),
                    rs.getDate("date_naissance").toLocalDate(),
                    rs.getString("departement"), rs.getString("poste"));
                a.setTelephone(rs.getString("telephone"));
                liste.add(a);
            }
        } catch (SQLException e) {
            Logger.error("Liste des administrateurs", e);
        }
        return liste;
    }

    public void modifier(Administrateur a) {
        String sql = "UPDATE administrateur SET nom=?, prenom=?, date_naissance=?, departement=?, poste=?, telephone=? WHERE id=?";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, a.getNom());
            pstmt.setString(2, a.getPrenom());
            pstmt.setDate(3, Date.valueOf(a.getDateNaissance()));
            pstmt.setString(4, a.getDepartement());
            pstmt.setString(5, a.getPoste());
            pstmt.setString(6, a.getTelephone());
            pstmt.setString(7, a.getId());
            pstmt.executeUpdate();
            Logger.info("Administrateur " + a.getId() + " modifié");
            Audit.log("UPDATE", "administrateur", a.getId(), a.getNom() + " " + a.getPrenom());
        } catch (SQLException e) {
            Logger.error("Modification de l'administrateur", e);
        }
    }

    public void supprimer(String id) {
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("DELETE FROM administrateur WHERE id=?")) {
            pstmt.setString(1, id);
            pstmt.executeUpdate();
            Logger.info("Administrateur " + id + " supprimé");
            Audit.log("DELETE", "administrateur", id, null);
        } catch (SQLException e) {
            Logger.error("Suppression de l'administrateur", e);
        }
    }
}
