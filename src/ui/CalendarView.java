package ui;

import dao.MedecinDao;
import dao.PatientDao;
import dao.RendezVousDao;
import enums.Specialite;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import model.Medecin;
import model.Patient;
import model.RendezVous;
import ui.Components.ToastKind;

import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Week calendar for rendez-vous. Click an empty slot → quick-create dialog.
 * Click an existing block → quick-edit / delete dialog.
 * Color is keyed by spécialité.
 */
public class CalendarView extends VBox {

    private static final int HOUR_START = 8;   // 08:00
    private static final int HOUR_END   = 19;  // 19:00 (exclusive)
    private static final double CELL_HEIGHT = 56;

    private final PatientDao pDao = new PatientDao();
    private final MedecinDao mDao = new MedecinDao();
    private final RendezVousDao rDao = new RendezVousDao();

    private LocalDate weekStart;
    private final Label weekLabel = new Label();
    private final GridPane grid = new GridPane();

    public CalendarView() {
        setPadding(new Insets(20, 24, 24, 24));
        setSpacing(14);
        weekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        getChildren().addAll(buildHeader(), buildLegend(), buildGridCard());
        refresh();
    }

    private HBox buildHeader() {
        Label title = new Label("Agenda");
        title.getStyleClass().add("section-title");
        Label sub = new Label("Vue hebdomadaire des rendez-vous. Cliquez une case vide pour planifier.");
        sub.getStyleClass().add("section-subtitle");
        VBox titleBox = new VBox(2, title, sub);
        HBox.setHgrow(titleBox, Priority.ALWAYS);

        Button prev = Components.ghost("◀");
        Button today = Components.ghost("Cette semaine");
        Button next = Components.ghost("▶");
        prev.setOnAction(e -> { weekStart = weekStart.minusWeeks(1); refresh(); });
        next.setOnAction(e -> { weekStart = weekStart.plusWeeks(1); refresh(); });
        today.setOnAction(e -> { weekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)); refresh(); });

        weekLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 700;");
        weekLabel.setMinWidth(220);
        weekLabel.setAlignment(Pos.CENTER);

        HBox row = new HBox(10, titleBox, prev, weekLabel, next, today);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private HBox buildLegend() {
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER_LEFT);
        Label l = new Label("Spécialités:");
        l.getStyleClass().add("section-subtitle");
        box.getChildren().add(l);
        for (Specialite s : Specialite.values()) {
            Label chip = new Label(s.name());
            chip.setStyle("-fx-background-color: " + colorFor(s) + ";"
                    + " -fx-text-fill: white; -fx-padding: 3 10 3 10;"
                    + " -fx-background-radius: 999; -fx-font-size: 10px; -fx-font-weight: 700;");
            box.getChildren().add(chip);
        }
        return box;
    }

    private VBox buildGridCard() {
        VBox card = new VBox(10);
        card.getStyleClass().add("card");

        grid.setHgap(0); grid.setVgap(0);
        grid.setStyle("-fx-background-color: white;");

        ScrollPane sp = new ScrollPane(grid);
        sp.setFitToWidth(true);
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sp.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        sp.getStyleClass().add("scroll-pane-transparent");
        sp.setStyle("-fx-background-color: transparent;");
        sp.setPrefHeight(560);

        card.getChildren().add(sp);
        VBox.setVgrow(card, Priority.ALWAYS);
        return card;
    }

    private void refresh() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM", Locale.FRENCH);
        LocalDate weekEnd = weekStart.plusDays(6);
        weekLabel.setText(weekStart.format(fmt) + "  —  " + weekEnd.format(fmt) + "  " + weekEnd.getYear());

        grid.getChildren().clear();
        grid.getColumnConstraints().clear();
        grid.getRowConstraints().clear();

        // Column constraints: hour col + 7 day cols
        javafx.scene.layout.ColumnConstraints hourCol = new javafx.scene.layout.ColumnConstraints();
        hourCol.setMinWidth(70); hourCol.setPrefWidth(70);
        grid.getColumnConstraints().add(hourCol);
        for (int i = 0; i < 7; i++) {
            javafx.scene.layout.ColumnConstraints c = new javafx.scene.layout.ColumnConstraints();
            c.setHgrow(Priority.ALWAYS);
            c.setPercentWidth(100.0 / 7);
            grid.getColumnConstraints().add(c);
        }

        // Header row: blank + 7 days
        Region corner = new Region();
        grid.add(corner, 0, 0);
        DateTimeFormatter dayFmt = DateTimeFormatter.ofPattern("EEE\ndd MMM", Locale.FRENCH);
        for (int d = 0; d < 7; d++) {
            LocalDate day = weekStart.plusDays(d);
            Label dayLbl = new Label(day.format(dayFmt));
            boolean today = day.equals(LocalDate.now());
            dayLbl.setStyle("-fx-padding: 8 4 8 4; -fx-alignment: center; -fx-text-alignment: center;"
                    + " -fx-min-height: 50; -fx-border-color: #e2e8f0; -fx-border-width: 0 0 1 1;"
                    + " -fx-font-size: 11px; -fx-font-weight: 700;"
                    + (today ? " -fx-text-fill: #0b8483;" : " -fx-text-fill: #475569;"));
            dayLbl.setMaxWidth(Double.MAX_VALUE);
            grid.add(dayLbl, d + 1, 0);
        }

        // Hour rows
        for (int h = HOUR_START, row = 1; h < HOUR_END; h++, row++) {
            Label hourLbl = new Label(String.format("%02d:00", h));
            hourLbl.setStyle("-fx-padding: 4 8 4 8; -fx-text-fill: #64748b; -fx-font-size: 11px;"
                    + " -fx-min-height: " + CELL_HEIGHT + "; -fx-alignment: top-right;"
                    + " -fx-border-color: #e2e8f0; -fx-border-width: 1 1 0 0;");
            hourLbl.setMaxWidth(Double.MAX_VALUE);
            grid.add(hourLbl, 0, row);

            for (int d = 0; d < 7; d++) {
                LocalDate day = weekStart.plusDays(d);
                final int hh = h;
                StackPane cell = new StackPane();
                cell.setMinHeight(CELL_HEIGHT);
                cell.setPrefHeight(CELL_HEIGHT);
                cell.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-border-width: 1 1 0 0; -fx-cursor: hand;");
                cell.setOnMouseEntered(e -> cell.setStyle(cell.getStyle() + " -fx-background-color: #ecfeff;"));
                cell.setOnMouseExited(e -> cell.setStyle(cell.getStyle().replace(" -fx-background-color: #ecfeff;", "")));
                cell.setOnMouseClicked(e -> openQuickCreate(day, hh, 0));
                grid.add(cell, d + 1, row);
            }
        }

        // Place RDVs
        LocalDateTime start = weekStart.atStartOfDay();
        LocalDateTime end = weekStart.plusDays(7).atStartOfDay();
        List<RendezVous> rdvs = rDao.entre(start, end, pDao, mDao);

        Map<String, Integer> stackedPerCell = new HashMap<>();
        for (RendezVous rv : rdvs) {
            if (rv.getDateHeure() == null) continue;
            LocalDateTime dt = rv.getDateHeure();
            int h = dt.getHour();
            if (h < HOUR_START || h >= HOUR_END) continue;
            int dayCol = (int) java.time.temporal.ChronoUnit.DAYS.between(weekStart, dt.toLocalDate());
            if (dayCol < 0 || dayCol > 6) continue;
            int rowIdx = (h - HOUR_START) + 1;

            String key = dayCol + ":" + rowIdx;
            int stack = stackedPerCell.getOrDefault(key, 0);
            stackedPerCell.put(key, stack + 1);

            grid.add(buildRdvBlock(rv, dt, stack), dayCol + 1, rowIdx);
        }
    }

    private Region buildRdvBlock(RendezVous rv, LocalDateTime dt, int stackOffset) {
        Specialite spe = rv.getMedecin() == null ? null : rv.getMedecin().getSpecialite();
        String color = colorFor(spe);
        VBox block = new VBox(1);
        Label time = new Label(String.format("%02d:%02d", dt.getHour(), dt.getMinute()));
        time.setStyle("-fx-text-fill: white; -fx-font-size: 10px; -fx-font-weight: 800;");
        Label who = new Label(rv.getNomPatient());
        who.setStyle("-fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: 700;");
        Label doc = new Label(rv.getNomMedecin());
        doc.setStyle("-fx-text-fill: rgba(255,255,255,0.85); -fx-font-size: 10px;");
        block.getChildren().addAll(time, who, doc);
        block.setStyle("-fx-background-color: " + color + ";"
                + " -fx-background-radius: 8; -fx-padding: 4 8 4 8; -fx-cursor: hand;"
                + " -fx-translate-y: " + (stackOffset * 4) + ";"
                + " -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.18), 6, 0, 0, 2);");
        block.setMaxWidth(Double.MAX_VALUE);
        block.setMaxHeight(Double.MAX_VALUE);
        VBox.setMargin(block, new Insets(2));
        block.setOnMouseClicked(e -> {
            e.consume();
            openEdit(rv);
        });
        return block;
    }

    private String colorFor(Specialite s) {
        if (s == null) return "#64748b";
        return switch (s) {
            case CARDIOLOGIE  -> "#dc2626";
            case NEUROLOGIE   -> "#7c3aed";
            case PEDIATRIE    -> "#f59e0b";
            case CHIRURGIE    -> "#0ea5a4";
            case DERMATOLOGIE -> "#ec4899";
            case PSYCHIATRIE  -> "#6366f1";
            case RADIOLOGIE   -> "#0284c7";
            case URGENCES     -> "#b91c1c";
        };
    }

    /* ---------------- Dialogs ---------------- */

    private void openQuickCreate(LocalDate day, int hour, int minute) {
        Dialog<Boolean> dlg = new Dialog<>();
        dlg.setTitle("Nouveau rendez-vous");
        dlg.setHeaderText(day + "  " + String.format("%02d:%02d", hour, minute));

        ComboBox<Patient> cbPat = new ComboBox<>(FXCollections.observableArrayList(pDao.listerPatients()));
        cbPat.setMaxWidth(Double.MAX_VALUE);
        cbPat.setConverter(new StringConverter<>() {
            public String toString(Patient p) { return p == null ? "" : p.getNom() + " " + p.getPrenom() + " (" + p.getId() + ")"; }
            public Patient fromString(String s) { return null; }
        });
        cbPat.setPromptText("Patient");

        ComboBox<Medecin> cbMed = new ComboBox<>(FXCollections.observableArrayList(mDao.listerMedecins()));
        cbMed.setMaxWidth(Double.MAX_VALUE);
        cbMed.setConverter(new StringConverter<>() {
            public String toString(Medecin m) { return m == null ? "" : "Dr. " + m.getNom() + " — " + (m.getSpecialite() == null ? "" : m.getSpecialite().name()); }
            public Medecin fromString(String s) { return null; }
        });
        cbMed.setPromptText("Médecin");

        Spinner<Integer> sH = new Spinner<>(HOUR_START, HOUR_END - 1, hour);
        Spinner<Integer> sM = new Spinner<>(0, 59, minute, 15);

        VBox box = new VBox(8,
                Components.labeled("Patient", cbPat),
                Components.labeled("Médecin", cbMed),
                Components.labeled("Heure", new HBox(6, sH, new Label(":"), sM)));
        box.setPrefWidth(360);
        box.setPadding(new Insets(8));
        dlg.getDialogPane().setContent(box);

        ButtonType ok = new ButtonType("Planifier", ButtonBar.ButtonData.OK_DONE);
        dlg.getDialogPane().getButtonTypes().addAll(ok, ButtonType.CANCEL);
        dlg.setResultConverter(bt -> {
            if (bt == ok && cbPat.getValue() != null && cbMed.getValue() != null) {
                LocalDateTime dt = LocalDateTime.of(day, LocalTime.of(sH.getValue(), sM.getValue()));
                rDao.ajouter(cbPat.getValue().getId(), cbMed.getValue().getId(), Timestamp.valueOf(dt));
                if (DashboardView.getToastHost() != null)
                    Components.toast(DashboardView.getToastHost(), "Rendez-vous planifié", ToastKind.SUCCESS);
                return true;
            }
            return false;
        });
        dlg.showAndWait().filter(b -> b).ifPresent(b -> refresh());
    }

    private void openEdit(RendezVous rv) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle("Rendez-vous " + rv.getId());
        a.setHeaderText("Patient : " + rv.getNomPatient()
                + "\nMédecin : " + rv.getNomMedecin()
                + "\nLe : " + DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").format(rv.getDateHeure()));
        ButtonType bDelete = new ButtonType("Supprimer", ButtonBar.ButtonData.OTHER);
        a.getButtonTypes().setAll(bDelete, ButtonType.CLOSE);
        a.showAndWait().filter(bt -> bt == bDelete).ifPresent(bt -> {
            rDao.supprimer(rv.getId());
            if (DashboardView.getToastHost() != null)
                Components.toast(DashboardView.getToastHost(), "Rendez-vous supprimé", ToastKind.INFO);
            refresh();
        });
    }
}
