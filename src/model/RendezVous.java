package model;

import java.time.LocalDateTime;

public class RendezVous {
    private String id; 
    private Patient patient;
    private Medecin medecin;
    private LocalDateTime dateHeure;

    public RendezVous(String id, Patient patient, Medecin medecin, LocalDateTime dateHeure) {
        this.id = id;
        this.patient = patient;
        this.medecin = medecin;
        this.dateHeure = dateHeure;
    }

    public String getId() { return id; }
    public Patient getPatient() { return patient; }
    public Medecin getMedecin() { return medecin; }
    public LocalDateTime getDateHeure() { return dateHeure; }
    
    // For TableView display
    public String getNomPatient() { return patient != null ? patient.getNom() + " " + patient.getPrenom() : ""; }
    public String getNomMedecin() { return medecin != null ? "Dr. " + medecin.getNom() : ""; }
}