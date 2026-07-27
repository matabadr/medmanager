package model;
import enums.Specialite;
import interfaces.Consultable;
import java.time.LocalDate;

public class Medecin extends Personne implements Consultable {
    private Specialite specialite;
    private String matricule;
    private String serviceNom;

    public Medecin(String id, String nom, String prenom, LocalDate dateNaissance, Specialite specialite, String matricule) {
        super(id, nom, prenom, dateNaissance);
        this.specialite = specialite;
        this.matricule = matricule;
    }

    @Override
    public String getRole() { return "Dr."; }

    public Specialite getSpecialite() { return specialite; }
    public String getMatricule() { return matricule; }

    public String getServiceNom() { return serviceNom; }
    public void setServiceNom(String serviceNom) { this.serviceNom = serviceNom; }

    @Override
    public boolean peutConsulter(Patient patient) {
        return true;
    }

    @Override
    public String toString() {
        return super.toString() + " — " + (specialite == null ? "?" : specialite.name());
    }
}
