package model;
import interfaces.Identifiable;
import java.time.LocalDate;

public class Administrateur extends Personne implements Identifiable {
    private String departement;
    private String poste;

    public Administrateur(String id, String nom, String prenom, LocalDate dateNaissance, String departement, String poste) {
        super(id, nom, prenom, dateNaissance);
        this.departement = departement;
        this.poste = poste;
    }

    @Override
    public String getRole() { return "Admin"; }

    @Override
    public String getIdentiteComplete() {
        return getRole() + " " + getPrenom() + " " + getNom() + " (" + poste + ")";
    }

    public String getDepartement() { return departement; }
    
    @Override
    public String toString() {
        return super.toString() + " — " + departement + " / " + poste;
    }
}