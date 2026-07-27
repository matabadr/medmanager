# MedManager

Application de bureau (JavaFX) pour la gestion d'un établissement hospitalier : patients, personnel médical, rendez-vous, services et statistiques — avec une base de données PostgreSQL en arrière-plan.

Projet réalisé dans le cadre d'un cours de programmation orientée objet (POO) — Java 21, JavaFX 21, PostgreSQL.

## Fonctionnalités

- **Gestion des patients** : ajout, recherche, détail, groupe sanguin, service d'admission
- **Gestion du personnel** : médecins (avec spécialité), infirmiers, techniciens, administrateurs — hiérarchie de classes avec héritage (`Personne` → sous-classes)
- **Services hospitaliers** : affectation des patients par service (clinique, administratif, technique), file d'attente, gestion de capacité
- **Rendez-vous** : planification patient/médecin avec vue calendrier
- **Authentification & rôles** : connexion (`LoginView`), portail patient dédié, session utilisateur, rôles (ADMIN, MEDECIN, INFIRMIER, PATIENT)
- **Tableau de bord & statistiques** : vue d'ensemble de l'activité de l'hôpital
- **Journal d'audit** : traçabilité des actions (`AuditView`, `Audit` DAO)
- **Gestion des erreurs métier** : exceptions personnalisées (patient introuvable, doublon, service complet, données invalides, groupe sanguin invalide)

## Stack technique

| Couche | Technologie |
|---|---|
| Langage | Java 21 |
| Interface graphique | JavaFX 21 (Controls + Graphics) |
| Base de données | PostgreSQL |
| Accès aux données | JDBC (driver `org.postgresql:postgresql`), couche DAO manuelle |
| Build | Maven |

## Architecture

Le projet suit une architecture en couches avec des interfaces pour les comportements communs :

```
src/
├── model/         # Entités métier (Patient, Medecin, Infirmier, Technicien, Administrateur, ServiceHospitalier, RendezVous...)
├── dao/           # Accès aux données (Database, PatientDao, MedecinDao, RendezVousDao, Audit, Stats...)
├── ui/            # Vues JavaFX (LoginView, DashboardView, GestionPatientsView, CalendarView, AuditView...)
├── enums/         # GroupeSanguin, Specialite, TypeService
├── exceptions/    # Exceptions métier personnalisées
├── interfaces/    # Identifiable, Assignable, Consultable, Affichable, Action
├── resources/     # styles.css
└── main/          # Point d'entrée (Main.java) + Logger
```

`Personne` est la classe de base abstraite dont héritent `Patient`, `Medecin`, `Infirmier`, `Technicien` et `Administrateur`, chacune implémentant les interfaces pertinentes selon son rôle (`Identifiable`, `Assignable`, `Consultable`).

## Prérequis

- [JDK 21](https://adoptium.net/) (ou supérieur)
- [Maven](https://maven.apache.org/download.cgi) 3.9+
- PostgreSQL (local ou distant)

## Installation & lancement

### 1. Configurer la base de données

Créez une base PostgreSQL et exécutez les scripts fournis dans `db/` :

```bash
psql -U postgres -f db/schema.sql
psql -U postgres -f db/seed.sql
```

Par défaut l'application se connecte à `jdbc:postgresql://localhost:5432/medmandb`. Vous pouvez surcharger la connexion via des variables d'environnement (voir `Database.java`) :

```bash
set DB_URL=jdbc:postgresql://localhost:5432/medmandb
set DB_USER=postgres
set DB_PASSWORD=votre_mot_de_passe
```

### 2. Lancer l'application avec Maven

Plus besoin de télécharger manuellement le JDK ou le SDK JavaFX : Maven s'en charge automatiquement au premier build.

```bash
mvn javafx:run
```

### 3. (Optionnel) Générer un jar exécutable autonome

```bash
mvn clean package
```

Le jar généré (`target/medmanager-1.1.0-shaded.jar`) embarque toutes les dépendances et peut être lancé directement :

```bash
java -jar target/medmanager-1.1.0-shaded.jar
```

## Développement dans Eclipse

Le projet reste compatible avec Eclipse (`.classpath` / `.project` conservés) : importez-le comme *Existing Maven Project*, Eclipse résoudra les dépendances via `pom.xml`.

## Roadmap

- [ ] Tests unitaires (JUnit)
- [ ] Export des statistiques (PDF/Excel)
- [ ] Packaging multiplateforme (`jlink` / `jpackage`)

## Licence

Projet académique — usage éducatif.
