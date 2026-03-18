package exceptions;

public class PatientNotFoundException extends MedManagerException {
    private final String idRecherche;

    public PatientNotFoundException(String id) {
        super("Patient introuvable (ID: " + id + ")");
        this.idRecherche = id;
    }

    public String getIdRecherche() { return idRecherche; }
}