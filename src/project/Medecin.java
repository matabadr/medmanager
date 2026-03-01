package project;

import java.time.LocalDate;
import java.time.Period;
import java.util.Objects;


public class Medecin {

    // ── Attributs ──
    private String id;
    private String nom;
    private String prenom;
    private LocalDate dateNaissance;
    private String telephone;
    private String specialite;
    private String matricule;

    // ── Constructeur ──
    public Medecin(String id, String nom, String prenom,
                   LocalDate dateNaissance, String specialite,
                   String matricule) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.dateNaissance = dateNaissance;
        this.specialite = specialite;
        this.matricule = matricule;
    }

    // ── Méthodes ──

    /** Calcule l'âge à partir de la date de naissance. */
    public int getAge() {
        return Period.between(dateNaissance, LocalDate.now()).getYears();
    }

    // ── Getters ──

    public String getId()               { return id; }
    public String getNom()              { return nom; }
    public String getPrenom()           { return prenom; }
    public LocalDate getDateNaissance() { return dateNaissance; }
    public String getTelephone()        { return telephone; }
    public String getSpecialite()       { return specialite; }
    public String getMatricule()        { return matricule; }

    // ── Setters ──

    public void setTelephone(String telephone) { this.telephone = telephone; }
    public void setSpecialite(String specialite) { this.specialite = specialite; }

    // ── Contrats Object ──

    @Override
    public String toString() {
        return "Dr. " + prenom + " " + nom
            + " — " + specialite
            + " (Mat: " + matricule + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Medecin autre = (Medecin) obj;
        return id.equals(autre.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
