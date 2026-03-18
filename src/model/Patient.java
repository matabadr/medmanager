package model;

import enums.GroupeSanguin;
import exceptions.InvalidPatientDataException; // NOUVEAU (Exercice 2)
import interfaces.Identifiable;
import java.time.LocalDate;

public class Patient extends Personne implements Identifiable {
    private GroupeSanguin groupeSanguin;
    private String serviceAdmission;

    public Patient(String id, String nom, String prenom, LocalDate dateNaissance) {
        super(id, nom, prenom, dateNaissance); 

        if (!id.matches("P\\d+"))
            throw new InvalidPatientDataException("id", id,
                "doit commencer par 'P' suivi de chiffres (ex: P001)");


        if (!nom.matches("[a-zA-ZÀ-ÿ\\s]+"))
            throw new InvalidPatientDataException("nom", nom,
                "ne doit contenir que des lettres et espaces");

        if (!prenom.matches("[a-zA-ZÀ-ÿ\\s]+"))
            throw new InvalidPatientDataException("prenom", prenom,
                "ne doit contenir que des lettres et espaces");

        if (dateNaissance.isAfter(LocalDate.now()))
            throw new InvalidPatientDataException("dateNaissance", dateNaissance.toString(),
                "ne peut pas être dans le futur");
    }

    @Override
    public String getRole() { return "Patient"; }

    public GroupeSanguin getGroupeSanguin() { return groupeSanguin; }
    public void setGroupeSanguin(GroupeSanguin gs) { this.groupeSanguin = gs; }

    public String getServiceAdmission() { return serviceAdmission; }
    public void setServiceAdmission(String serviceAdmission) { this.serviceAdmission = serviceAdmission; }

    @Override
    public String getIdentiteComplete() {
        return getRole() + " " + getPrenom() + " " + getNom();
    }
}