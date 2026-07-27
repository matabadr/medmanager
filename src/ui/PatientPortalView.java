package ui;

import dao.Audit;
import dao.MedecinDao;
import dao.PatientDao;
import dao.RendezVousDao;
import dao.Stats;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import model.Patient;
import model.RendezVous;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Patient-facing portal. Shown when a user with role PATIENT logs in.
 * Self-contained: own top bar, own layout, no admin/medecin navigation.
 * Only reads data scoped to the logged-in patient.
 */
public class PatientPortalView extends BorderPane {

    private static final DateTimeFormatter FMT_LONG =
            DateTimeFormatter.ofPattern("EEEE dd MMMM yyyy 'à' HH'h'mm", Locale.FRENCH);
    private static final DateTimeFormatter FMT_SHORT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy  HH:mm");
    private static final DateTimeFormatter FMT_BIRTH =
            DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.FRENCH);

    private final PatientDao pDao = new PatientDao();
    private final MedecinDao mDao = new MedecinDao();
    private final RendezVousDao rDao = new RendezVousDao();

    private Runnable logoutHandler;
    private final Label clock = new Label();
    private final Circle statusDot = new Circle(5);
    private final Label statusText = new Label("PostgreSQL · vérification...");
    private final Label statusMono = new Label("");

    public PatientPortalView() {
        getStyleClass().add("app-shell");
        setTop(buildTopBar());
        setBottom(buildStatusBar());
        setCenter(buildContent());
        startClock();
        startStatusProbe();
    }

    public void setLogoutHandler(Runnable r) { this.logoutHandler = r; }

    /* ---------- top bar ---------- */

    private HBox buildTopBar() {
        HBox bar = new HBox(12);
        bar.getStyleClass().add("topbar");
        bar.setAlignment(Pos.CENTER_LEFT);

        Label brand = new Label("⚕  MedManager  ·  Espace patient");
        brand.getStyleClass().add("topbar-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        clock.getStyleClass().add("topbar-clock");

        Button themeBtn = new Button("🌙");
        themeBtn.getStyleClass().add("theme-toggle");
        themeBtn.setOnAction(e -> {
            StackPane host = DashboardView.getToastHost();
            if (host != null) {
                boolean dark = host.getStyleClass().contains("dark");
                if (dark) { host.getStyleClass().remove("dark"); themeBtn.setText("🌙"); }
                else      { host.getStyleClass().add("dark");    themeBtn.setText("☀"); }
            }
        });

        // user chip
        String name = Session.current() == null ? "" : Session.current().getDisplayName();
        String init = initials(name);
        Label userChip = new Label(init + "    " + name + " · Patient");
        userChip.getStyleClass().add("user-chip");

        Button logout = new Button("⎋  Déconnexion");
        logout.getStyleClass().add("logout-btn");
        logout.setOnAction(e -> {
            Audit.log("LOGOUT", "utilisateur",
                    Session.current() == null ? null : Session.current().getUsername(), null);
            if (logoutHandler != null) logoutHandler.run();
        });

        bar.getChildren().addAll(brand, spacer, clock, themeBtn, userChip, logout);
        return bar;
    }

    /* ---------- status bar ---------- */

    private HBox buildStatusBar() {
        HBox bar = new HBox(12);
        bar.getStyleClass().add("statusbar");
        bar.setAlignment(Pos.CENTER_LEFT);
        statusDot.getStyleClass().add("status-dot");
        statusText.getStyleClass().add("status-text");
        statusMono.getStyleClass().add("status-mono");
        Region s = new Region(); HBox.setHgrow(s, Priority.ALWAYS);
        Label v = new Label("MedManager v1.2  ·  Espace patient");
        v.getStyleClass().add("status-text");
        bar.getChildren().addAll(statusDot, statusText, new Label("·") {{ getStyleClass().add("status-text"); }},
                statusMono, s, v);
        return bar;
    }

    private void startClock() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("EEE dd MMM yyyy  ·  HH:mm:ss", Locale.FRENCH);
        Runnable tick = () -> clock.setText(LocalDateTime.now().format(fmt));
        tick.run();
        Timeline t = new Timeline(new KeyFrame(Duration.seconds(1), e -> tick.run()));
        t.setCycleCount(Timeline.INDEFINITE);
        t.play();
    }

    private void startStatusProbe() {
        Runnable probe = () -> new Thread(() -> {
            long ms = Stats.ping();
            Platform.runLater(() -> {
                if (ms < 0) {
                    if (!statusDot.getStyleClass().contains("error")) statusDot.getStyleClass().add("error");
                    statusText.setText("PostgreSQL · hors-ligne"); statusMono.setText("");
                } else {
                    statusDot.getStyleClass().remove("error");
                    statusText.setText("Connecté à votre dossier"); statusMono.setText(ms + " ms");
                }
            });
        }, "db-probe").start();
        probe.run();
        Timeline tl = new Timeline(new KeyFrame(Duration.seconds(8), e -> probe.run()));
        tl.setCycleCount(Timeline.INDEFINITE);
        tl.play();
    }

    /* ---------- main content ---------- */

    private ScrollPane buildContent() {
        VBox box = new VBox(16);
        box.setPadding(new Insets(22, 28, 22, 28));

        String pid = Session.current() == null ? null : Session.current().getPatientId();
        Patient me = pid == null ? null : pDao.findById(pid).orElse(null);
        if (me == null) {
            Label err = new Label("Impossible de charger votre dossier (patient introuvable).");
            err.setStyle("-fx-text-fill: #dc2626; -fx-font-weight: 700;");
            box.getChildren().add(err);
            ScrollPane sp = wrap(box);
            return sp;
        }

        List<RendezVous> history = rDao.listerPourPatient(me.getId(), mDao);
        RendezVous next = history.stream()
                .filter(r -> r.getDateHeure() != null && r.getDateHeure().isAfter(LocalDateTime.now()))
                .reduce((a, b) -> a.getDateHeure().isBefore(b.getDateHeure()) ? a : b)
                .orElse(null);
        long past = history.stream()
                .filter(r -> r.getDateHeure() != null && r.getDateHeure().isBefore(LocalDateTime.now()))
                .count();
        long upcoming = history.size() - past;

        box.getChildren().addAll(
                greetingCard(me),
                nextRdvHero(next),
                kpisRow(history.size(), upcoming, past, me),
                infoTwoColumns(me),
                historyCard(history));

        return wrap(box);
    }

    private ScrollPane wrap(VBox box) {
        ScrollPane sp = new ScrollPane(box);
        sp.setFitToWidth(true);
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sp.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        sp.setStyle("-fx-background-color: transparent;");
        sp.getStyleClass().add("scroll-pane-transparent");
        return sp;
    }

    /* ---------- sections ---------- */

    private HBox greetingCard(Patient p) {
        Label hello = new Label("Bonjour " + p.getPrenom() + " 👋");
        hello.getStyleClass().add("section-title");
        Label sub = new Label("Votre espace personnel — informations et rendez-vous.");
        sub.getStyleClass().add("section-subtitle");
        VBox texts = new VBox(2, hello, sub);
        HBox.setHgrow(texts, Priority.ALWAYS);

        Label avatar = new Label(initials(p.getPrenom() + " " + p.getNom()));
        avatar.setStyle(
                "-fx-min-width: 56; -fx-min-height: 56; -fx-max-width: 56; -fx-max-height: 56;"
              + " -fx-background-color: linear-gradient(to bottom right, #14b8b7, #0b8483);"
              + " -fx-background-radius: 999; -fx-alignment: center;"
              + " -fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: 800;");

        HBox row = new HBox(14, avatar, texts);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private VBox nextRdvHero(RendezVous next) {
        VBox card = new VBox(10);
        card.getStyleClass().add("card");
        card.setStyle("-fx-background-color: linear-gradient(to right, #0f172a 0%, #0b8483 100%);"
                + " -fx-background-radius: 16; -fx-padding: 22 26 22 26;"
                + " -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.18), 18, 0, 0, 6);");
        Label tag = new Label(next == null ? "AUCUN RENDEZ-VOUS À VENIR" : "PROCHAIN RENDEZ-VOUS");
        tag.setStyle("-fx-text-fill: rgba(255,255,255,0.7); -fx-font-size: 11px; -fx-font-weight: 800;"
                + " -fx-letter-spacing: 0.08em;");
        if (next == null) {
            Label body = new Label("Vous n'avez pas de rendez-vous programmé pour le moment.");
            body.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");
            Label hint = new Label("Contactez l'accueil pour planifier une consultation.");
            hint.setStyle("-fx-text-fill: rgba(255,255,255,0.75); -fx-font-size: 12px;");
            card.getChildren().addAll(tag, body, hint);
            return card;
        }
        Label when = new Label(FMT_LONG.format(next.getDateHeure()));
        when.setStyle("-fx-text-fill: white; -fx-font-size: 22px; -fx-font-weight: 800;");
        Label with = new Label("avec " + (next.getMedecin() == null
                ? "votre médecin"
                : "Dr. " + next.getMedecin().getNom() + " " + next.getMedecin().getPrenom()
                  + (next.getMedecin().getSpecialite() == null ? ""
                     : "  ·  " + next.getMedecin().getSpecialite().name())));
        with.setStyle("-fx-text-fill: rgba(255,255,255,0.92); -fx-font-size: 14px;");

        Label countdown = new Label(countdownString(next.getDateHeure()));
        countdown.setStyle("-fx-text-fill: rgba(255,255,255,0.85); -fx-font-size: 12px; -fx-font-style: italic;");

        card.getChildren().addAll(tag, when, with, countdown);
        return card;
    }

    private static String countdownString(LocalDateTime dt) {
        if (dt == null) return "";
        java.time.Duration d = java.time.Duration.between(LocalDateTime.now(), dt);
        long days = d.toDays();
        long hours = d.minusDays(days).toHours();
        if (days > 0) return "dans " + days + " jour" + (days > 1 ? "s" : "")
                + (hours > 0 ? " et " + hours + "h" : "");
        if (hours > 0) return "dans " + hours + " heures";
        long mins = d.toMinutes();
        if (mins > 0) return "dans " + mins + " minutes";
        return "imminent";
    }

    private HBox kpisRow(int total, long upcoming, long past, Patient me) {
        HBox row = new HBox(14);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getChildren().addAll(
                miniStat("🗓", "Rendez-vous", String.valueOf(total), "au total"),
                miniStat("⏳", "À venir",     String.valueOf(upcoming), "rendez-vous"),
                miniStat("✓",  "Passés",      String.valueOf(past), "consultations"),
                miniStat("🩸", "Groupe",      me.getGroupeSanguin() == null ? "—" : me.getGroupeSanguin().getLabel(), "sanguin"));
        return row;
    }

    private VBox miniStat(String icon, String label, String value, String hint) {
        VBox b = new VBox(2);
        b.getStyleClass().add("kpi");
        b.setMinWidth(180);
        HBox.setHgrow(b, Priority.ALWAYS);
        b.setMaxWidth(Double.MAX_VALUE);
        HBox top = new HBox(8);
        Label i = new Label(icon); i.getStyleClass().add("kpi-icon");
        Label l = new Label(label.toUpperCase()); l.getStyleClass().add("kpi-label");
        top.getChildren().addAll(i, l);
        top.setAlignment(Pos.CENTER_LEFT);
        Label v = new Label(value);
        v.getStyleClass().addAll("kpi-value", "kpi-accent");
        Label h = new Label(hint); h.getStyleClass().add("section-subtitle");
        b.getChildren().addAll(top, v, h);
        return b;
    }

    private HBox infoTwoColumns(Patient p) {
        HBox row = new HBox(14);
        VBox info = personalCard(p);
        VBox contact = contactCard(p);
        HBox.setHgrow(info, Priority.ALWAYS);
        HBox.setHgrow(contact, Priority.ALWAYS);
        row.getChildren().addAll(info, contact);
        return row;
    }

    private VBox personalCard(Patient p) {
        VBox card = new VBox(8);
        card.getStyleClass().add("card");
        card.setMaxWidth(Double.MAX_VALUE);
        Label t = new Label("Informations personnelles");
        t.getStyleClass().add("section-title");

        GridPane g = new GridPane();
        g.setHgap(20); g.setVgap(8);
        int age = p.getDateNaissance() == null ? 0
                : Period.between(p.getDateNaissance(), LocalDate.now()).getYears();
        addRow(g, 0, "Identifiant",        p.getId());
        addRow(g, 1, "Nom complet",        p.getPrenom() + " " + p.getNom());
        addRow(g, 2, "Date de naissance",  p.getDateNaissance() == null ? "—" : FMT_BIRTH.format(p.getDateNaissance()));
        addRow(g, 3, "Âge",                age + " ans");
        addRow(g, 4, "Groupe sanguin",     p.getGroupeSanguin() == null ? "—" : p.getGroupeSanguin().getLabel());

        card.getChildren().addAll(t, g);
        return card;
    }

    private VBox contactCard(Patient p) {
        VBox card = new VBox(8);
        card.getStyleClass().add("card");
        card.setMaxWidth(Double.MAX_VALUE);
        Label t = new Label("Contact & service");
        t.getStyleClass().add("section-title");

        GridPane g = new GridPane();
        g.setHgap(20); g.setVgap(8);
        addRow(g, 0, "Téléphone", text(p.getTelephone(), "—"));
        addRow(g, 1, "Service",   text(p.getServiceAdmission(), "non affecté"));

        Label info = new Label("Pour mettre à jour vos coordonnées, contactez l'accueil de l'hôpital.");
        info.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11px; -fx-font-style: italic;");

        card.getChildren().addAll(t, g, info);
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
    private VBox historyCard(List<RendezVous> history) {
        VBox card = new VBox(10);
        card.getStyleClass().add("card");
        Label t = new Label("Mes rendez-vous");
        t.getStyleClass().add("section-title");
        Label badge = Components.statBadge(history.size()
                + (history.size() <= 1 ? " rendez-vous" : " rendez-vous"));
        HBox head = new HBox(10, t, badge);
        head.setAlignment(Pos.CENTER_LEFT);

        if (history.isEmpty()) {
            Label empty = new Label("Aucun rendez-vous enregistré dans votre dossier.");
            empty.getStyleClass().add("section-subtitle");
            card.getChildren().addAll(head, empty);
            return card;
        }

        TableView<RendezVous> table = new TableView<>();
        TableColumn<RendezVous, String> colId = new TableColumn<>("Réf.");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colId.setPrefWidth(90);

        TableColumn<RendezVous, LocalDateTime> colDate = new TableColumn<>("Date / heure");
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateHeure"));
        colDate.setPrefWidth(180);
        colDate.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(LocalDateTime d, boolean empty) {
                super.updateItem(d, empty);
                setText(empty || d == null ? "" : FMT_SHORT.format(d));
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
        table.setItems(FXCollections.observableArrayList(history));
        table.setPrefHeight(280);

        card.getChildren().addAll(head, table);
        return card;
    }

    /* ---------- helpers ---------- */

    private static String initials(String s) {
        if (s == null || s.isBlank()) return "?";
        String[] parts = s.trim().split("\\s+");
        if (parts.length == 1) return parts[0].substring(0, 1).toUpperCase();
        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
    }

    private static String text(String s, String fallback) {
        return (s == null || s.isBlank()) ? fallback : s;
    }
}
