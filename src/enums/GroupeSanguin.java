package enums;

import exceptions.InvalidGroupeSanguinException;

public enum GroupeSanguin {
    A_POSITIF("A+"), A_NEGATIF("A-"),
    B_POSITIF("B+"), B_NEGATIF("B-"),
    AB_POSITIF("AB+"), AB_NEGATIF("AB-"),
    O_POSITIF("O+"), O_NEGATIF("O-");

    private final String label;
    GroupeSanguin(String label) { this.label = label; }
    public String getLabel() { return label; }

    public static GroupeSanguin fromLabel(String label) {
        for (GroupeSanguin gs : values()) {
            if (gs.label.equalsIgnoreCase(label)) return gs;
        }

        IllegalArgumentException cause = new IllegalArgumentException(
            "Valeur inconnue dans l'enum GroupeSanguin : '" + label + "'");
        throw new InvalidGroupeSanguinException(label, cause);
    }
}