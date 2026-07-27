package ui;

import dao.MedecinDao;
import enums.Specialite;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import model.Medecin;
import ui.Components.ToastKind;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class GestionMedecinsView extends VBox {

    private final MedecinDao dao = new MedecinDao();
    private final TableView<Medecin> table = new TableView<>();
    private final ObservableList<Medecin> source = FXCollections.observableArrayList();

    private final TextField idField = new TextField();
    private final TextField nomField = new TextField();
    private final TextField prenomField = new TextField();
    private final DatePicker datePicker = new DatePicker();
    private final TextField matriculeField = new TextField();
    private final ComboBox<Specialite> speBox = new ComboBox<>();

    public GestionMedecinsView() {
        setPadding(new Insets(24, 28, 24, 28));
        setSpacing(16);
        getChildren().addAll(buildHeader(), buildTableCard(), buildFormCard());
        rafraichirTable();
    }

    private final Label statBadge = Components.statBadge("0 médecin");

    private HBox buildHeader() {
        Label title = new Label("Médecins");
        title.getStyleClass().add("section-title");
        Label sub = new Label("Spécialités, matricules et affectations");
        sub.getStyleClass().add("section-subtitle");
        VBox titleBox = new VBox(2, title, sub);
        HBox.setHgrow(titleBox, Priority.ALWAYS);

        Button btnExport = Components.ghost("⤓  Exporter CSV");
        btnExport.setOnAction(e -> Components.exportCsv(table, "medecins.csv",
                getScene() == null ? null : getScene().getWindow()));

        HBox row = new HBox(12, titleBox, statBadge, btnExport);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    @SuppressWarnings("unchecked")
    private VBox buildTableCard() {
        TableColumn<Medecin, String> colId = new TableColumn<>("ID");
        TableColumn<Medecin, String> colNom = new TableColumn<>("Nom");
        TableColumn<Medecin, String> colPrenom = new TableColumn<>("Prénom");
        TableColumn<Medecin, LocalDate> colDate = new TableColumn<>("Naissance");
        TableColumn<Medecin, Specialite> colSpe = new TableColumn<>("Spécialité");
        TableColumn<Medecin, String> colMat = new TableColumn<>("Matricule");

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateNaissance"));
        colSpe.setCellValueFactory(new PropertyValueFactory<>("specialite"));
        colMat.setCellValueFactory(new PropertyValueFactory<>("matricule"));

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        colDate.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(LocalDate d, boolean empty) {
                super.updateItem(d, empty);
                setText(empty || d == null ? "" : fmt.format(d));
            }
        });

        colId.setPrefWidth(70);
        colNom.setPrefWidth(150);
        colPrenom.setPrefWidth(150);
        colDate.setPrefWidth(120);
        colSpe.setPrefWidth(150);
        colMat.setPrefWidth(110);

        table.getColumns().addAll(colId, colNom, colPrenom, colDate, colSpe, colMat);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPrefHeight(280);

        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) remplirFormulaire(newSel);
        });

        TextField search = Components.attachSearch(table, source,
                m -> m.getId() + " " + m.getNom() + " " + m.getPrenom() + " "
                   + (m.getSpecialite() == null ? "" : m.getSpecialite().name())
                   + " " + (m.getMatricule() == null ? "" : m.getMatricule()));
        HBox toolbar = new HBox(10, search);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        VBox card = new VBox(12, toolbar, table);
        card.getStyleClass().add("card");
        return card;
    }

    private VBox buildFormCard() {
        Label title = new Label("Fiche médecin");
        title.getStyleClass().add("section-title");
        Label sub = new Label("Renseignez ou modifiez les informations du médecin.");
        sub.getStyleClass().add("section-subtitle");

        idField.setDisable(true); idField.setPromptText("Généré automatiquement");
        nomField.setPromptText("Nom"); prenomField.setPromptText("Prénom");
        matriculeField.setPromptText("MED-12345");
        speBox.getItems().addAll(Specialite.values());
        speBox.setPromptText("Sélectionner");
        speBox.setMaxWidth(Double.MAX_VALUE);
        datePicker.setPromptText("jj/mm/aaaa");
        datePicker.setMaxWidth(Double.MAX_VALUE);

        HBox ligne1 = new HBox(12,
                Components.labeled("ID", idField),
                Components.labeled("Nom", nomField),
                Components.labeled("Prénom", prenomField));
        HBox ligne2 = new HBox(12,
                Components.labeled("Date de naissance", datePicker),
                Components.labeled("Spécialité", speBox),
                Components.labeled("Matricule", matriculeField));

        Button btnAjouter   = Components.primary("➕  Ajouter");
        Button btnModifier  = Components.ghost("✎  Modifier");
        Button btnSupprimer = Components.danger("🗑  Supprimer");
        Button btnVider     = Components.ghost("Vider");

        btnAjouter.setOnAction(e -> {
            if (!valider()) return;
            try {
                Medecin m = new Medecin(dao.genererProchainId(), nomField.getText().trim(),
                        prenomField.getText().trim(), datePicker.getValue(),
                        speBox.getValue(), matriculeField.getText());
                dao.ajouterMedecin(m);
                rafraichirTable(); viderFormulaire();
                toast("Médecin ajouté", ToastKind.SUCCESS);
            } catch (Exception ex) { toast("Erreur : " + ex.getMessage(), ToastKind.ERROR); }
        });
        btnModifier.setOnAction(e -> {
            if (idField.getText().isEmpty()) { toast("Sélectionnez un médecin", ToastKind.ERROR); return; }
            if (!valider()) return;
            try {
                Medecin m = new Medecin(idField.getText(), nomField.getText().trim(),
                        prenomField.getText().trim(), datePicker.getValue(),
                        speBox.getValue(), matriculeField.getText());
                dao.modifierMedecin(m);
                rafraichirTable();
                toast("Médecin mis à jour", ToastKind.SUCCESS);
            } catch (Exception ex) { toast("Erreur : " + ex.getMessage(), ToastKind.ERROR); }
        });
        btnSupprimer.setOnAction(e -> {
            if (idField.getText().isEmpty()) { toast("Sélectionnez un médecin", ToastKind.ERROR); return; }
            if (!Components.confirm("Supprimer ce médecin ?",
                    "Dr. " + nomField.getText() + " " + prenomField.getText()
                  + "\nCette action est définitive.")) return;
            dao.supprimerMedecin(idField.getText());
            rafraichirTable(); viderFormulaire();
            toast("Médecin supprimé", ToastKind.INFO);
        });
        btnVider.setOnAction(e -> viderFormulaire());

        HBox actions = new HBox(10, btnAjouter, btnModifier, btnSupprimer, btnVider);

        VBox card = new VBox(10, title, sub, ligne1, ligne2, actions);
        card.getStyleClass().add("card");
        return card;
    }

    private boolean valider() {
        Components.clearErrors(nomField, prenomField, matriculeField);
        boolean ok = true;
        if (!Components.validateRequired(nomField))    ok = false;
        if (!Components.validateRequired(prenomField)) ok = false;
        if (datePicker.getValue() == null) { datePicker.getStyleClass().add("error"); ok = false; }
        else datePicker.getStyleClass().remove("error");
        if (speBox.getValue() == null) { speBox.getStyleClass().add("error"); ok = false; }
        else speBox.getStyleClass().remove("error");
        if (!ok) toast("Veuillez remplir les champs obligatoires", ToastKind.ERROR);
        return ok;
    }

    private void rafraichirTable() {
        source.setAll(dao.listerMedecins());
        statBadge.setText(source.size() + (source.size() <= 1 ? " médecin" : " médecins"));
    }

    private void remplirFormulaire(Medecin m) {
        idField.setText(m.getId());
        nomField.setText(m.getNom());
        prenomField.setText(m.getPrenom());
        datePicker.setValue(m.getDateNaissance());
        matriculeField.setText(m.getMatricule());
        speBox.setValue(m.getSpecialite());
    }

    private void viderFormulaire() {
        idField.clear(); nomField.clear(); prenomField.clear(); matriculeField.clear();
        datePicker.setValue(null); speBox.setValue(null);
        Components.clearErrors(nomField, prenomField, matriculeField, datePicker, speBox);
        table.getSelectionModel().clearSelection();
    }

    private void toast(String msg, ToastKind k) {
        if (DashboardView.getToastHost() != null) Components.toast(DashboardView.getToastHost(), msg, k);
    }
}
