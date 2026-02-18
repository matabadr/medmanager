package project;

import java.util.Scanner;
// test commit change
public class Main {

    // ── Constantes patients ──
    static final int MAX_PATIENTS = 100;
    static String[] nomsPatients = new String[MAX_PATIENTS];
    static String[] prenomsPatients = new String[MAX_PATIENTS];
    static int[] anneesNaissance = new int[MAX_PATIENTS];
    static int[] servicePatients = new int[MAX_PATIENTS];
    static int nbPatients = 0;

    // ── Constantes services ──
    static final int MAX_SERVICES = 10;
    static String[] nomsServices = {"Cardiologie", "Pédiatrie", "Urgences", "Chirurgie","dentist","general"};
    static int[] capacitesServices = {20, 15, 30, 10,15,50};
    static int[] occupationServices = {0, 0, 0, 0,0,0};
    static int nbServices = 6;

    // ════════════════════════════════════════
    //  MAIN
    // ════════════════════════════════════════
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int choix;

        do {
            afficherMenu();
            choix = lireChoix(scanner);

            switch (choix) {
                case 1 -> ajouterPatient(scanner);
                case 2 -> afficherPatients();
                case 3 -> rechercherPatient(scanner);
                case 4 -> afficherStatistiques();
                case 5 -> afficherPatientsTries();
                case 0 -> System.out.println("\n👋 A Bientot !");
                default -> System.out.println("⚠ Choix invalide.");
            }
        } while (choix != 0);

        scanner.close();
    }

    // ════════════════════════════════════════
    //  MENU
    // ════════════════════════════════════════
    static void afficherMenu() {
        System.out.println("\n══════ MedManager v0.1 ══════");
        System.out.println("  1. ➕ Ajouter un patient");
        System.out.println("  2. 📋 Afficher tous les patients");
        System.out.println("  3. 🔍 Rechercher un patient");
        System.out.println("  4. 📊 Statistiques");
        System.out.println("  5. 🔤 Patients par ordre alphabétique");
        System.out.println("  0. 🚪 Quitter");
        System.out.print("Votre choix : ");
    }

    // ════════════════════════════════════════
    //  UTILITAIRE — Lire un entier
    // ════════════════════════════════════════
    static int lireChoix(Scanner scanner) {
        while (!scanner.hasNextInt()) {
            System.out.print("⚠ Entrez un nombre : ");
            scanner.next();
        }
        int choix = scanner.nextInt();
        scanner.nextLine(); // nettoie le buffer
        return choix;
    }

    // ════════════════════════════════════════
    //  1 — AJOUTER UN PATIENT (ex. 1 + ex. 4)
    // ════════════════════════════════════════
    static void ajouterPatient(Scanner scanner) {
        if (nbPatients >= MAX_PATIENTS) {
            System.out.println("⚠ Capacité maximale atteinte !");
            return;
        }

        System.out.println("\n--- Nouveau Patient ---");

        System.out.print("Nom : ");
        nomsPatients[nbPatients] = scanner.nextLine();

        System.out.print("Prénom : ");
        prenomsPatients[nbPatients] = scanner.nextLine();

        // Exercice 1 — Validation de l'âge
        int age;
        do {
            System.out.print("Année de naissance : ");
            anneesNaissance[nbPatients] = lireChoix(scanner);
            age = 2026 - anneesNaissance[nbPatients];

            if (age < 0 || age > 150) {
                System.out.println("❌ Âge invalide (" + age + " ans). Veuillez réessayer.");
            }
        } while (age < 0 || age > 150);

        // Exercice 4 — Choisir un service
        int serviceIndex = choisirService(scanner);
        servicePatients[nbPatients] = serviceIndex;
        occupationServices[serviceIndex]++;
        nbPatients++;

        System.out.println("✅ Patient enregistré (" + age + " ans) → " + nomsServices[serviceIndex]);
    }

    // ════════════════════════════════════════
    //  2 — AFFICHER LES PATIENTS (ex. 5)
    // ════════════════════════════════════════
    static void afficherPatients() {
        if (nbPatients == 0) {
            System.out.println("\nAucun patient enregistré.");
            return;
        }

        System.out.println("\n┌─────┬────────────────┬────────────────┬───────┬────────────────┐");
        System.out.println("│  #  │ Nom            │ Prénom         │  Âge  │ Service        │");
        System.out.println("├─────┼────────────────┼────────────────┼───────┼────────────────┤");

        for (int i = 0; i < nbPatients; i++) {
            int age = 2026 - anneesNaissance[i];
            System.out.printf("│ %-3d │ %-14s │ %-14s │ %-5d │ %-14s │%n",
                (i + 1), nomsPatients[i], prenomsPatients[i], age,
                nomsServices[servicePatients[i]]);
        }

        System.out.println("└─────┴────────────────┴────────────────┴───────┴────────────────┘");
        System.out.println("  Total : " + nbPatients + " patient(s)");
    }

    // ════════════════════════════════════════
    //  3 — RECHERCHER UN PATIENT
    // ════════════════════════════════════════
    static void rechercherPatient(Scanner scanner) {
        System.out.print("\nRechercher (nom) : ");
        String recherche = scanner.nextLine().toLowerCase();
        boolean trouve = false;

        for (int i = 0; i < nbPatients; i++) {
            if (nomsPatients[i].toLowerCase().contains(recherche)) {
                int age = 2026 - anneesNaissance[i];
                System.out.println("→ " + prenomsPatients[i] + " " + nomsPatients[i]
                    + " (" + age + " ans) — " + nomsServices[servicePatients[i]]);
                trouve = true;
            }
        }

        if (!trouve) {
            System.out.println("Aucun résultat pour \"" + recherche + "\"");
        }
    }

    // ════════════════════════════════════════
    //  4 — STATISTIQUES (ex. 2)
    // ════════════════════════════════════════
    static void afficherStatistiques() {
        if (nbPatients == 0) {
            System.out.println("\nAucun patient enregistré.");
            return;
        }

        int sommeAges = 0;
        int ageMin = 2026 - anneesNaissance[0];
        int ageMax = 2026 - anneesNaissance[0];

        for (int i = 0; i < nbPatients; i++) {
            int age = 2026 - anneesNaissance[i];
            sommeAges += age;
            if (age < ageMin) ageMin = age;
            if (age > ageMax) ageMax = age;
        }

        double ageMoyen = (double) sommeAges / nbPatients;

        System.out.println("\n--- 📊 Statistiques ---");
        System.out.println("Total patients : " + nbPatients);
        System.out.printf("Âge moyen      : %.1f ans%n", ageMoyen);
        System.out.println("Plus jeune     : " + ageMin + " ans");
        System.out.println("Plus vieux     : " + ageMax + " ans");

        System.out.println("\n--- Occupation des services ---");
        for (int i = 0; i < nbServices; i++) {
            System.out.printf("  %-15s : %d / %d%n",
                nomsServices[i], occupationServices[i], capacitesServices[i]);
        }
    }

    // ════════════════════════════════════════
    //  5 — TRI PAR NOM (ex. 3)
    // ════════════════════════════════════════
    static void afficherPatientsTries() {
        if (nbPatients == 0) {
            System.out.println("\nAucun patient enregistré.");
            return;
        }

        // Copie des tableaux pour ne pas modifier l'ordre original
        String[] noms = nomsPatients.clone();
        String[] prenoms = prenomsPatients.clone();
        int[] annees = anneesNaissance.clone();
        int[] services = servicePatients.clone();

        // Tri à bulles
        for (int i = 0; i < nbPatients - 1; i++) {
            for (int j = 0; j < nbPatients - 1 - i; j++) {
                if (noms[j].compareToIgnoreCase(noms[j + 1]) > 0) {
                    String tmpNom = noms[j];
                    noms[j] = noms[j + 1];
                    noms[j + 1] = tmpNom;

                    String tmpPrenom = prenoms[j];
                    prenoms[j] = prenoms[j + 1];
                    prenoms[j + 1] = tmpPrenom;

                    int tmpAnnee = annees[j];
                    annees[j] = annees[j + 1];
                    annees[j + 1] = tmpAnnee;

                    int tmpService = services[j];
                    services[j] = services[j + 1];
                    services[j + 1] = tmpService;
                }
            }
        }

        // Affichage
        System.out.println("\n┌─────┬────────────────┬────────────────┬───────┬────────────────┐");
        System.out.println("│  #  │ Nom            │ Prénom         │  Âge  │ Service        │");
        System.out.println("├─────┼────────────────┼────────────────┼───────┼────────────────┤");

        for (int i = 0; i < nbPatients; i++) {
            int age = 2026 - annees[i];
            System.out.printf("│ %-3d │ %-14s │ %-14s │ %-5d │ %-14s │%n",
                (i + 1), noms[i], prenoms[i], age, nomsServices[services[i]]);
        }

        System.out.println("└─────┴────────────────┴────────────────┴───────┴────────────────┘");
    }

    // ════════════════════════════════════════
    //  EXERCICE 4 — Choisir un service
    // ════════════════════════════════════════
    static int choisirService(Scanner scanner) {
        System.out.println("\nServices disponibles :");
        for (int i = 0; i < nbServices; i++) {
            int places = capacitesServices[i] - occupationServices[i];
            System.out.printf("  %d. %-15s (%d place(s) restante(s))%n",
                (i + 1), nomsServices[i], places);
        }

        int choix;
        do {
            System.out.print("Choisir un service (1-" + nbServices + ") : ");
            choix = lireChoix(scanner) - 1;

            if (choix < 0 || choix >= nbServices) {
                System.out.println("❌ Service invalide.");
            } else if (occupationServices[choix] >= capacitesServices[choix]) {
                System.out.println("❌ Service complet, choisissez un autre.");
                choix = -1;
            }
        } while (choix < 0 || choix >= nbServices);

        return choix;
    }
}