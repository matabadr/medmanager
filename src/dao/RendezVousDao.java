package dao;

import enums.Specialite;
import main.Logger;
import model.Medecin;
import model.Patient;
import model.RendezVous;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class RendezVousDao {

    public String genererProchainId() {
        String sql = "SELECT id FROM rendez_vous ORDER BY id DESC LIMIT 1";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                String lastId = rs.getString("id"); // ex: "RDV0007"
                int n = Integer.parseInt(lastId.substring(3));
                return String.format("RDV%04d", n + 1);
            }
        } catch (SQLException e) {
            Logger.error("Génération ID rendez-vous", e);
        }
        return "RDV0001";
    }

    public String ajouter(String patientId, String medecinId, Timestamp dateHeure) {
        String id = genererProchainId();
        String sql = "INSERT INTO rendez_vous (id, patient_id, medecin_id, date_heure) VALUES (?, ?, ?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.setString(2, patientId);
            pstmt.setString(3, medecinId);
            pstmt.setTimestamp(4, dateHeure);
            pstmt.executeUpdate();
            Logger.info("Rendez-vous " + id + " planifié");
            Audit.log("CREATE", "rendez_vous", id, "patient=" + patientId + ", medecin=" + medecinId);
            return id;
        } catch (SQLException e) {
            Logger.error("Ajout rendez-vous", e);
            return null;
        }
    }

    public void supprimer(String id) {
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("DELETE FROM rendez_vous WHERE id = ?")) {
            pstmt.setString(1, id);
            pstmt.executeUpdate();
            Audit.log("DELETE", "rendez_vous", id, null);
        } catch (SQLException e) {
            Logger.error("Suppression rendez-vous " + id, e);
        }
    }

    public void replanifier(String id, Timestamp nouvelleDate) {
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("UPDATE rendez_vous SET date_heure = ? WHERE id = ?")) {
            pstmt.setTimestamp(1, nouvelleDate);
            pstmt.setString(2, id);
            pstmt.executeUpdate();
            Audit.log("UPDATE", "rendez_vous", id, "date=" + nouvelleDate);
        } catch (SQLException e) {
            Logger.error("Replanification rendez-vous " + id, e);
        }
    }

    /** Next N upcoming appointments, full join. */
    public List<RendezVous> listerProchains(int limit) {
        List<RendezVous> liste = new ArrayList<>();
        String sql =
            "SELECT rv.id AS rv_id, rv.date_heure, " +
            "       p.id AS p_id, p.nom AS p_nom, p.prenom AS p_prenom, p.date_naissance AS p_dn, " +
            "       p.groupe_sanguin AS p_gs, p.telephone AS p_tel, " +
            "       m.id AS m_id, m.nom AS m_nom, m.prenom AS m_prenom, m.date_naissance AS m_dn, " +
            "       m.specialite AS m_spe, m.matricule AS m_mat " +
            "FROM rendez_vous rv " +
            "LEFT JOIN patient p ON p.id = rv.patient_id " +
            "LEFT JOIN medecin m ON m.id = rv.medecin_id " +
            "WHERE rv.date_heure >= NOW() " +
            "ORDER BY rv.date_heure ASC LIMIT ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, limit);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Patient p = null;
                    String pid = rs.getString("p_id");
                    if (pid != null) {
                        p = new Patient(pid, rs.getString("p_nom"), rs.getString("p_prenom"),
                                        rs.getDate("p_dn").toLocalDate());
                        p.setTelephone(rs.getString("p_tel"));
                        String gs = rs.getString("p_gs");
                        if (gs != null) {
                            try { p.setGroupeSanguin(enums.GroupeSanguin.fromLabel(gs)); }
                            catch (Exception ignore) {}
                        }
                    }
                    Medecin m = null;
                    String mid = rs.getString("m_id");
                    if (mid != null) {
                        Specialite spe = null;
                        String s = rs.getString("m_spe");
                        if (s != null) { try { spe = Specialite.valueOf(s); } catch (Exception ignore) {} }
                        m = new Medecin(mid, rs.getString("m_nom"), rs.getString("m_prenom"),
                                        rs.getDate("m_dn").toLocalDate(), spe, rs.getString("m_mat"));
                    }
                    Timestamp ts = rs.getTimestamp("date_heure");
                    liste.add(new RendezVous(rs.getString("rv_id"), p, m,
                                             ts == null ? null : ts.toLocalDateTime()));
                }
            }
        } catch (SQLException e) {
            Logger.error("Liste prochains rendez-vous", e);
        }
        return liste;
    }

    /** Visit counts per day-of-week for the current week. */
    public java.util.Map<java.time.DayOfWeek, Integer> visitesParJourSemaine(java.time.LocalDate weekStart) {
        java.util.Map<java.time.DayOfWeek, Integer> out = new java.util.EnumMap<>(java.time.DayOfWeek.class);
        for (java.time.DayOfWeek d : java.time.DayOfWeek.values()) out.put(d, 0);
        String sql = "SELECT EXTRACT(ISODOW FROM date_heure)::int AS dow, COUNT(*) " +
                     "FROM rendez_vous WHERE date_heure >= ? AND date_heure < ? GROUP BY dow";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setTimestamp(1, Timestamp.valueOf(weekStart.atStartOfDay()));
            pstmt.setTimestamp(2, Timestamp.valueOf(weekStart.plusDays(7).atStartOfDay()));
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int dow = rs.getInt(1);
                    out.put(java.time.DayOfWeek.of(dow), rs.getInt(2));
                }
            }
        } catch (SQLException e) {
            Logger.error("Visites par jour semaine", e);
        }
        return out;
    }

    /** RDV between two timestamps inclusive of start, exclusive of end. */
    public List<RendezVous> entre(java.time.LocalDateTime start, java.time.LocalDateTime end,
                                  PatientDao pDao, MedecinDao mDao) {
        List<RendezVous> liste = new ArrayList<>();
        String sql =
            "SELECT rv.id AS rv_id, rv.date_heure, " +
            "       p.id AS p_id, p.nom AS p_nom, p.prenom AS p_prenom, p.date_naissance AS p_dn, " +
            "       p.groupe_sanguin AS p_gs, p.telephone AS p_tel, " +
            "       m.id AS m_id, m.nom AS m_nom, m.prenom AS m_prenom, m.date_naissance AS m_dn, " +
            "       m.specialite AS m_spe, m.matricule AS m_mat " +
            "FROM rendez_vous rv " +
            "LEFT JOIN patient p ON p.id = rv.patient_id " +
            "LEFT JOIN medecin m ON m.id = rv.medecin_id " +
            "WHERE rv.date_heure >= ? AND rv.date_heure < ? " +
            "ORDER BY rv.date_heure";

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setTimestamp(1, Timestamp.valueOf(start));
            pstmt.setTimestamp(2, Timestamp.valueOf(end));
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Patient p = null;
                    String pid = rs.getString("p_id");
                    if (pid != null) {
                        p = new Patient(pid, rs.getString("p_nom"), rs.getString("p_prenom"),
                                        rs.getDate("p_dn").toLocalDate());
                        p.setTelephone(rs.getString("p_tel"));
                        String gs = rs.getString("p_gs");
                        if (gs != null) {
                            try { p.setGroupeSanguin(enums.GroupeSanguin.fromLabel(gs)); }
                            catch (Exception ignore) {}
                        }
                    }
                    Medecin m = null;
                    String mid = rs.getString("m_id");
                    if (mid != null) {
                        Specialite spe = null;
                        String s = rs.getString("m_spe");
                        if (s != null) {
                            try { spe = Specialite.valueOf(s); } catch (Exception ignore) {}
                        }
                        m = new Medecin(mid, rs.getString("m_nom"), rs.getString("m_prenom"),
                                        rs.getDate("m_dn").toLocalDate(), spe, rs.getString("m_mat"));
                    }
                    Timestamp ts = rs.getTimestamp("date_heure");
                    liste.add(new RendezVous(rs.getString("rv_id"), p, m,
                                             ts == null ? null : ts.toLocalDateTime()));
                }
            }
        } catch (SQLException e) {
            Logger.error("Liste RDV par plage", e);
        }
        return liste;
    }

    /** Patient RDV history. */
    public List<RendezVous> listerPourPatient(String patientId, MedecinDao mDao) {
        List<RendezVous> liste = new ArrayList<>();
        String sql =
            "SELECT rv.id AS rv_id, rv.date_heure, " +
            "       m.id AS m_id, m.nom AS m_nom, m.prenom AS m_prenom, m.date_naissance AS m_dn, " +
            "       m.specialite AS m_spe, m.matricule AS m_mat " +
            "FROM rendez_vous rv " +
            "LEFT JOIN medecin m ON m.id = rv.medecin_id " +
            "WHERE rv.patient_id = ? " +
            "ORDER BY rv.date_heure DESC";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, patientId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Medecin m = null;
                    String mid = rs.getString("m_id");
                    if (mid != null) {
                        Specialite spe = null;
                        String s = rs.getString("m_spe");
                        if (s != null) { try { spe = Specialite.valueOf(s); } catch (Exception ignore) {} }
                        m = new Medecin(mid, rs.getString("m_nom"), rs.getString("m_prenom"),
                                        rs.getDate("m_dn").toLocalDate(), spe, rs.getString("m_mat"));
                    }
                    Timestamp ts = rs.getTimestamp("date_heure");
                    liste.add(new RendezVous(rs.getString("rv_id"), null, m,
                                             ts == null ? null : ts.toLocalDateTime()));
                }
            }
        } catch (SQLException e) {
            Logger.error("Liste RDV patient", e);
        }
        return liste;
    }

    /**
     * Lists all appointments with a single JOIN, hydrating Patient + Medecin
     * inline. The pDao/mDao parameters are kept for API compatibility but
     * are no longer used to fetch full lists.
     */
    public List<RendezVous> lister(PatientDao pDao, MedecinDao mDao) {
        List<RendezVous> liste = new ArrayList<>();
        String sql =
            "SELECT rv.id AS rv_id, rv.date_heure, " +
            "       p.id AS p_id, p.nom AS p_nom, p.prenom AS p_prenom, " +
            "       p.date_naissance AS p_dn, p.groupe_sanguin AS p_gs, p.telephone AS p_tel, " +
            "       m.id AS m_id, m.nom AS m_nom, m.prenom AS m_prenom, " +
            "       m.date_naissance AS m_dn, m.specialite AS m_spe, m.matricule AS m_mat " +
            "FROM rendez_vous rv " +
            "LEFT JOIN patient p ON p.id = rv.patient_id " +
            "LEFT JOIN medecin m ON m.id = rv.medecin_id " +
            "ORDER BY rv.date_heure DESC";

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                Patient p = null;
                String pid = rs.getString("p_id");
                if (pid != null) {
                    p = new Patient(pid, rs.getString("p_nom"), rs.getString("p_prenom"),
                                    rs.getDate("p_dn").toLocalDate());
                    p.setTelephone(rs.getString("p_tel"));
                    String gs = rs.getString("p_gs");
                    if (gs != null) {
                        try { p.setGroupeSanguin(enums.GroupeSanguin.fromLabel(gs)); }
                        catch (Exception ignore) { /* tolerate bad enum data */ }
                    }
                }

                Medecin m = null;
                String mid = rs.getString("m_id");
                if (mid != null) {
                    Specialite spe = null;
                    String s = rs.getString("m_spe");
                    if (s != null) {
                        try { spe = Specialite.valueOf(s); } catch (Exception ignore) { }
                    }
                    m = new Medecin(mid, rs.getString("m_nom"), rs.getString("m_prenom"),
                                    rs.getDate("m_dn").toLocalDate(), spe, rs.getString("m_mat"));
                }

                Timestamp ts = rs.getTimestamp("date_heure");
                liste.add(new RendezVous(rs.getString("rv_id"), p, m,
                                         ts == null ? null : ts.toLocalDateTime()));
            }
        } catch (SQLException e) {
            Logger.error("Liste des rendez-vous", e);
        }
        return liste;
    }
}
