package main;

import model.*;
import enums.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {

    static List<Patient> patients = new ArrayList<>();
    static List<Medecin> medecins = new ArrayList<>();
    static List<ServiceHospitalier> services = new ArrayList<>();
    static List<RendezVous> rendezVousList = new ArrayList<>();
    static int prochainIdPatient = 1;
    static int prochainIdMedecin = 1;

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        services.add(new ServiceHospitalier("Cardiologie", TypeService.CLINIQUE));
        services.add(new ServiceHospitalier("Urgences", TypeService.CLINIQUE));
        services.add(new ServiceHospitalier("Pédiatrie", TypeService.CLINIQUE));

        int choix;
        do {
            afficherMenu();
            choix = lireEntier(sc);
            switch (choix) {
                case 1 -> ajouterPatient(sc);
                case 2 -> ajouterMedecin(sc);
                case 3 -> afficherTousLesPatients();
                case 4 -> afficherTousLesMedecins();
                case 5 -> affecterPatientAuService(sc);
                case 6 -> tableauDeBordServices();
                case 7 -> supprimerPatient(sc);
                case 8 -> modifierPatient(sc);
                case 9 -> affecterMedecinAuService(sc);
                case 10 -> planifierRendezVous(sc);
                case 11 -> testerEquals();
                case 0 -> System.out.println("\n👋 Fermeture de MedManager.");
                default -> System.out.println("⚠ Choix invalide.");
            }
        } while (choix != 0);

        sc.close();
    }

    static void afficherMenu() {
        System.out.println("\n══════ MedManager v1.1 ══════");
        System.out.println("  1. ➕ Ajouter un patient");
        System.out.println("  2. ➕ Ajouter un médecin");
        System.out.println("  3. 📋 Afficher les patients");
        System.out.println("  4. 📋 Afficher les médecins");
        System.out.println("  5. 🏥 Affecter patient → service");
        System.out.println("  6. 📊 Tableau de bord des services");
        System.out.println("  7. 🗑 Supprimer un patient");
        System.out.println("  8. ✏️ Modifier un patient");
        System.out.println("  9. 🏥 Affecter médecin → service");
        System.out.println("  10. 📅 Planifier un rendez-vous");
        System.out.println("  11. 🧪 Tester equals et hashCode");
        System.out.println("  0. 🚪 Quitter");
        System.out.print("Votre choix : ");
    }

    static int lireEntier(Scanner sc) {
        while (!sc.hasNextInt()) {
            System.out.print("⚠ Nombre attendu : ");
            sc.next();
        }
        int val = sc.nextInt();
        sc.nextLine();
        return val;
    }

    static void ajouterPatient(Scanner sc) {
        System.out.println("\n--- Nouveau Patient ---");
        String id = String.format("P%03d", prochainIdPatient++);

        System.out.print("Nom : ");
        String nom = sc.nextLine();
        System.out.print("Prénom : ");
        String prenom = sc.nextLine();
        System.out.print("Date de naissance (AAAA-MM-JJ) : ");
        LocalDate dn = LocalDate.parse(sc.nextLine());

        Patient p = new Patient(id, nom, prenom, dn);

        boolean validGS = false;
        do {
            System.out.print("Groupe sanguin (A+, A-, B+, B-, AB+, AB-, O+, O-) : ");
            String gs = sc.nextLine();
            try {
                p.setGroupeSanguin(GroupeSanguin.fromLabel(gs));
                validGS = true;
            } catch (IllegalArgumentException e) {
                System.out.println("⚠ Groupe sanguin invalide.");
            }
        } while (!validGS);

        boolean validTel = false;
        do {
            System.out.print("Téléphone : ");
            String tel = sc.nextLine();
            if (tel.matches("[+\\d\\s]{10,}")) {
                p.setTelephone(tel);
                validTel = true;
            } else {
                System.out.println("⚠ Numéro invalide.");
            }
        } while (!validTel);

        patients.add(p);
        System.out.println("✅ " + p.getIdentiteComplete() + " enregistré (" + p.getAge() + " ans)");
    }

    static void ajouterMedecin(Scanner sc) {
        System.out.println("\n--- Nouveau Médecin ---");
        String id = String.format("M%03d", prochainIdMedecin++);

        System.out.print("Nom : ");
        String nom = sc.nextLine();
        System.out.print("Prénom : ");
        String prenom = sc.nextLine();
        System.out.print("Date de naissance (AAAA-MM-JJ) : ");
        LocalDate dn = LocalDate.parse(sc.nextLine());
        
        Specialite speEnum = null;
        while (speEnum == null) {
            System.out.print("Spécialité (ex: CARDIOLOGIE, PEDIATRIE, URGENCES) : ");
            String spe = sc.nextLine().toUpperCase();
            try {
                speEnum = Specialite.valueOf(spe);
            } catch (IllegalArgumentException e) {
                System.out.println("⚠ Spécialité invalide.");
            }
        }
        
        System.out.print("Matricule : ");
        String mat = sc.nextLine();

        Medecin m = new Medecin(id, nom, prenom, dn, speEnum, mat);
        medecins.add(m);
        System.out.println("✅ " + m + " enregistré");
    }

    static void afficherTousLesPatients() {
        if (patients.isEmpty()) {
            System.out.println("\nAucun patient enregistré.");
            return;
        }
        System.out.println("\n--- Patients ---");
        System.out.printf("%-6s %-15s %-15s %-5s %-5s%n", "ID", "Nom", "Prénom", "Âge", "Sang");
        System.out.println("─".repeat(50));
        for (Patient p : patients) {
            System.out.printf("%-6s %-15s %-15s %-5d %-5s%n",
                p.getId(), p.getNom(), p.getPrenom(),
                p.getAge(),
                p.getGroupeSanguin() != null ? p.getGroupeSanguin().getLabel() : "—");
        }
    }

    static void afficherTousLesMedecins() {
        if (medecins.isEmpty()) {
            System.out.println("\nAucun médecin enregistré.");
            return;
        }
        System.out.println("\n--- Médecins ---");
        for (Medecin m : medecins) {
            System.out.println("  → " + m);
        }
    }

    static void affecterPatientAuService(Scanner sc) {
        if (patients.isEmpty()) {
            System.out.println("\nAucun patient à affecter.");
            return;
        }

        System.out.print("\nID du patient : ");
        String idPat = sc.nextLine();
        Patient patient = null;
        for (Patient p : patients) {
            if (p.getId().equals(idPat)) { patient = p; break; }
        }
        if (patient == null) {
            System.out.println("⚠ Patient introuvable.");
            return;
        }

        System.out.println("Services disponibles :");
        for (int i = 0; i < services.size(); i++) {
            System.out.println("  " + (i+1) + ". " + services.get(i).getNom());
        }
        System.out.print("Votre choix : ");
        int idx = lireEntier(sc) - 1;

        if (idx < 0 || idx >= services.size()) {
            System.out.println("⚠ Service invalide.");
            return;
        }

        ServiceHospitalier service = services.get(idx);
        if (service.admettre(patient)) {
            patient.setServiceAdmission(service.getNom());
            System.out.println("✅ " + patient.getIdentiteComplete() + " → " + service.getNom());
        }
    }

    static void tableauDeBordServices() {
        for (ServiceHospitalier s : services) {
            s.afficherTableauDeBord();
        }
    }

    static void supprimerPatient(Scanner sc) {
        System.out.print("\nID du patient à supprimer : ");
        String id = sc.nextLine();
        Patient patient = null;
        for (Patient p : patients) {
            if (p.getId().equals(id)) { patient = p; break; }
        }
        if (patient == null) {
            System.out.println("⚠ Patient introuvable.");
            return;
        }

        System.out.println(patient);
        System.out.print("Confirmer la suppression (o/n) : ");
        if (sc.nextLine().equalsIgnoreCase("o")) {
            patients.remove(patient);
            for (ServiceHospitalier s : services) {
                s.retirerPatient(patient);
            }
            System.out.println("✅ Patient supprimé.");
        }
    }

    static void modifierPatient(Scanner sc) {
        System.out.print("\nID du patient à modifier : ");
        String id = sc.nextLine();
        Patient patient = null;
        for (Patient p : patients) {
            if (p.getId().equals(id)) { patient = p; break; }
        }
        if (patient == null) {
            System.out.println("⚠ Patient introuvable.");
            return;
        }

        int choix;
        do {
            System.out.println("\n--- Modifier Patient : " + patient.getIdentiteComplete() + " ---");
            System.out.println("1. Nom [" + patient.getNom() + "]");
            System.out.println("2. Prénom [" + patient.getPrenom() + "]");
            System.out.println("3. Date de naissance [" + patient.getAge() + " ans]");
            System.out.println("4. Téléphone [" + patient.getTelephone() + "]");
            System.out.println("5. Groupe sanguin [" + (patient.getGroupeSanguin() != null ? patient.getGroupeSanguin().getLabel() : "Non défini") + "]");
            System.out.println("0. Terminer");
            System.out.print("Votre choix : ");
            
            choix = lireEntier(sc);

            switch (choix) {
                case 1 -> {
                    System.out.print("Nouveau nom : ");
                    patient.setNom(sc.nextLine());
                }
                case 2 -> {
                    System.out.print("Nouveau prénom : ");
                    patient.setPrenom(sc.nextLine());
                }
                case 3 -> {
                    System.out.print("Nouvelle date de naissance (AAAA-MM-JJ) : ");
                    patient.setDateNaissance(LocalDate.parse(sc.nextLine()));
                }
                case 4 -> {
                    boolean validTel = false;
                    do {
                        System.out.print("Nouveau téléphone : ");
                        String tel = sc.nextLine();
                        if (tel.matches("[+\\d\\s]{10,}")) {
                            patient.setTelephone(tel);
                            validTel = true;
                        } else {
                            System.out.println("⚠ Numéro invalide.");
                        }
                    } while (!validTel);
                }
                case 5 -> {
                    boolean validGS = false;
                    do {
                        System.out.print("Nouveau groupe sanguin (A+, A-, B+, etc.) : ");
                        String gs = sc.nextLine();
                        try {
                            patient.setGroupeSanguin(GroupeSanguin.fromLabel(gs));
                            validGS = true;
                        } catch (IllegalArgumentException e) {
                            System.out.println("⚠ Groupe sanguin invalide.");
                        }
                    } while (!validGS);
                }
                case 0 -> System.out.println("✅ Modifications enregistrées.");
                default -> System.out.println("⚠ Choix invalide.");
            }
        } while (choix != 0);
    }

    static void affecterMedecinAuService(Scanner sc) {
        if (medecins.isEmpty()) {
            System.out.println("\nAucun médecin enregistré.");
            return;
        }
        System.out.print("\nID du médecin : ");
        String idMed = sc.nextLine();
        Medecin medecin = null;
        for (Medecin m : medecins) {
            if (m.getId().equals(idMed)) { medecin = m; break; }
        }
        if (medecin == null) {
            System.out.println("⚠ Médecin introuvable.");
            return;
        }

        System.out.println("Services disponibles :");
        for (int i = 0; i < services.size(); i++) {
            System.out.println("  " + (i+1) + ". " + services.get(i).getNom());
        }
        System.out.print("Votre choix : ");
        int idx = lireEntier(sc) - 1;

        if (idx < 0 || idx >= services.size()) {
            System.out.println("⚠ Service invalide.");
            return;
        }

        services.get(idx).ajouterMedecin(medecin);
        System.out.println("✅ Dr " + medecin.getNom() + " affecté au service " + services.get(idx).getNom());
    }

    static void planifierRendezVous(Scanner sc) {
        if (patients.isEmpty() || medecins.isEmpty()) {
            System.out.println("⚠ Au moins un patient et un médecin sont requis.");
            return;
        }
        System.out.print("\nID du patient : ");
        String idPat = sc.nextLine();
        Patient p = null;
        for (Patient pat : patients) if (pat.getId().equals(idPat)) p = pat;

        System.out.print("ID du médecin : ");
        String idMed = sc.nextLine();
        Medecin m = null;
        for (Medecin med : medecins) if (med.getId().equals(idMed)) m = med;

        if (p == null || m == null) {
            System.out.println("⚠ Patient ou médecin introuvable.");
            return;
        }

        LocalDateTime dt = null;
        boolean validDate = false;
        do {
            System.out.print("Date et heure (AAAA-MM-JJTHH:MM) : ");
            String input = sc.nextLine();
            try {
                dt = LocalDateTime.parse(input);
                validDate = true;
            } catch (java.time.format.DateTimeParseException e) {
                System.out.println("⚠ Format invalide. Veuillez utiliser AAAA-MM-JJTHH:MM.");
            }
        } while (!validDate);

        RendezVous rdv = new RendezVous(p, m, dt);
        rendezVousList.add(rdv);
        System.out.println("✅ Rendez-vous planifié.");
    }

    static void testerEquals() {
        System.out.println("\n--- Test equals et hashCode ---");
        Patient p1 = new Patient("P999", "zz", "bb", LocalDate.of(1990, 1, 1));
        Patient p2 = new Patient("P999", "aa", "oo", LocalDate.of(1992, 2, 2));

        System.out.println("p1 == p2 : " + (p1 == p2));
        System.out.println("p1.equals(p2) : " + p1.equals(p2));
        System.out.println("p1.hashCode() == p2.hashCode() : " + (p1.hashCode() == p2.hashCode()));
    }
}