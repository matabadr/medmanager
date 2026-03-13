package model;

import enums.TypeService;
import java.util.ArrayList;
import java.util.List;

public class ServiceHospitalier {
    private String nom;
    private TypeService type;
    private List<Patient> patients;
    private List<Medecin> medecins;

    public ServiceHospitalier(String nom, TypeService type) {
        this.nom = nom;
        this.type = type;
        this.patients = new ArrayList<>();
        this.medecins = new ArrayList<>();
    }

    public String getNom() { 
        return nom; 
    }

    public TypeService getType() { 
        return type; 
    }

    public boolean admettre(Patient p) {
        if (!patients.contains(p)) {
            patients.add(p);
            return true;
        }
        return false;
    }

    public void retirerPatient(Patient p) {
        patients.remove(p);
    }

    public void ajouterMedecin(Medecin m) {
        if (!medecins.contains(m)) {
            medecins.add(m);
        }
    }

    public void afficherTableauDeBord() {
        System.out.println("\n--- Service: " + nom + " (" + type + ") ---");
        System.out.println("Nombre de patients : " + patients.size());
        System.out.println("Nombre de médecins : " + medecins.size());
    }
}