package model;

import enums.TypeService;
import exceptions.DuplicatePersonneException;
import exceptions.PersonneNotFoundException;
import exceptions.ServiceCompletException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class ServiceHospitalier {
    private String nom;
    private TypeService type;
    private List<Patient> patients;
    private List<Medecin> medecins;
    private int capaciteLits;
    private Queue<Patient> fileAttente;

    public ServiceHospitalier(String nom, TypeService type, int capaciteLits) {
        this.nom = nom;
        this.type = type;
        this.capaciteLits = capaciteLits;
        this.patients = new ArrayList<>();
        this.medecins = new ArrayList<>();
        this.fileAttente = new ArrayDeque<>(); 
    }

    public String getNom() { return nom; }
    public TypeService getType() { return type; }
    public int getCapaciteLits() { return capaciteLits; }

    public void admettre(Patient patient) {
        if (patient == null)
            throw new IllegalArgumentException("Le patient ne peut pas être null");
        if (patients.contains(patient))
            throw new DuplicatePersonneException(patient.getId());
        if (patients.size() >= capaciteLits)
            throw new ServiceCompletException(nom, capaciteLits);

        patients.add(patient);
        patient.setServiceAdmission(nom);
    }

    public Patient trouverPatientOrFail(String id) {
        for (Patient p : patients) {
            if (p.getId().equals(id)) return p;
        }
        throw new PersonneNotFoundException(id, "Patient");
    }

    public int ajouterFileAttente(Patient patient) {
        if (!fileAttente.contains(patient)) {
            fileAttente.add(patient);
        }

        int position = 1;
        for (Patient p : fileAttente) {
            if (p.equals(patient)) return position;
            position++;
        }
        return fileAttente.size();
    }

    public int getTailleFileAttente() {
        return fileAttente.size();
    }

    public void retirerPatient(Patient p) { patients.remove(p); }

    public void ajouterMedecin(Medecin m) {
        if (!medecins.contains(m)) medecins.add(m);
    }

    public void afficherTableauDeBord() {
        System.out.println("\n--- Service: " + nom + " (" + type + ") ---");
        System.out.println("Patients : " + patients.size() + "/" + capaciteLits + " lits");
        System.out.println("Médecins : " + medecins.size());
        if (!fileAttente.isEmpty())
            System.out.println("File d'attente : " + fileAttente.size() + " patient(s)");
    }
}