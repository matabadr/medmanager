package ui;

import dao.PatientDao;
import enums.GroupeSanguin;
import exceptions.MedManagerException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import model.Patient;
import ui.Components.ToastKind;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class GestionPatientsView extends VBox {

    private final PatientDao dao = new PatientDao();
    private final TableView<Patient> table = new TableView<>();
    private final ObservableList<Patient> source = FXCollections.observableArrayList();

    private final TextField idField = new TextField();
    private final TextField nomField = new TextField();
    private final TextField prenomField = new TextField();
    private final DatePicker datePicker = new DatePicker();
    private final TextField telField = new TextField();
    private final ComboBox<GroupeSanguin> groupeBox = new ComboBox<>();

    public GestionPatientsView() {
        setPadding(new Insets(24, 28, 24, 28));
        setSpacing(16);

        getChildren().addAll(buildHeader(), buildTableCard(), buildFormCard());
        rafraichirTable();
    }

    /* ---------- header ---------- */

    private final Label statBadge = Components.statBadge("0 patient");

    private HBox buildHeader() {
        Label title = new Label("Patients");
        title.getStyleClass().add("section-title");
        Label sub = new Label("Liste, ajout, modification et suppression");
        sub.getStyleClass().add("section-subtitle");

        VBox titleBox = new VBox(2, title, sub);
        HBox.setHgrow(titleBox, Priority.ALWAYS);

        Button btnExport = Components.ghost("⤓  Exporter CSV");
        btnExport.setOnAction(e -> Components.exportCsv(table, "patients.csv",
                getScene() == null ? null : getScene().getWindow()));

        HBox box = new HBox(12, titleBox, statBadge, btnExport);
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    /* ---------- table card ---------- */

    @SuppressWarnings("unchecked")
    private VBox buildTableCard() {
        TableColumn<Patient, String> colId     = new TableColumn<>("ID");
        TableColumn<Patient, String> colNom    = new TableColumn<>("Nom");
        TableColumn<Patient, String> colPrenom = new TableColumn<>("Prénom");
        TableColumn<Patient, LocalDate> colDate = new TableColumn<>("Naissance");
        TableColumn<Patient, String> colTel    = new TableColumn<>("Téléphone");
        TableColumn<Patient, GroupeSanguin> colGr = new TableColumn<>("Groupe");

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateNaissance"));
        colTel.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        colGr.setCellValueFactory(new PropertyValueFactory<>("groupeSanguin"));

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
        colTel.setPrefWidth(140);
        colGr.setPrefWidth(90);

        table.getColumns().addAll(colId, colNom, colPrenom, colDate, colTel, colGr);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPrefHeight(280);

        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) remplirFormulaire(newSel);
        });
        // Double-click row → open detail view
        table.setRowFactory(tv -> {
            javafx.scene.control.TableRow<Patient> r = new javafx.scene.control.TableRow<>();
            r.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !r.isEmpty()) {
                    DashboardView host = lookupDashboard();
                    if (host != null) host.openPatientDetail(r.getItem());
                }
            });
            return r;
        });

        TextField search = Components.attachSearch(table, source,
                p -> p.getId() + " " + p.getNom() + " " + p.getPrenom() + " "
                   + (p.getTelephone() == null ? "" : p.getTelephone()));

        HBox toolbar = new HBox(10, search);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        VBox card = new VBox(12, toolbar, table);
        card.getStyleClass().add("card");
        return card;
    }

    /* ---------- form card ---------- */

    private VBox buildFormCard() {
        Label title = new Label("Fiche patient");
        title.getStyleClass().add("section-title");
        Label sub = new Label("Sélectionnez une ligne du tableau pour modifier, ou remplissez pour ajouter.");
        sub.getStyleClass().add("section-subtitle");

        idField.setPromptText("Généré automatiquement");
        idField.setDisable(true);
        nomField.setPromptText("Dupont");
        prenomField.setPromptText("Marie");
        telField.setPromptText("06 12 34 56 78");
        groupeBox.getItems().addAll(GroupeSanguin.values());
        groupeBox.setPromptText("Sélectionner");
        groupeBox.setMaxWidth(Double.MAX_VALUE);
        datePicker.setPromptText("jj/mm/aaaa");
        datePicker.setMaxWidth(Double.MAX_VALUE);

        HBox ligne1 = new HBox(12,
                Components.labeled("ID", idField),
                Components.labeled("Nom", nomField),
                Components.labeled("Prénom", prenomField));
        HBox ligne2 = new HBox(12,
                Components.labeled("Date de naissance", datePicker),
                Components.labeled("Téléphone", telField),
                Components.labeled("Groupe sanguin", groupeBox));

        Button btnAjouter   = Components.primary("➕  Ajouter");
        Button btnModifier  = Components.ghost("✎  Modifier");
        Button btnSupprimer = Components.danger("🗑  Supprimer");
        Button btnVider     = Components.ghost("Vider");

        btnAjouter.setOnAction(e -> ajouter());
        btnModifier.setOnAction(e -> modifier());
        btnSupprimer.setOnAction(e -> supprimer());
        btnVider.setOnAction(e -> viderFormulaire());

        HBox actions = new HBox(10, btnAjouter, btnModifier, btnSupprimer, btnVider);
        actions.setAlignment(Pos.CENTER_LEFT);

        VBox card = new VBox(10, title, sub, ligne1, ligne2, actions);
        card.getStyleClass().add("card");
        return card;
    }

    /* ---------- actions ---------- */

    private void ajouter() {
        if (!valider()) return;
        try {
            Patient p = new Patient(dao.genererProchainId(), nomField.getText().trim(),
                    prenomField.getText().trim(), datePicker.getValue());
            p.setTelephone(telField.getText());
            p.setGroupeSanguin(groupeBox.getValue());
            dao.ajouterPatient(p);
            rafraichirTable();
            viderFormulaire();
            toast("Patient ajouté avec succès", ToastKind.SUCCESS);
        } catch (MedManagerException ex) {
            toast(ex.getMessage(), ToastKind.ERROR);
        } catch (Exception ex) {
            toast("Erreur : " + ex.getMessage(), ToastKind.ERROR);
        }
    }

    private void modifier() {
        if (idField.getText() == null || idField.getText().isEmpty()) {
            toast("Sélectionnez d'abord un patient", ToastKind.ERROR);
            return;
        }
        if (!valider()) return;
        try {
            Patient p = new Patient(idField.getText(), nomField.getText().trim(),
                    prenomField.getText().trim(), datePicker.getValue());
            p.setTelephone(telField.getText());
            p.setGroupeSanguin(groupeBox.getValue());
            dao.modifierPatient(p);
            rafraichirTable();
            toast("Patient mis à jour", ToastKind.SUCCESS);
        } catch (Exception ex) {
            toast("Erreur : " + ex.getMessage(), ToastKind.ERROR);
        }
    }

    private void supprimer() {
        if (idField.getText() == null || idField.getText().isEmpty()) {
            toast("Sélectionnez d'abord un patient", ToastKind.ERROR);
            return;
        }
        if (!Components.confirm("Supprimer ce patient ?",
                "Patient " + idField.getText() + " — " + nomField.getText() + " " + prenomField.getText()
              + "\nCette action est définitive.")) return;
        dao.supprimerPatient(idField.getText());
        rafraichirTable();
        viderFormulaire();
        toast("Patient supprimé", ToastKind.INFO);
    }

    private boolean valider() {
        Components.clearErrors(nomField, prenomField, telField);
        Components.markError(groupeBox, false);
        boolean ok = true;
        if (!Components.validateRequired(nomField))    ok = false;
        if (!Components.validateRequired(prenomField)) ok = false;
        if (datePicker.getValue() == null) {
            datePicker.getStyleClass().add("error");
            ok = false;
        } else {
            datePicker.getStyleClass().remove("error");
        }
        if (!ok) toast("Veuillez remplir les champs obligatoires", ToastKind.ERROR);
        return ok;
    }

    private void rafraichirTable() {
        source.setAll(dao.listerPatients());
        statBadge.setText(source.size() + (source.size() <= 1 ? " patient" : " patients"));
    }

    private void remplirFormulaire(Patient p) {
        idField.setText(p.getId());
        nomField.setText(p.getNom());
        prenomField.setText(p.getPrenom());
        datePicker.setValue(p.getDateNaissance());
        telField.setText(p.getTelephone());
        groupeBox.setValue(p.getGroupeSanguin());
    }

    private void viderFormulaire() {
        idField.clear(); nomField.clear(); prenomField.clear();
        telField.clear(); datePicker.setValue(null); groupeBox.setValue(null);
        Components.clearErrors(nomField, prenomField, telField, datePicker);
        table.getSelectionModel().clearSelection();
    }

    private void toast(String msg, ToastKind kind) {
        if (DashboardView.getToastHost() != null) {
            Components.toast(DashboardView.getToastHost(), msg, kind);
        }
    }

    private DashboardView lookupDashboard() {
        return DashboardView.activeDashboard();
    }
}
