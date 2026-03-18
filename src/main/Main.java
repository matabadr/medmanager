package main;

import interfaces.Action;
import model.*;
import enums.*;
import exceptions.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {

    static List<Patient> patients = new ArrayList<>();
    static List<Medecin> medecins = new ArrayList<>();
    static List<Administrateur> administrateurs = new ArrayList<>();
    static List<ServiceHospitalier> services = new ArrayList<>();
    static List<RendezVous> rendezVousList = new ArrayList<>();
    static int prochainIdPatient = 1;
    static int prochainIdMedecin = 1;
    static int prochainIdAdmin = 1;

    static List<Action> actions = new ArrayList<>();

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        services.add(new ServiceHospitalier("Cardiologie", TypeService.CLINIQUE, 30));
        services.add(new ServiceHospitalier("Urgences",    TypeService.CLINIQUE, 50));
        services.add(new ServiceHospitalier("Pédiatrie",   TypeService.CLINIQUE, 2));

        actions.add(new AjouterPatientAction());
        actions.add(new AjouterMedecinAction());
        actions.add(new AfficherPatientsAction());
        actions.add(new AfficherMedecinsAction());
        actions.add(new AffecterPatientAction());
        actions.add(new TableauDeBordAction());
        actions.add(new SupprimerPatientAction());
        actions.add(new ModifierPatientAction());
        actions.add(new AffecterMedecinAction());
        actions.add(new PlanifierRendezVousAction());
        actions.add(new TesterEqualsAction());
        actions.add(new ChercherPatientAction());
        actions.add(new AjouterAdministrateurAction());

        int choix;
        do {
            afficherMenu();
            choix = lireEntierDansPlage(sc, 0, actions.size(), "Votre choix : ");
            if (choix == 0) {
                System.out.println("\n👋 Fermeture de MedManager.");
            } else {
                actions.get(choix - 1).executer(sc);
            }
        } while (choix != 0);

        sc.close();
    }

    static void afficherMenu() {
        System.out.println("\n══════ MedManager v1.2 ══════");
        for (int i = 0; i < actions.size(); i++) {
            System.out.println("  " + (i + 1) + ". " + actions.get(i).getLibelle());
        }
        System.out.println("  0. 🚪 Quitter");
    }

    static String lireChamp(Scanner sc, String invite) {
        while (true) {
            System.out.print(invite);
            String valeur = sc.nextLine().trim();
            if (valeur.isBlank()) {
                Logger.warn("Ce champ ne peut pas être vide.");
            } else {
                return valeur;
            }
        }
    }

    static LocalDate lireDate(Scanner sc, String invite) {
        while (true) {
            System.out.print(invite);
            String saisie = sc.nextLine().trim();
            try {
                return LocalDate.parse(saisie);
            } catch (DateTimeParseException e) {
                Logger.warn("Format de date invalide : \"" + saisie + "\" — attendu : AAAA-MM-JJ");
            }
        }
    }

    static GroupeSanguin lireGroupeSanguin(Scanner sc) {
        while (true) {
            System.out.print("Groupe sanguin (A+, A-, B+, B-, AB+, AB-, O+, O-) : ");
            String saisie = sc.nextLine().trim();
            try {
                return GroupeSanguin.fromLabel(saisie);
            } catch (InvalidGroupeSanguinException e) {
                Logger.warn(e.getMessage());
            }
        }
    }

    static int lireEntierDansPlage(Scanner sc, int min, int max, String invite) {
        while (true) {
            System.out.print(invite);
            try {
                int valeur = Integer.parseInt(sc.nextLine().trim());
                if (valeur < min || valeur > max) {
                    Logger.warn("Valeur hors plage. Entrez un nombre entre " + min + " et " + max + ".");
                } else {
                    return valeur;
                }
            } catch (NumberFormatException e) {
                Logger.warn("Saisie invalide. Entrez un nombre entier.");
            }
        }
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

    static Patient chercherPatient(String id) {
        for (Patient p : patients) {
            if (p.getId().equals(id)) return p;
        }
        throw new PersonneNotFoundException(id, "Patient");
    }

    static class AjouterPatientAction implements Action {
        public String getLibelle() { return "➕ Ajouter un patient"; }
        public void executer(Scanner sc) {
            System.out.println("\n--- Nouveau Patient ---");
            String id = String.format("P%03d", prochainIdPatient++);


            String nom    = lireChamp(sc, "Nom : ");
            String prenom = lireChamp(sc, "Prénom : ");
            LocalDate dn  = lireDate(sc, "Date de naissance (AAAA-MM-JJ) : ");

            try {
                Patient p = new Patient(id, nom, prenom, dn);
                p.setGroupeSanguin(lireGroupeSanguin(sc));

                boolean validTel = false;
                do {
                    System.out.print("Téléphone : ");
                    String tel = sc.nextLine();
                    if (tel.matches("[+\\d\\s]{10,}")) {
                        p.setTelephone(tel);
                        validTel = true;
                    } else {
                        Logger.warn("Numéro invalide.");
                    }
                } while (!validTel);

                patients.add(p);
                Logger.info(p.getIdentiteComplete() + " enregistré (" + p.getAge() + " ans)");

            } catch (InvalidPatientDataException e) {
                Logger.error("Données patient invalides", e);
            } catch (IllegalArgumentException e) {
                Logger.warn(e.getMessage());
            }
        }
    }

    static class AjouterMedecinAction implements Action {
        public String getLibelle() { return "➕ Ajouter un médecin"; }
        public void executer(Scanner sc) {
            System.out.println("\n--- Nouveau Médecin ---");
            String id = String.format("M%03d", prochainIdMedecin++);

            String nom    = lireChamp(sc, "Nom : ");
            String prenom = lireChamp(sc, "Prénom : ");
            LocalDate dn  = lireDate(sc, "Date de naissance (AAAA-MM-JJ) : ");

            Specialite speEnum = null;
            while (speEnum == null) {
                System.out.print("Spécialité (ex: CARDIOLOGIE, PEDIATRIE, URGENCES) : ");
                String spe = sc.nextLine().toUpperCase();
                try {
                    speEnum = Specialite.valueOf(spe);
                } catch (IllegalArgumentException e) {
                    Logger.warn("Spécialité invalide.");
                }
            }

            String mat = lireChamp(sc, "Matricule : ");

            Medecin m = new Medecin(id, nom, prenom, dn, speEnum, mat);
            medecins.add(m);
            Logger.info(m + " enregistré");
        }
    }

    static class AfficherPatientsAction implements Action {
        public String getLibelle() { return "📋 Afficher les patients"; }
        public void executer(Scanner sc) {
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
    }

    static class AfficherMedecinsAction implements Action {
        public String getLibelle() { return "📋 Afficher les médecins"; }
        public void executer(Scanner sc) {
            if (medecins.isEmpty()) {
                System.out.println("\nAucun médecin enregistré.");
                return;
            }
            System.out.println("\n--- Médecins ---");
            for (Medecin m : medecins) {
                System.out.println("  → " + m);
            }
        }
    }

    static class AffecterPatientAction implements Action {
        public String getLibelle() { return "🏥 Affecter patient → service"; }
        public void executer(Scanner sc) {
            if (patients.isEmpty()) {
                System.out.println("\nAucun patient à affecter.");
                return;
            }

            System.out.print("\nID du patient : ");
            String idPat = sc.nextLine();

            try {
                Patient patient = chercherPatient(idPat);

                System.out.println("Services disponibles :");
                for (int i = 0; i < services.size(); i++) {
                    System.out.println("  " + (i + 1) + ". " + services.get(i).getNom());
                }

                int idx = lireEntierDansPlage(sc, 1, services.size(), "Votre choix : ") - 1;
                ServiceHospitalier service = services.get(idx);
                service.admettre(patient);
                Logger.info(patient.getIdentiteComplete() + " admis en " + service.getNom());

            } catch (PersonneNotFoundException e) {
                Logger.warn(e.getMessage());

            } catch (ServiceCompletException e) {
                Logger.warn(e.getMessage());
                ServiceHospitalier servicePlein = null;
                for (ServiceHospitalier s : services) {
                    if (s.getNom().equals(e.getNomService())) { servicePlein = s; break; }
                }
                if (servicePlein != null) {
                    try {
                        Patient patient = chercherPatient(idPat);
                        int position = servicePlein.ajouterFileAttente(patient);
                        Logger.info(patient.getIdentiteComplete()
                            + " ajouté en file d'attente — position : " + position
                            + "/" + servicePlein.getTailleFileAttente());
                    } catch (PersonneNotFoundException ignored) {}
                }

            } catch (DuplicatePersonneException e) {
                Logger.warn(e.getMessage());
            }
        }
    }

    static class TableauDeBordAction implements Action {
        public String getLibelle() { return "📊 Tableau de bord des services"; }
        public void executer(Scanner sc) {
            for (ServiceHospitalier s : services) {
                s.afficherTableauDeBord();
            }
        }
    }

    static class SupprimerPatientAction implements Action {
        public String getLibelle() { return "🗑 Supprimer un patient"; }
        public void executer(Scanner sc) {
            System.out.print("\nID du patient à supprimer : ");
            String id = sc.nextLine();
            try {
                Patient patient = chercherPatient(id);
                System.out.println(patient);
                System.out.print("Confirmer la suppression (o/n) : ");
                if (sc.nextLine().equalsIgnoreCase("o")) {
                    patients.remove(patient);
                    for (ServiceHospitalier s : services) s.retirerPatient(patient);
                    Logger.info("Patient " + patient.getId() + " supprimé.");
                }
            } catch (PersonneNotFoundException e) {
                Logger.warn(e.getMessage());
            }
        }
    }

    static class ModifierPatientAction implements Action {
        public String getLibelle() { return "✏️ Modifier un patient"; }
        public void executer(Scanner sc) {
            System.out.print("\nID du patient à modifier : ");
            String id = sc.nextLine();
            try {
                Patient patient = chercherPatient(id);

                int choix;
                do {
                    System.out.println("\n--- Modifier Patient : " + patient.getIdentiteComplete() + " ---");
                    System.out.println("1. Nom [" + patient.getNom() + "]");
                    System.out.println("2. Prénom [" + patient.getPrenom() + "]");
                    System.out.println("3. Date de naissance [" + patient.getAge() + " ans]");
                    System.out.println("4. Téléphone [" + patient.getTelephone() + "]");
                    System.out.println("5. Groupe sanguin [" + (patient.getGroupeSanguin() != null ? patient.getGroupeSanguin().getLabel() : "Non défini") + "]");
                    System.out.println("0. Terminer");

                    choix = lireEntierDansPlage(sc, 0, 5, "Votre choix : ");

                    switch (choix) {
                        // MODIFIÉ — lireChamp() pour nom/prenom
                        case 1 -> patient.setNom(lireChamp(sc, "Nouveau nom : "));
                        case 2 -> patient.setPrenom(lireChamp(sc, "Nouveau prénom : "));
                        case 3 -> patient.setDateNaissance(lireDate(sc, "Nouvelle date de naissance (AAAA-MM-JJ) : "));
                        case 4 -> {
                            boolean validTel = false;
                            do {
                                System.out.print("Nouveau téléphone : ");
                                String tel = sc.nextLine();
                                if (tel.matches("[+\\d\\s]{10,}")) {
                                    patient.setTelephone(tel);
                                    validTel = true;
                                } else {
                                    Logger.warn("Numéro invalide.");
                                }
                            } while (!validTel);
                        }
                        case 5 -> patient.setGroupeSanguin(lireGroupeSanguin(sc));
                        case 0 -> Logger.info("Modifications enregistrées pour " + patient.getId());
                    }
                } while (choix != 0);

            } catch (PersonneNotFoundException e) {
                Logger.warn(e.getMessage());
            }
        }
    }

    static class AffecterMedecinAction implements Action {
        public String getLibelle() { return "🏥 Affecter médecin → service"; }
        public void executer(Scanner sc) {
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
                Logger.warn("Médecin introuvable (ID: " + idMed + ")");
                return;
            }

            System.out.println("Services disponibles :");
            for (int i = 0; i < services.size(); i++) {
                System.out.println("  " + (i + 1) + ". " + services.get(i).getNom());
            }

            int idx = lireEntierDansPlage(sc, 1, services.size(), "Votre choix : ") - 1;
            services.get(idx).ajouterMedecin(medecin);
            Logger.info("Dr " + medecin.getNom() + " affecté au service " + services.get(idx).getNom());
        }
    }

    static class PlanifierRendezVousAction implements Action {
        public String getLibelle() { return "📅 Planifier un rendez-vous"; }
        public void executer(Scanner sc) {
            if (patients.isEmpty() || medecins.isEmpty()) {
                Logger.warn("Au moins un patient et un médecin sont requis.");
                return;
            }
            System.out.print("\nID du patient : ");
            String idPat = sc.nextLine();

            System.out.print("ID du médecin : ");
            String idMed = sc.nextLine();
            Medecin m = null;
            for (Medecin med : medecins) if (med.getId().equals(idMed)) m = med;

            try {
                Patient p = chercherPatient(idPat);

                if (m == null) {
                    Logger.warn("Médecin introuvable (ID: " + idMed + ")");
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
                    } catch (DateTimeParseException e) {
                        Logger.warn("Format invalide. Veuillez utiliser AAAA-MM-JJTHH:MM.");
                    }
                } while (!validDate);

                RendezVous rdv = new RendezVous(p, m, dt);
                rendezVousList.add(rdv);
                Logger.info("Rendez-vous planifié : " + p.getId() + " avec Dr " + m.getNom());

            } catch (PersonneNotFoundException e) {
                Logger.warn(e.getMessage());
            }
        }
    }

    static class TesterEqualsAction implements Action {
        public String getLibelle() { return "🧪 Tester equals et hashCode"; }
        public void executer(Scanner sc) {
            System.out.println("\n--- Test equals et hashCode ---");
            Patient p1 = new Patient("P999", "zz", "bb", LocalDate.of(1990, 1, 1));
            Patient p2 = new Patient("P999", "aa", "oo", LocalDate.of(1992, 2, 2));

            System.out.println("p1 == p2 : " + (p1 == p2));
            System.out.println("p1.equals(p2) : " + p1.equals(p2));
            System.out.println("p1.hashCode() == p2.hashCode() : " + (p1.hashCode() == p2.hashCode()));
        }
    }

    static class ChercherPatientAction implements Action {
        public String getLibelle() { return "🔍 Chercher un patient"; }
        public void executer(Scanner sc) {
            System.out.print("\nID du patient : ");
            String id = sc.nextLine();
            try {
                Patient p = chercherPatient(id);
                System.out.println("\n--- Résultat ---");
                System.out.printf("%-6s %-15s %-15s %-5d %-5s %-15s%n",
                    p.getId(), p.getNom(), p.getPrenom(),
                    p.getAge(),
                    p.getGroupeSanguin() != null ? p.getGroupeSanguin().getLabel() : "—",
                    p.getTelephone() != null ? p.getTelephone() : "—");
                if (p.getServiceAdmission() != null)
                    System.out.println("Service : " + p.getServiceAdmission());
            } catch (PersonneNotFoundException e) {
                Logger.warn(e.getMessage());
            }
        }
    }

    static class AjouterAdministrateurAction implements Action {
        public String getLibelle() { return "➕ Ajouter un administrateur"; }
        public void executer(Scanner sc) {
            System.out.println("\n--- Nouvel Administrateur ---");
            String id = String.format("A%03d", prochainIdAdmin++);

            String nom         = lireChamp(sc, "Nom : ");
            String prenom      = lireChamp(sc, "Prénom : ");
            LocalDate dn       = lireDate(sc, "Date de naissance (AAAA-MM-JJ) : ");
            String departement = lireChamp(sc, "Département : ");
            String poste       = lireChamp(sc, "Poste : ");

            Administrateur a = new Administrateur(id, nom, prenom, dn, departement, poste);
            administrateurs.add(a);
            Logger.info(a.getIdentiteComplete() + " enregistré");
        }
    }
}