package model;

import java.time.LocalDate;
import java.time.Period;
import java.util.Objects; 

public abstract class Personne {
    private String id;
    private String nom;
    private String prenom;
    private LocalDate dateNaissance;
    private String telephone;

    public Personne(String id, String nom, String prenom, LocalDate dateNaissance) {
        
        Objects.requireNonNull(id, "L'ID ne peut pas être null");
        Objects.requireNonNull(nom, "Le nom ne peut pas être null");
        Objects.requireNonNull(prenom, "Le prénom ne peut pas être null");
        Objects.requireNonNull(dateNaissance, "La date de naissance ne peut pas être null");
        
        if (id.trim().isBlank())   throw new IllegalArgumentException("L'ID ne peut pas être vide");
        if (nom.trim().isBlank())  throw new IllegalArgumentException("Le nom ne peut pas être vide");
        if (prenom.trim().isBlank()) throw new IllegalArgumentException("Le prénom ne peut pas être vide");

        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.dateNaissance = dateNaissance;
    }

    public int getAge() {
        return Period.between(dateNaissance, LocalDate.now()).getYears();
    }

    public abstract String getRole();

    public String getId()     { return id; }
    public String getNom()    { return nom; }
    public String getPrenom() { return prenom; }

    public void setNom(String nom)                       { this.nom = nom; }
    public void setPrenom(String prenom)                 { this.prenom = prenom; }
    public void setDateNaissance(LocalDate dateNaissance){ this.dateNaissance = dateNaissance; }

    public String getTelephone()              { return telephone; }
    public void setTelephone(String telephone){ this.telephone = telephone; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Personne personne = (Personne) o;
        return id != null ? id.equals(personne.id) : personne.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return getRole() + " " + prenom + " " + nom;
    }
}