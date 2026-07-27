package ui;

import dao.Audit;
import dao.UtilisateurDao;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import model.Utilisateur;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Profile / status view of the currently logged-in user.
 * Shows account info, last connection, action counters,
 * and a table of recent actions filtered by username.
 */
public class UserProfileView extends VBox {

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("dd MMM yyyy 'à' HH:mm", java.util.Locale.FRENCH);
    private static final DateTimeFormatter FMT_TS =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    public UserProfileView(Runnable onBack) {
        setPadding(new Insets(24, 28, 24, 28));
        setSpacing(16);

        Utilisateur me = Session.current();
        if (me == null) {
            getChildren().add(new Label("Aucune session active."));
            return;
        }
        // Refresh from DB in case derniere_connexion changed
        Utilisateur fresh = new UtilisateurDao().findByUsername(me.getUsername()).orElse(me);

        Button back = Components.ghost("←  Retour");
        back.setOnAction(e -> { if (onBack != null) onBack.run(); });

        Label title = new Label("Mon profil");
        title.getStyleClass().add("section-title");
        Label sub = new Label("Informations du compte, statut et activité récente.");
        sub.getStyleClass().add("section-subtitle");
        VBox titleBox = new VBox(2, title, sub);
        HBox.setHgrow(titleBox, Priority.ALWAYS);

        HBox header = new HBox(12, back, titleBox);
        header.setAlignment(Pos.CENTER_LEFT);

        HBox row1 = new HBox(16, identityCard(fresh), accountCard(fresh));
        HBox.setHgrow(row1.getChildren().get(0), Priority.ALWAYS);
        HBox.setHgrow(row1.getChildren().get(1), Priority.ALWAYS);

        VBox countersCard = activityCountersCard(fresh);
        VBox historyCard = recentActivityCard(fresh);

        getChildren().addAll(header, row1, countersCard, historyCard);
    }

    /* ---------- identity card ---------- */

    private VBox identityCard(Utilisateur u) {
        VBox card = new VBox(10);
        card.getStyleClass().add("card");

        String initials = initialsOf(u.getDisplayName());
        Label avatar = new Label(initials);
        avatar.setStyle(
                "-fx-min-width: 84; -fx-min-height: 84; -fx-max-width: 84; -fx-max-height: 84;"
              + " -fx-background-color: linear-gradient(to bottom right, #14b8b7, #0b8483);"
              + " -fx-background-radius: 999; -fx-alignment: center;"
              + " -fx-text-fill: white; -fx-font-size: 30px; -fx-font-weight: 800;"
              + " -fx-effect: dropshadow(gaussian, rgba(11,132,131,0.40), 16, 0, 0, 6);");

        Label name = new Label(u.getDisplayName());
        name.getStyleClass().add("section-title");
        Label idLine = new Label("@" + u.getUsername());
        idLine.setStyle("-fx-text-fill: #64748b; -fx-font-size: 13px;");

        Label roleChip = new Label(roleLabel(u));
        roleChip.getStyleClass().add("topbar-pill");
        roleChip.setStyle("-fx-font-size: 11px;");

        Label statusDot = new Label("●");
        statusDot.setStyle("-fx-text-fill: " + (u.isActif() ? "#16a34a" : "#dc2626") + "; -fx-font-size: 14px;");
        Label statusLbl = new Label(u.isActif() ? "Compte actif" : "Compte désactivé");
        statusLbl.setStyle("-fx-text-fill: " + (u.isActif() ? "#16a34a" : "#dc2626") + ";"
                + " -fx-font-weight: 700; -fx-font-size: 12px;");
        HBox status = new HBox(6, statusDot, statusLbl);
        status.setAlignment(Pos.CENTER_LEFT);

        HBox chips = new HBox(8, roleChip, status);
        chips.setAlignment(Pos.CENTER_LEFT);

        VBox text = new VBox(4, name, idLine, chips);
        HBox row = new HBox(18, avatar, text);
        row.setAlignment(Pos.CENTER_LEFT);
        card.getChildren().add(row);
        return card;
    }

    /* ---------- account card ---------- */

    private VBox accountCard(Utilisateur u) {
        VBox card = new VBox(8);
        card.getStyleClass().add("card");
        Label t = new Label("Informations du compte");
        t.getStyleClass().add("section-title");

        GridPane g = new GridPane();
        g.setHgap(20); g.setVgap(8);

        addRow(g, 0, "Nom complet",
                u.getNomComplet() == null ? "—" : u.getNomComplet());
        addRow(g, 1, "Identifiant", u.getUsername());
        addRow(g, 2, "Rôle", roleLabel(u));
        addRow(g, 3, "Statut", u.isActif() ? "Actif" : "Désactivé");
        addRow(g, 4, "Dernière connexion",
                u.getDerniereConnexion() == null ? "Jamais" : FMT.format(u.getDerniereConnexion()));
        addRow(g, 5, "Session en cours",
                Session.current() != null && Session.current().getUsername().equals(u.getUsername())
                        ? "Oui — depuis ce poste" : "Non");

        card.getChildren().addAll(t, g);
        return card;
    }

    private void addRow(GridPane g, int row, String label, String value) {
        Label l = new Label(label.toUpperCase());
        l.getStyleClass().add("field-label");
        Label v = new Label(value);
        v.setStyle("-fx-font-size: 14px;");
        g.add(l, 0, row); g.add(v, 1, row);
    }

    /* ---------- activity counters card ---------- */

    private VBox activityCountersCard(Utilisateur u) {
        VBox card = new VBox(10);
        card.getStyleClass().add("card");
        Label t = new Label("Mes actions");
        t.getStyleClass().add("section-title");

        Map<String, Integer> counts = Audit.countActionsForUser(u.getUsername());

        HBox tiles = new HBox(14);
        tiles.setAlignment(Pos.CENTER_LEFT);
        tiles.getChildren().addAll(
                miniTile("Créations",   counts.getOrDefault("CREATE", 0), "#0ea5a4"),
                miniTile("Modifications", counts.getOrDefault("UPDATE", 0), "#3b82f6"),
                miniTile("Suppressions",  counts.getOrDefault("DELETE", 0), "#dc2626"),
                miniTile("Connexions",    counts.getOrDefault("LOGIN",  0), "#16a34a"),
                miniTile("Déconnexions",  counts.getOrDefault("LOGOUT", 0), "#64748b")
        );
        card.getChildren().addAll(t, tiles);
        return card;
    }

    private VBox miniTile(String label, int value, String colorHex) {
        VBox box = new VBox(2);
        box.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 12; -fx-padding: 14 18 14 18;"
                + " -fx-border-color: " + colorHex + "; -fx-border-width: 0 0 0 4;"
                + " -fx-border-radius: 12;");
        Label l = new Label(label.toUpperCase());
        l.getStyleClass().add("kpi-label");
        Label v = new Label(String.valueOf(value));
        v.setStyle("-fx-font-size: 22px; -fx-font-weight: 800; -fx-text-fill: " + colorHex + ";");
        box.getChildren().addAll(l, v);
        box.setMinWidth(140);
        HBox.setHgrow(box, Priority.ALWAYS);
        return box;
    }

    /* ---------- recent activity table ---------- */

    @SuppressWarnings("unchecked")
    private VBox recentActivityCard(Utilisateur u) {
        VBox card = new VBox(10);
        card.getStyleClass().add("card");

        List<Audit.Entry> history = Audit.recentForUser(u.getUsername(), 100);

        Label t = new Label("Activité récente");
        t.getStyleClass().add("section-title");
        Label badge = Components.statBadge(history.size()
                + (history.size() <= 1 ? " entrée" : " entrées"));
        HBox head = new HBox(10, t, badge);
        head.setAlignment(Pos.CENTER_LEFT);

        if (history.isEmpty()) {
            Label empty = new Label("Vous n'avez encore effectué aucune action enregistrée.");
            empty.getStyleClass().add("section-subtitle");
            card.getChildren().addAll(head, empty);
            return card;
        }

        TableView<Audit.Entry> table = new TableView<>();

        TableColumn<Audit.Entry, LocalDateTime> colTs = new TableColumn<>("Date / heure");
        colTs.setCellValueFactory(new PropertyValueFactory<>("ts"));
        colTs.setPrefWidth(160);
        colTs.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(LocalDateTime d, boolean empty) {
                super.updateItem(d, empty);
                setText(empty || d == null ? "" : FMT_TS.format(d));
            }
        });

        TableColumn<Audit.Entry, String> colAct = new TableColumn<>("Action");
        colAct.setCellValueFactory(new PropertyValueFactory<>("action"));
        colAct.setPrefWidth(100);

        TableColumn<Audit.Entry, String> colTbl = new TableColumn<>("Cible");
        colTbl.setCellValueFactory(new PropertyValueFactory<>("targetTable"));
        colTbl.setPrefWidth(140);

        TableColumn<Audit.Entry, String> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("targetId"));
        colId.setPrefWidth(110);

        TableColumn<Audit.Entry, String> colDet = new TableColumn<>("Détails");
        colDet.setCellValueFactory(new PropertyValueFactory<>("details"));
        colDet.setPrefWidth(280);

        table.getColumns().addAll(colTs, colAct, colTbl, colId, colDet);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setItems(FXCollections.observableArrayList(history));
        table.setPrefHeight(320);

        card.getChildren().addAll(head, table);
        return card;
    }

    /* ---------- helpers ---------- */

    private static String roleLabel(Utilisateur u) {
        return switch (u.getRole()) {
            case ADMIN     -> "Administrateur";
            case MEDECIN   -> "Médecin";
            case INFIRMIER -> "Infirmier";
            case PATIENT   -> "Patient";
        };
    }

    private static String initialsOf(String s) {
        if (s == null || s.isBlank()) return "?";
        String[] parts = s.trim().split("\\s+");
        if (parts.length == 1) return parts[0].substring(0, 1).toUpperCase();
        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
    }
}
