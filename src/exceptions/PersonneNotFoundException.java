package exceptions;

public class PersonneNotFoundException extends MedManagerException {

    private final String idRecherche;
    private final String typePersonne;

    public PersonneNotFoundException(String id, String typePersonne) {
        super(typePersonne + " introuvable (ID: " + id + ")");
        this.idRecherche = id;
        this.typePersonne = typePersonne;
    }

    public String getIdRecherche() { return idRecherche; }
    public String getTypePersonne() { return typePersonne; } 
}