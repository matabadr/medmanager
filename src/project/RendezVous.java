package project;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Représente un rendez-vous entre un Patient et un Medecin.
 * Exercice 6 (Bonus) — Chapitre 2.
 *
 * Un RendezVous est une ASSOCIATION :
 *   - Il référence un Patient et un Medecin sans les posséder.
 *   - Le patient et le médecin existent indépendamment du rendez-vous.
 *   - Si on supprime le RendezVous, Patient et Medecin restent intacts.
 *
 * C'est différent de la COMPOSITION dans ServiceHospitalier
 * où le service "possède" ses listes (elles n'ont pas d'existence
 * indépendante du service).
 */
public class RendezVous {

    private static final DateTimeFormatter FORMATTER =
        DateTimeFormatter.ofPattern("dd/MM/yyyy 'à' HH:mm");

    // ── Attributs ──
    private String id;
    private Patient patient;      // Association : référence externe
    private Medecin medecin;      // Association : référence externe
    private LocalDateTime dateHeure;
    private String motif;

    // ── Constructeur ──
    public RendezVous(String id, Patient patient, Medecin medecin,
                      LocalDateTime dateHeure, String motif) {
        if (patient == null) throw new IllegalArgumentException("Le patient ne peut pas être null.");
        if (medecin == null) throw new IllegalArgumentException("Le médecin ne peut pas être null.");
        if (dateHeure == null) throw new IllegalArgumentException("La date/heure ne peut pas être null.");
        this.id       = id;
        this.patient  = patient;
        this.medecin  = medecin;
        this.dateHeure = dateHeure;
        this.motif    = motif;
    }

    // ── Getters ──
    public String getId()             { return id; }
    public Patient getPatient()       { return patient; }
    public Medecin getMedecin()       { return medecin; }
    public LocalDateTime getDateHeure() { return dateHeure; }
    public String getMotif()          { return motif; }

    // ── Contrats Object ──

    @Override
    public String toString() {
        return "RDV[" + id + "] "
            + patient.getIdentiteComplete()
            + " ↔ " + medecin
            + " | " + dateHeure.format(FORMATTER)
            + (motif != null ? " | Motif : " + motif : "");
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        RendezVous autre = (RendezVous) obj;
        return id.equals(autre.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
