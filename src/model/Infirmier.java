package model;
import interfaces.Assignable;
import interfaces.Identifiable;
import interfaces.Consultable;
import java.time.LocalDate;

public class Infirmier extends Personne implements Identifiable, Assignable, Consultable {
    private String numeroDiplome;
    private String service;

    public Infirmier(String id, String nom, String prenom, LocalDate dateNaissance, String numeroDiplome, String service) {
        super(id, nom, prenom, dateNaissance);
        this.numeroDiplome = numeroDiplome;
        this.service = service;
    }

    @Override public String getRole() { return "Inf."; }

    @Override
    public String getIdentiteComplete() {
        return getRole() + " " + getPrenom() + " " + getNom();
    }

    @Override public boolean estDisponible() { return true; }
    @Override public void assigner(String svc) { this.service = svc; }

    public String getSpecialite() { return service; }
    public String getNumeroDiplome() { return numeroDiplome; }

    public void administrerMedicament(String nomMedicament) {
        administrerMedicament(nomMedicament, 500.0);
    }

    public void administrerMedicament(String nomMedicament, double doseMg) {
        System.out.println("Administration de " + doseMg + "mg de " + nomMedicament);
    }

    @Override
    public boolean peutConsulter(Patient patient) {
        return patient.getServiceAdmission() != null && patient.getServiceAdmission().equalsIgnoreCase(this.service);
    }

    @Override
    public String toString() {
        return super.toString() + " — " + service;
    }
}