package exceptions;

public class ServiceCompletException extends MedManagerException {
    private final String nomService;
    private final int capacite;

    public ServiceCompletException(String nomService, int capacite) {
        super("Service " + nomService + " complet (" + capacite + "/" + capacite + " lits)");
        this.nomService = nomService;
        this.capacite = capacite;
    }

    public String getNomService() { return nomService; }
    public int getCapacite() { return capacite; }
}