package ui;

import model.Utilisateur;

/** Single-process session holder for the currently authenticated user. */
public final class Session {
    private static Utilisateur current;

    private Session() {}

    public static Utilisateur current() { return current; }

    public static void set(Utilisateur u) { current = u; }

    public static void clear() { current = null; }

    public static boolean isAuthenticated() { return current != null; }

    public static boolean isAdmin() {
        return current != null && current.getRole() == Utilisateur.Role.ADMIN;
    }

    public static boolean isMedecin() {
        return current != null && current.getRole() == Utilisateur.Role.MEDECIN;
    }

    public static boolean isInfirmier() {
        return current != null && current.getRole() == Utilisateur.Role.INFIRMIER;
    }

    public static boolean isPatient() {
        return current != null && current.getRole() == Utilisateur.Role.PATIENT;
    }

    /** May see the admin-management screen and the audit log. */
    public static boolean canManageAdmins() { return isAdmin(); }

    /** May edit patient records. INFIRMIER is read-only on those. */
    public static boolean canEditPatients() { return isAdmin() || isMedecin(); }

    /** May edit medecin records. Only ADMIN. */
    public static boolean canEditMedecins() { return isAdmin(); }
}
