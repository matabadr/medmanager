package ui;

import dao.Audit;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AuditView extends VBox {

    private final TableView<Audit.Entry> table = new TableView<>();
    private final ObservableList<Audit.Entry> source = FXCollections.observableArrayList();
    private final Label statBadge = Components.statBadge("0 entrée");

    public AuditView() {
        setPadding(new Insets(24, 28, 24, 28));
        setSpacing(16);
        getChildren().addAll(buildHeader(), buildTableCard());
        rafraichir();
    }

    private HBox buildHeader() {
        Label title = new Label("Journal d'audit");
        title.getStyleClass().add("section-title");
        Label sub = new Label("Trace des opérations (création, modification, suppression, connexion)");
        sub.getStyleClass().add("section-subtitle");
        VBox titleBox = new VBox(2, title, sub);
        HBox.setHgrow(titleBox, Priority.ALWAYS);

        Button btnRefresh = Components.ghost("⟳  Rafraîchir");
        btnRefresh.setOnAction(e -> rafraichir());
        Button btnExport = Components.ghost("⤓  Exporter CSV");
        btnExport.setOnAction(e -> Components.exportCsv(table, "audit-log.csv",
                getScene() == null ? null : getScene().getWindow()));

        HBox row = new HBox(12, titleBox, statBadge, btnRefresh, btnExport);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    @SuppressWarnings("unchecked")
    private VBox buildTableCard() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

        TableColumn<Audit.Entry, LocalDateTime> colTs = new TableColumn<>("Date / heure");
        colTs.setCellValueFactory(new PropertyValueFactory<>("ts"));
        colTs.setPrefWidth(160);
        colTs.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(LocalDateTime d, boolean empty) {
                super.updateItem(d, empty);
                setText(empty || d == null ? "" : fmt.format(d));
            }
        });

        TableColumn<Audit.Entry, String> colUser = new TableColumn<>("Utilisateur");
        colUser.setCellValueFactory(new PropertyValueFactory<>("username"));
        colUser.setPrefWidth(110);

        TableColumn<Audit.Entry, String> colRole = new TableColumn<>("Rôle");
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        colRole.setPrefWidth(100);

        TableColumn<Audit.Entry, String> colAct = new TableColumn<>("Action");
        colAct.setCellValueFactory(new PropertyValueFactory<>("action"));
        colAct.setPrefWidth(90);

        TableColumn<Audit.Entry, String> colTable = new TableColumn<>("Table");
        colTable.setCellValueFactory(new PropertyValueFactory<>("targetTable"));
        colTable.setPrefWidth(130);

        TableColumn<Audit.Entry, String> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("targetId"));
        colId.setPrefWidth(110);

        TableColumn<Audit.Entry, String> colDetails = new TableColumn<>("Détails");
        colDetails.setCellValueFactory(new PropertyValueFactory<>("details"));
        colDetails.setPrefWidth(260);

        table.getColumns().addAll(colTs, colUser, colRole, colAct, colTable, colId, colDetails);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPrefHeight(520);

        TextField search = Components.attachSearch(table, source,
                e -> (e.ts() == null ? "" : fmt.format(e.ts()))
                   + " " + n(e.username()) + " " + n(e.role()) + " " + n(e.action())
                   + " " + n(e.targetTable()) + " " + n(e.targetId()) + " " + n(e.details()));
        HBox toolbar = new HBox(10, search);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        VBox card = new VBox(12, toolbar, table);
        card.getStyleClass().add("card");
        return card;
    }

    private static String n(String s) { return s == null ? "" : s; }

    private void rafraichir() {
        source.setAll(Audit.recent(500));
        statBadge.setText(source.size() + (source.size() <= 1 ? " entrée" : " entrées"));
    }
}
