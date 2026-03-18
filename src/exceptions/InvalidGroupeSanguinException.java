package exceptions;

public class InvalidGroupeSanguinException extends MedManagerException {
    private static final long serialVersionUID = 1L;

    public InvalidGroupeSanguinException(String valeur) {
        super("Groupe sanguin invalide : '" + valeur
            + "'. Valeurs acceptées : A+, A-, B+, B-, AB+, AB-, O+, O-");
    }


    public InvalidGroupeSanguinException(String valeur, Throwable cause) {
        super("Groupe sanguin invalide : '" + valeur
            + "'. Valeurs acceptées : A+, A-, B+, B-, AB+, AB-, O+, O-", cause);
    }
}