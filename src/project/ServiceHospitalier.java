package project;

import java.util.ArrayList;
import java.util.List;


public class ServiceHospitalier {

    // ── Attributs ──
    private String code;
    private String nom;
    private int capaciteLits;
    private List<Medecin> medecins;
    private List<Patient> patients;

    // ── Constructeur ──
    public ServiceHospitalier(String code, String nom, int capaciteLits) {
        this.code = code;
        this.nom = nom;
        this.capaciteLits = capaciteLits;
        this.medecins = new ArrayList<>();
        this.patients = new ArrayList<>();
    }

  
    public void ajouterMedecin(Medecin medecin) {
        if (medecin == null) {
            System.out.println("⚠ Médecin invalide.");
            return;
        }
        medecins.add(medecin);
    }

    public boolean admettre(Patient patient) {
        if (patients.size() >= capaciteLits) {
            System.out.println("⚠ Service " + nom
                + " complet (" + capaciteLits + "/" + capaciteLits + " lits)");
            return false;
        }
        patients.add(patient);
        return true;
    }

    public boolean retirerPatient(Patient patient) {
        return patients.remove(patient);
    }

    public Patient trouverPatient(String id) {
        for (Patient p : patients) {
            if (p.getId().equals(id)) {
                return p;
            }
        }
        return null; // Sera amélioré avec Optional au chapitre Collections
    }

    public int getLitsDisponibles() {
        return capaciteLits - patients.size();
    }

    public void afficherTableauDeBord() {
        System.out.println("\n╔══════════════════════════════════════╗");
        System.out.printf("║  🏥 %-20s  [%s]       ║%n", nom, code);
        System.out.printf("║  🛏 Lits : %d/%d disponibles       ║%n",
            getLitsDisponibles(), capaciteLits);
        System.out.println("╠══════════════════════════════════════╣");

        System.out.println("║  👨‍⚕ Médecins :");
        for (Medecin m : medecins) {
            System.out.println("║    → " + m);
        }
        if (medecins.isEmpty())
            System.out.println("║    (aucun)");

        System.out.println("║  👤 Patients :");
        for (Patient p : patients) {
            System.out.printf("║    → %-20s %d ans%n",
                p.getIdentiteComplete(), p.getAge());
        }
        if (patients.isEmpty())
            System.out.println("║    (aucun)");

        System.out.println("╚══════════════════════════════════════╝");
    }
    
    public String getNom()            { return nom; }
    public String getCode()           { return code; }
    public int getCapaciteLits()      { return capaciteLits; }
    public List<Medecin> getMedecins() { return medecins; }
    public List<Patient> getPatients() { return patients; }

    
    @Override
    public String toString() {
        return nom + " [" + getLitsDisponibles()
            + "/" + capaciteLits + " lits disponibles]";
    }
}
