package project;

import java.time.LocalDate;

public class TestEqualsHashCode {

    public static void main(String[] args) {

        // Deux patients avec le MÊME ID mais des noms différents
        Patient p1 = new Patient("P001", "Amrani",  "Youssef", LocalDate.of(1995, 7, 22));
        Patient p2 = new Patient("P001", "El Fassi", "Nadia",  LocalDate.of(1988, 11, 5));

        System.out.println("=== Test equals / hashCode / == ===\n");

        System.out.println("p1 : " + p1);
        System.out.println("p2 : " + p2);
        System.out.println();

        // 1. equals() : true — même ID = même patient logiquement
        System.out.println("p1.equals(p2) → " + p1.equals(p2));
        // VRAI : on a défini equals() sur l'ID. Deux fiches avec le même
        // identifiant représentent le même patient, même si les autres
        // champs sont différents (ex. mise à jour de nom de famille).

        // 2. hashCode() : identiques — contrat Java equals/hashCode
        System.out.println("p1.hashCode() → " + p1.hashCode());
        System.out.println("p2.hashCode() → " + p2.hashCode());
        System.out.println("hashCodes égaux ? → " + (p1.hashCode() == p2.hashCode()));
        // VRAI : si equals() est true, hashCode() DOIT être identique.
        // C'est un contrat Java non négociable. Sans ça, HashMap et HashSet
        // ne fonctionneraient pas correctement.

        // 3. == : false — ce sont deux objets distincts en mémoire
        System.out.println("p1 == p2 → " + (p1 == p2));
        // FAUX : chaque `new Patient(...)` crée une nouvelle instance en mémoire.
        // == compare les adresses mémoire, pas le contenu. En Java, on n'utilise
        // presque jamais == pour comparer des objets (sauf null-check).

        System.out.println();
        System.out.println("Conclusion :");
        System.out.println("  equals() = identité LOGIQUE (définie par nous via l'ID)");
        System.out.println("  hashCode() = code de hachage COHÉRENT avec equals()");
        System.out.println("  == = identité PHYSIQUE (adresse mémoire)");
    }
}
