package model;
import java.time.LocalDateTime;

public class RendezVous {
    private Patient patient;
    private Medecin medecin;
    private LocalDateTime dateHeure;

    public RendezVous(Patient patient, Medecin medecin, LocalDateTime dateHeure) {
        this.patient = patient;
        this.medecin = medecin;
        this.dateHeure = dateHeure;
    }

    @Override
    public String toString() {
        return "RDV le " + dateHeure + " : " + patient.getNom() + " avec " + medecin.getNom();
    }
}