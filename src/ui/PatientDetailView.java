package ui;

import dao.MedecinDao;
import dao.RendezVousDao;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import model.Patient;
import model.RendezVous;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PatientDetailView extends VBox {

    private final RendezVousDao rDao = new RendezVousDao();
    private final MedecinDao mDao = new MedecinDao();

    public PatientDetailView(Patient p, Runnable onBack) {
        setPadding(new Insets(24, 28, 24, 28));
        setSpacing(16);

        // Header with back button
        Button back = Components.ghost("←  Retour à la liste");
        back.setOnAction(e -> { if (onBack != null) onBack.run(); });

        Label title = new Label("Dossier patient");
        title.getStyleClass().add("section-title");
        Label sub = new Label("Informations détaillées et historique des rendez-vous");
        sub.getStyleClass().add("section-subtitle");
        VBox titleBox = new VBox(2, title, sub);
        HBox.setHgrow(titleBox, Priority.ALWAYS);

        HBox header = new HBox(12, back, titleBox);
        header.setAlignment(Pos.CENTER_LEFT);

        // Identity card
        VBox identity = identityCard(p);

        // Info grid card
        VBox infoCard = infoCard(p);

        // RDV history card
        VBox histo = rdvHistoryCard(p);

        HBox topRow = new HBox(16, identity, infoCard);
        HBox.setHgrow(identity, Priority.ALWAYS);
        HBox.setHgrow(infoCard, Priority.ALWAYS);

        getChildren().addAll(header, topRow, histo);
    }

    private VBox identityCard(Patient p) {
        VBox card = new VBox(8);
        card.getStyleClass().add("card");

        // Avatar circle with initials
        String initials = (text(p.getPrenom(), "?").substring(0, 1)
                         + text(p.getNom(), "?").substring(0, 1)).toUpperCase();
        Label avatar = new Label(initials);
        avatar.setStyle("-fx-min-width: 64; -fx-min-height: 64; -fx-max-width: 64; -fx-max-height: 64;"
                + " -fx-background-color: linear-gradient(to bottom right, #14b8b7, #0b8483);"
                + " -fx-background-radius: 999; -fx-alignment: center;"
                + " -fx-text-fill: white; -fx-font-size: 22px; -fx-font-weight: 800;");

        Label name = new Label(p.getPrenom() + " " + p.getNom());
        name.getStyleClass().add("section-title");
        Label idLine = new Label("ID: " + p.getId());
        idLine.getStyleClass().add("section-subtitle");

        int age = p.getDateNaissance() == null ? 0
                : Period.between(p.getDateNaissance(), LocalDate.now()).getYears();
        Label ageLine = new Label(age + " ans  ·  "
                + (p.getGroupeSanguin() == null ? "?" : p.getGroupeSanguin().getLabel())
                + "  ·  " + (p.getServiceAdmission() == null ? "non affecté" : p.getServiceAdmission()));
        ageLine.getStyleClass().add("section-subtitle");

        HBox row = new HBox(14, avatar, new VBox(2, name, idLine, ageLine));
        row.setAlignment(Pos.CENTER_LEFT);
        card.getChildren().add(row);
        return card;
    }

    private VBox infoCard(Patient p) {
        VBox card = new VBox(8);
        card.getStyleClass().add("card");
        Label title = new Label("Informations");
        title.getStyleClass().add("section-title");

        GridPane g = new GridPane();
        g.setHgap(20); g.setVgap(8);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMMM yyyy", java.util.Locale.FRENCH);
        addRow(g, 0, "Nom complet",       p.getPrenom() + " " + p.getNom());
        addRow(g, 1, "Date de naissance", p.getDateNaissance() == null ? "—" : fmt.format(p.getDateNaissance()));
        addRow(g, 2, "Téléphone",         text(p.getTelephone(), "—"));
        addRow(g, 3, "Groupe sanguin",    p.getGroupeSanguin() == null ? "—" : p.getGroupeSanguin().getLabel());
        addRow(g, 4, "Service",           text(p.getServiceAdmission(), "non affecté"));

        card.getChildren().addAll(title, g);
        return card;
    }

    private void addRow(GridPane g, int row, String label, String value) {
        Label l = new Label(label.toUpperCase());
        l.getStyleClass().add("field-label");
        Label v = new Label(value);
        v.setStyle("-fx-font-size: 14px;");
        g.add(l, 0, row); g.add(v, 1, row);
    }

    @SuppressWarnings("unchecked")
    private VBox rdvHistoryCard(Patient p) {
        VBox card = new VBox(10);
        card.getStyleClass().add("card");
        Label title = new Label("Historique des rendez-vous");
        title.getStyleClass().add("section-title");

        List<RendezVous> history = rDao.listerPourPatient(p.getId(), mDao);

        Label badge = Components.statBadge(history.size()
                + (history.size() <= 1 ? " rendez-vous" : " rendez-vous"));
        HBox header = new HBox(10, title, badge);
        header.setAlignment(Pos.CENTER_LEFT);

        if (history.isEmpty()) {
            Label empty = new Label("Aucun rendez-vous enregistré pour ce patient.");
            empty.getStyleClass().add("section-subtitle");
            card.getChildren().addAll(header, new Separator(), empty);
            return card;
        }

        TableView<RendezVous> table = new TableView<>();
        TableColumn<RendezVous, String> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colId.setPrefWidth(90);

        TableColumn<RendezVous, LocalDateTime> colDate = new TableColumn<>("Date / heure");
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateHeure"));
        colDate.setPrefWidth(180);
        DateTimeFormatter dt = DateTimeFormatter.ofPattern("dd/MM/yyyy  HH:mm");
        colDate.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(LocalDateTime d, boolean empty) {
                super.updateItem(d, empty);
                setText(empty || d == null ? "" : dt.format(d));
            }
        });

        TableColumn<RendezVous, String> colMed = new TableColumn<>("Médecin");
        colMed.setCellValueFactory(new PropertyValueFactory<>("nomMedecin"));
        colMed.setPrefWidth(220);

        TableColumn<RendezVous, String> colSpe = new TableColumn<>("Spécialité");
        colSpe.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getMedecin() == null || c.getValue().getMedecin().getSpecialite() == null
                  ? "" : c.getValue().getMedecin().getSpecialite().name()));
        colSpe.setPrefWidth(150);

        TableColumn<RendezVous, String> colWhen = new TableColumn<>("État");
        colWhen.setCellValueFactory(c -> {
            LocalDateTime d = c.getValue().getDateHeure();
            return new javafx.beans.property.SimpleStringProperty(
                    d == null ? "?" : (d.isBefore(LocalDateTime.now()) ? "Passé" : "À venir"));
        });
        colWhen.setPrefWidth(100);

        table.getColumns().addAll(colId, colDate, colMed, colSpe, colWhen);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setItems(javafx.collections.FXCollections.observableArrayList(history));
        table.setPrefHeight(280);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        card.getChildren().addAll(header, table);
        return card;
    }

    private static String text(String s, String fallback) {
        return (s == null || s.isBlank()) ? fallback : s;
    }
}
