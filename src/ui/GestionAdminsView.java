package ui;

import dao.AdministrateurDao;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import model.Administrateur;
import ui.Components.ToastKind;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class GestionAdminsView extends VBox {

    private final AdministrateurDao dao = new AdministrateurDao();
    private final TableView<Administrateur> table = new TableView<>();
    private final ObservableList<Administrateur> source = FXCollections.observableArrayList();

    private final TextField idField = new TextField();
    private final TextField nomField = new TextField();
    private final TextField prenomField = new TextField();
    private final DatePicker datePicker = new DatePicker();
    private final TextField depField = new TextField();
    private final TextField posteField = new TextField();

    public GestionAdminsView() {
        setPadding(new Insets(24, 28, 24, 28));
        setSpacing(16);
        getChildren().addAll(buildHeader(), buildTableCard(), buildFormCard());
        rafraichirTable();
    }

    private final Label statBadge = Components.statBadge("0 administrateur");

    private HBox buildHeader() {
        Label title = new Label("Administrateurs");
        title.getStyleClass().add("section-title");
        Label sub = new Label("Personnel administratif et postes");
        sub.getStyleClass().add("section-subtitle");
        VBox titleBox = new VBox(2, title, sub);
        HBox.setHgrow(titleBox, Priority.ALWAYS);

        Button btnExport = Components.ghost("⤓  Exporter CSV");
        btnExport.setOnAction(e -> Components.exportCsv(table, "administrateurs.csv",
                getScene() == null ? null : getScene().getWindow()));

        HBox row = new HBox(12, titleBox, statBadge, btnExport);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    @SuppressWarnings("unchecked")
    private VBox buildTableCard() {
        TableColumn<Administrateur, String> colId = new TableColumn<>("ID");
        TableColumn<Administrateur, String> colNom = new TableColumn<>("Nom");
        TableColumn<Administrateur, String> colPrenom = new TableColumn<>("Prénom");
        TableColumn<Administrateur, LocalDate> colDate = new TableColumn<>("Naissance");
        TableColumn<Administrateur, String> colDep = new TableColumn<>("Département");
        TableColumn<Administrateur, String> colPoste = new TableColumn<>("Poste");

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateNaissance"));
        colDep.setCellValueFactory(new PropertyValueFactory<>("departement"));
        colPoste.setCellValueFactory(new PropertyValueFactory<>("poste"));

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
        colDep.setPrefWidth(150);
        colPoste.setPrefWidth(140);

        table.getColumns().addAll(colId, colNom, colPrenom, colDate, colDep, colPoste);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPrefHeight(280);

        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) remplirFormulaire(newSel);
        });

        TextField search = Components.attachSearch(table, source,
                a -> a.getId() + " " + a.getNom() + " " + a.getPrenom() + " "
                   + (a.getDepartement() == null ? "" : a.getDepartement())
                   + " " + (a.getPoste() == null ? "" : a.getPoste()));
        HBox toolbar = new HBox(10, search);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        VBox card = new VBox(12, toolbar, table);
        card.getStyleClass().add("card");
        return card;
    }

    private VBox buildFormCard() {
        Label title = new Label("Fiche administrateur");
        title.getStyleClass().add("section-title");
        Label sub = new Label("Création, modification et suppression du personnel administratif.");
        sub.getStyleClass().add("section-subtitle");

        idField.setDisable(true); idField.setPromptText("Généré automatiquement");
        nomField.setPromptText("Nom"); prenomField.setPromptText("Prénom");
        depField.setPromptText("Comptabilité, Accueil...");
        posteField.setPromptText("Responsable, Agent...");
        datePicker.setPromptText("jj/mm/aaaa");
        datePicker.setMaxWidth(Double.MAX_VALUE);

        HBox ligne1 = new HBox(12,
                Components.labeled("ID", idField),
                Components.labeled("Nom", nomField),
                Components.labeled("Prénom", prenomField));
        HBox ligne2 = new HBox(12,
                Components.labeled("Date de naissance", datePicker),
                Components.labeled("Département", depField),
                Components.labeled("Poste", posteField));

        Button btnAjouter   = Components.primary("➕  Ajouter");
        Button btnModifier  = Components.ghost("✎  Modifier");
        Button btnSupprimer = Components.danger("🗑  Supprimer");
        Button btnVider     = Components.ghost("Vider");

        btnAjouter.setOnAction(e -> {
            if (!valider()) return;
            try {
                dao.ajouter(new Administrateur(dao.genererProchainId(), nomField.getText().trim(),
                        prenomField.getText().trim(), datePicker.getValue(),
                        depField.getText(), posteField.getText()));
                rafraichirTable(); viderFormulaire();
                toast("Administrateur ajouté", ToastKind.SUCCESS);
            } catch (Exception ex) { toast("Erreur : " + ex.getMessage(), ToastKind.ERROR); }
        });
        btnModifier.setOnAction(e -> {
            if (idField.getText().isEmpty()) { toast("Sélectionnez un administrateur", ToastKind.ERROR); return; }
            if (!valider()) return;
            dao.modifier(new Administrateur(idField.getText(), nomField.getText().trim(),
                    prenomField.getText().trim(), datePicker.getValue(),
                    depField.getText(), posteField.getText()));
            rafraichirTable();
            toast("Administrateur mis à jour", ToastKind.SUCCESS);
        });
        btnSupprimer.setOnAction(e -> {
            if (idField.getText().isEmpty()) { toast("Sélectionnez un administrateur", ToastKind.ERROR); return; }
            if (!Components.confirm("Supprimer cet administrateur ?",
                    nomField.getText() + " " + prenomField.getText()
                  + "\nCette action est définitive.")) return;
            dao.supprimer(idField.getText());
            rafraichirTable(); viderFormulaire();
            toast("Administrateur supprimé", ToastKind.INFO);
        });
        btnVider.setOnAction(e -> viderFormulaire());

        HBox actions = new HBox(10, btnAjouter, btnModifier, btnSupprimer, btnVider);

        VBox card = new VBox(10, title, sub, ligne1, ligne2, actions);
        card.getStyleClass().add("card");
        return card;
    }

    private boolean valider() {
        Components.clearErrors(nomField, prenomField, depField, posteField);
        boolean ok = true;
        if (!Components.validateRequired(nomField))    ok = false;
        if (!Components.validateRequired(prenomField)) ok = false;
        if (datePicker.getValue() == null) { datePicker.getStyleClass().add("error"); ok = false; }
        else datePicker.getStyleClass().remove("error");
        if (!ok) toast("Veuillez remplir les champs obligatoires", ToastKind.ERROR);
        return ok;
    }

    private void rafraichirTable() {
        source.setAll(dao.lister());
        statBadge.setText(source.size() + (source.size() <= 1 ? " administrateur" : " administrateurs"));
    }

    private void remplirFormulaire(Administrateur a) {
        idField.setText(a.getId());
        nomField.setText(a.getNom());
        prenomField.setText(a.getPrenom());
        datePicker.setValue(a.getDateNaissance());
        depField.setText(a.getDepartement());
        posteField.setText(a.getPoste());
    }

    private void viderFormulaire() {
        idField.clear(); nomField.clear(); prenomField.clear();
        depField.clear(); posteField.clear(); datePicker.setValue(null);
        Components.clearErrors(nomField, prenomField, depField, posteField, datePicker);
        table.getSelectionModel().clearSelection();
    }

    private void toast(String msg, ToastKind k) {
        if (DashboardView.getToastHost() != null) Components.toast(DashboardView.getToastHost(), msg, k);
    }
}
