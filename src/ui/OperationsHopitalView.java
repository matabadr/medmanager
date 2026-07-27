package ui;

import dao.MedecinDao;
import dao.PatientDao;
import dao.RendezVousDao;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import model.Medecin;
import model.Patient;
import model.RendezVous;
import ui.Components.ToastKind;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class OperationsHopitalView extends TabPane {

    private final PatientDao pDao = new PatientDao();
    private final MedecinDao mDao = new MedecinDao();
    private final RendezVousDao rDao = new RendezVousDao();

    public OperationsHopitalView() {
        Tab tabAgenda = new Tab("🗓  Agenda", new CalendarView());
        tabAgenda.setClosable(false);
        Tab tabRdv = new Tab("📅  Rendez-vous", scrolled(buildRdv()));
        tabRdv.setClosable(false);
        Tab tabServices = new Tab("🏥  Affectation Services", scrolled(buildServices()));
        tabServices.setClosable(false);
        getTabs().addAll(tabAgenda, tabRdv, tabServices);
    }

    private javafx.scene.control.ScrollPane scrolled(javafx.scene.Node n) {
        javafx.scene.control.ScrollPane sp = new javafx.scene.control.ScrollPane(n);
        sp.setFitToWidth(true);
        sp.setHbarPolicy(javafx.scene.control.ScrollPane.ScrollBarPolicy.NEVER);
        sp.setVbarPolicy(javafx.scene.control.ScrollPane.ScrollBarPolicy.AS_NEEDED);
        sp.getStyleClass().add("scroll-pane-transparent");
        sp.setStyle("-fx-background-color: transparent;");
        return sp;
    }

    /* ---------------- RDV tab ---------------- */

    @SuppressWarnings("unchecked")
    private VBox buildRdv() {
        VBox box = new VBox(16);
        box.setPadding(new Insets(24, 28, 24, 28));

        Label title = new Label("Planification des rendez-vous");
        title.getStyleClass().add("section-title");
        Label sub = new Label("Affecte un patient à un médecin à une date et heure.");
        sub.getStyleClass().add("section-subtitle");

        TableView<RendezVous> table = new TableView<>();
        TableColumn<RendezVous, String> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id")); colId.setPrefWidth(90);
        TableColumn<RendezVous, String> colPat = new TableColumn<>("Patient");
        colPat.setCellValueFactory(new PropertyValueFactory<>("nomPatient")); colPat.setPrefWidth(200);
        TableColumn<RendezVous, String> colMed = new TableColumn<>("Médecin");
        colMed.setCellValueFactory(new PropertyValueFactory<>("nomMedecin")); colMed.setPrefWidth(200);
        TableColumn<RendezVous, LocalDateTime> colDate = new TableColumn<>("Date & heure");
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateHeure")); colDate.setPrefWidth(200);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy  HH:mm");
        colDate.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(LocalDateTime d, boolean empty) {
                super.updateItem(d, empty);
                setText(empty || d == null ? "" : fmt.format(d));
            }
        });

        table.getColumns().addAll(colId, colPat, colMed, colDate);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPrefHeight(280);

        ComboBox<Patient> comboPat = new ComboBox<>(FXCollections.observableArrayList(pDao.listerPatients()));
        comboPat.setMaxWidth(Double.MAX_VALUE);
        comboPat.setConverter(new StringConverter<>() {
            public String toString(Patient p) { return p == null ? "" : p.getNom() + " " + p.getPrenom() + "  (" + p.getId() + ")"; }
            public Patient fromString(String s) { return null; }
        });
        comboPat.setPromptText("Choisir un patient");

        ComboBox<Medecin> comboMed = new ComboBox<>(FXCollections.observableArrayList(mDao.listerMedecins()));
        comboMed.setMaxWidth(Double.MAX_VALUE);
        comboMed.setConverter(new StringConverter<>() {
            public String toString(Medecin m) { return m == null ? "" : "Dr. " + m.getNom() + " " + m.getPrenom()
                    + (m.getSpecialite() == null ? "" : " · " + m.getSpecialite().name()); }
            public Medecin fromString(String s) { return null; }
        });
        comboMed.setPromptText("Choisir un médecin");

        DatePicker datePicker = new DatePicker(LocalDate.now());
        datePicker.setMaxWidth(Double.MAX_VALUE);

        Spinner<Integer> hourSpin = new Spinner<>(0, 23, 9);
        hourSpin.setEditable(true);
        Spinner<Integer> minSpin  = new Spinner<>(0, 59, 0, 5);
        minSpin.setEditable(true);
        hourSpin.setPrefWidth(80); minSpin.setPrefWidth(80);
        HBox time = new HBox(6, hourSpin, new Label(":"), minSpin);
        time.setAlignment(Pos.CENTER_LEFT);

        Button btnPlanifier = Components.primary("📅  Planifier");
        Button btnActualiser = Components.ghost("Actualiser");

        btnPlanifier.setOnAction(e -> {
            Components.markError(comboPat, false);
            Components.markError(comboMed, false);
            if (comboPat.getValue() == null) { Components.markError(comboPat, true); toast(box, "Sélectionnez un patient", ToastKind.ERROR); return; }
            if (comboMed.getValue() == null) { Components.markError(comboMed, true); toast(box, "Sélectionnez un médecin", ToastKind.ERROR); return; }
            if (datePicker.getValue() == null) { toast(box, "Sélectionnez une date", ToastKind.ERROR); return; }

            LocalDateTime dt = LocalDateTime.of(datePicker.getValue(),
                    LocalTime.of(hourSpin.getValue(), minSpin.getValue()));
            try {
                rDao.ajouter(comboPat.getValue().getId(), comboMed.getValue().getId(), Timestamp.valueOf(dt));
                table.setItems(FXCollections.observableArrayList(rDao.lister(pDao, mDao)));
                toast(box, "Rendez-vous planifié", ToastKind.SUCCESS);
            } catch (Exception ex) {
                toast(box, "Erreur de planification : " + ex.getMessage(), ToastKind.ERROR);
            }
        });

        btnActualiser.setOnAction(e -> {
            comboPat.setItems(FXCollections.observableArrayList(pDao.listerPatients()));
            comboMed.setItems(FXCollections.observableArrayList(mDao.listerMedecins()));
            table.setItems(FXCollections.observableArrayList(rDao.lister(pDao, mDao)));
            toast(box, "Données rechargées", ToastKind.INFO);
        });

        HBox ligne1 = new HBox(12,
                Components.labeled("Patient", comboPat),
                Components.labeled("Médecin", comboMed));
        HBox.setHgrow(ligne1, Priority.ALWAYS);
        HBox ligne2 = new HBox(12,
                Components.labeled("Date", datePicker),
                Components.labeled("Heure", time));
        HBox actions = new HBox(10, btnPlanifier, btnActualiser);
        actions.setAlignment(Pos.CENTER_LEFT);

        VBox formCard = new VBox(10, title, sub, ligne1, ligne2, actions);
        formCard.getStyleClass().add("card");

        VBox tableCard = new VBox(12, table);
        tableCard.getStyleClass().add("card");

        table.setItems(FXCollections.observableArrayList(rDao.lister(pDao, mDao)));

        box.getChildren().addAll(tableCard, formCard);
        return box;
    }

    /* ---------------- Services tab ---------------- */

    private VBox buildServices() {
        VBox box = new VBox(16);
        box.setPadding(new Insets(24, 28, 24, 28));

        Label title = new Label("Affectation aux services");
        title.getStyleClass().add("section-title");
        Label sub = new Label("Assigner un patient ou un médecin à un service hospitalier.");
        sub.getStyleClass().add("section-subtitle");

        ComboBox<String> comboService = new ComboBox<>(FXCollections.observableArrayList(
                "Cardiologie", "Urgences", "Pédiatrie", "Chirurgie", "Neurologie",
                "Radiologie", "Psychiatrie", "Dermatologie", "Laboratoire", "Administration"));
        comboService.setPromptText("Sélectionner un service");
        comboService.setMaxWidth(Double.MAX_VALUE);

        ComboBox<Patient> comboPat = new ComboBox<>(FXCollections.observableArrayList(pDao.listerPatients()));
        comboPat.setPromptText("Patient");
        comboPat.setMaxWidth(Double.MAX_VALUE);
        comboPat.setConverter(new StringConverter<>() {
            public String toString(Patient p) { return p == null ? "" : p.getNom() + " " + p.getPrenom() + "  (" + p.getId() + ")"; }
            public Patient fromString(String s) { return null; }
        });

        ComboBox<Medecin> comboMed = new ComboBox<>(FXCollections.observableArrayList(mDao.listerMedecins()));
        comboMed.setPromptText("Médecin");
        comboMed.setMaxWidth(Double.MAX_VALUE);
        comboMed.setConverter(new StringConverter<>() {
            public String toString(Medecin m) { return m == null ? "" : "Dr. " + m.getNom() + " " + m.getPrenom(); }
            public Medecin fromString(String s) { return null; }
        });

        Button btnAffecterPat = Components.primary("Affecter le patient");
        Button btnAffecterMed = Components.primary("Affecter le médecin");

        btnAffecterPat.setOnAction(e -> {
            if (comboService.getValue() == null || comboPat.getValue() == null) {
                toast(box, "Sélectionnez un service et un patient", ToastKind.ERROR); return;
            }
            pDao.affecterService(comboPat.getValue().getId(), comboService.getValue());
            toast(box, comboPat.getValue().getNom() + " affecté à " + comboService.getValue(), ToastKind.SUCCESS);
        });
        btnAffecterMed.setOnAction(e -> {
            if (comboService.getValue() == null || comboMed.getValue() == null) {
                toast(box, "Sélectionnez un service et un médecin", ToastKind.ERROR); return;
            }
            mDao.affecterService(comboMed.getValue().getId(), comboService.getValue());
            toast(box, "Dr " + comboMed.getValue().getNom() + " affecté à " + comboService.getValue(), ToastKind.SUCCESS);
        });

        VBox card = new VBox(14,
                title, sub,
                Components.labeled("Service", comboService),
                new Separator(),
                Components.labeled("Patient", comboPat), btnAffecterPat,
                new Separator(),
                Components.labeled("Médecin", comboMed), btnAffecterMed);
        card.getStyleClass().add("card");

        box.getChildren().add(card);
        return box;
    }

    private void toast(javafx.scene.Node owner, String msg, ToastKind k) {
        if (DashboardView.getToastHost() != null)
            Components.toast(DashboardView.getToastHost(), msg, k);
    }
}
