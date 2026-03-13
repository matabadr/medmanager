package model;
import enums.GroupeSanguin;
import interfaces.Identifiable;
import java.time.LocalDate;

public class Patient extends Personne implements Identifiable {    
	private GroupeSanguin groupeSanguin;
    private String serviceAdmission; 

    public Patient(String id, String nom, String prenom, LocalDate dateNaissance) {
        super(id, nom, prenom, dateNaissance);
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