package exceptions;

public class InvalidPatientDataException extends MedManagerException {

    private final String champ;   
    private final String valeur; 

    public InvalidPatientDataException(String champ, String valeur, String raison) {
        super("Données patient invalides — " + champ + " : '" + valeur + "' (" + raison + ")");
        this.champ = champ;
        this.valeur = valeur;
    }

    public String getChamp()  { return champ; }
    public String getValeur() { return valeur; }
}