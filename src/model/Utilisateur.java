package model;

import java.time.LocalDateTime;

public class Utilisateur {
    public enum Role { ADMIN, MEDECIN, INFIRMIER, PATIENT }

    private final String username;
    private final Role role;
    private final String nomComplet;
    private final boolean actif;
    private final LocalDateTime derniereConnexion;
    private final String patientId;     // set only for PATIENT role

    public Utilisateur(String username, Role role, String nomComplet,
                       boolean actif, LocalDateTime derniereConnexion,
                       String patientId) {
        this.username = username;
        this.role = role;
        this.nomComplet = nomComplet;
        this.actif = actif;
        this.derniereConnexion = derniereConnexion;
        this.patientId = patientId;
    }

    public String getUsername()                 { return username; }
    public Role getRole()                       { return role; }
    public String getNomComplet()               { return nomComplet; }
    public boolean isActif()                    { return actif; }
    public LocalDateTime getDerniereConnexion() { return derniereConnexion; }
    public String getPatientId()                { return patientId; }

    public String getDisplayName() {
        return (nomComplet == null || nomComplet.isBlank()) ? username : nomComplet;
    }
}
