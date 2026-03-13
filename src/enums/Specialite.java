package enums;

public enum Specialite {
    CARDIOLOGIE("Maladies du cœur"),
    NEUROLOGIE("Système nerveux"),
    PEDIATRIE("Maladies des enfants"),
    CHIRURGIE("Interventions chirurgicales"),
    DERMATOLOGIE("Maladies de la peau"),
    PSYCHIATRIE("Santé mentale"),
    RADIOLOGIE("Imagerie médicale"),
    URGENCES("Soins immédiats");

    private final String description;
    Specialite(String description) { this.description = description; }
    public String getDescription() { return description; }
}