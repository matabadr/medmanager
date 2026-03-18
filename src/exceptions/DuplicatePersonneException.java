package exceptions;

public class DuplicatePersonneException extends MedManagerException {
    public DuplicatePersonneException(String id) {
        super("Une personne avec l'ID '" + id + "' existe déjà");
    }
}