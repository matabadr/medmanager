package model;
import java.time.LocalDate;

public class Technicien extends Personne {
    private String numeroLaboratoire;

    public Technicien(String id, String nom, String prenom, LocalDate dateNaissance, String numeroLaboratoire) {
        super(id, nom, prenom, dateNaissance);
        this.numeroLaboratoire = numeroLaboratoire;
    }

    @Override
    public String getRole() { return "Technicien de labo"; }

    @Override
    public String toString() {
        return super.toString() + " — Labo n°" + numeroLaboratoire;
    }
}