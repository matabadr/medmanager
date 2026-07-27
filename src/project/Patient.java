package project;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Objects;


public class Patient {

    private String id;
    private String nom;
    private String prenom;
    private LocalDate dateNaissance;
    private String telephone;
    private String groupeSanguin;

    public Patient(String id, String nom, String prenom,
                   LocalDate dateNaissance, String telephone,
                   String groupeSanguin) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.dateNaissance = dateNaissance;
        this.telephone = telephone;
        if (groupeSanguin != null) {
            setGroupeSanguin(groupeSanguin);
        }
    }

    public Patient(String id, String nom, String prenom,
                   LocalDate dateNaissance) {
        this(id, nom, prenom, dateNaissance, null, null);
    }

   
    public int getAge() {
        return Period.between(dateNaissance, LocalDate.now()).getYears();
    }

    public String getIdentiteComplete() {
        return prenom + " " + nom + " (ID: " + id + ")";
    }


    public String getId()             { return id; }
    public String getNom()            { return nom; }
    public String getPrenom()         { return prenom; }
    public LocalDate getDateNaissance() { return dateNaissance; }
    public String getTelephone()      { return telephone; }
    public String getGroupeSanguin()  { return groupeSanguin; }


    public void setGroupeSanguin(String groupe) {
        List<String> valides = List.of(
            "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"
        );
        if (groupe == null || !valides.contains(groupe)) {
            throw new IllegalArgumentException(
                "Groupe sanguin invalide : " + groupe +
                ". Valeurs acceptées : A+, A-, B+, B-, AB+, AB-, O+, O-"
            );
        }
        this.groupeSanguin = groupe;
    }

  
    public void setTelephone(String telephone) {
        if (telephone == null || !telephone.matches("[+\\d\\s]{10,}")) {
            throw new IllegalArgumentException(
                "Numéro de téléphone invalide : " + telephone +
                ". Le numéro doit contenir au moins 10 caractères (chiffres, espaces, +)."
            );
        }
        this.telephone = telephone;
    }

    public void setNom(String nom)     { this.nom = nom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    public void setDateNaissance(LocalDate dateNaissance) {
        this.dateNaissance = dateNaissance;
    }

    // ── Contrats Object ──

    @Override
    public String toString() {
        return "Patient{"
            + "id='" + id + "'"
            + ", nom='" + nom + "'"
            + ", prenom='" + prenom + "'"
            + ", age=" + getAge()
            + ", groupeSanguin='" + (groupeSanguin != null ? groupeSanguin : "—") + "'"
            + "}";
    }

    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Patient autre = (Patient) obj;
        return id.equals(autre.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
