package ui;

import dao.AdministrateurDao;
import dao.Audit;
import dao.MedecinDao;
import dao.PatientDao;
import dao.RendezVousDao;
import dao.Stats;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import javafx.util.Duration;
import model.Patient;
import model.RendezVous;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DashboardView extends BorderPane {

    /* ---------- statics ---------- */
    private static StackPane toastHost;
    public static void setToastHost(StackPane host) { toastHost = host; }
    public static StackPane getToastHost() { return toastHost; }
    private static DashboardView active;
    public static DashboardView activeDashboard() { return active; }

    /* ---------- instance ---------- */
    private Runnable logoutHandler;
    public void setLogoutHandler(Runnable h) { this.logoutHandler = h; }

    private final Label clock = new Label();
    private final Label currentTitle = new Label("Medical Overview");
    private final List<Button> menuButtons = new ArrayList<>();
    private final StackPane centerHolder = new StackPane();
    private VBox rightPanel;

    private final Circle statusDot = new Circle(5);
    private final Label statusText = new Label("PostgreSQL · vérification...");
    private final Label statusMono = new Label("");

    private Button btnHome, btnPatients, btnMedecins, btnAdmins, btnOps, btnAudit;

    public DashboardView() {
        active = this;
        getStyleClass().add("app-shell");

        setLeft(buildSidebar());
        setTop(buildTopBar());
        setBottom(buildStatusBar());

        rightPanel = buildRightPanel();
        setRight(rightPanel);

        centerHolder.getChildren().setAll(wrapScroll(buildOverview()));
        setCenter(centerHolder);

        startClock();
        startStatusProbe();
        sceneProperty().addListener((o, ov, nv) -> { if (nv != null) installShortcuts(nv); });
    }

    /* =========================================================
     * Sidebar — narrow, icon-only (SVG icons for reliable rendering)
     * ========================================================= */

    // 24x24 viewBox filled icon paths (Material-style)
    private static final String SVG_LOGO     = "M10 4h4v6h6v4h-6v6h-4v-6H4v-4h6z";
    private static final String SVG_HOME     = "M12 3l9 8h-3v9h-5v-6h-2v6H6v-9H3l9-8z";
    private static final String SVG_USERS    = "M9 12c2.2 0 4-1.8 4-4S11.2 4 9 4 5 5.8 5 8s1.8 4 4 4zm0 2c-2.7 0-8 1.3-8 4v2h16v-2c0-2.7-5.3-4-8-4zm9-2c1.7 0 3-1.3 3-3s-1.3-3-3-3-3 1.3-3 3 1.3 3 3 3zm0 2c-.5 0-1.1.1-1.6.2 1 .8 1.6 1.9 1.6 3.3v2h6v-2c0-2.7-3.7-3.5-6-3.5z";
    private static final String SVG_DOCTOR   = "M8 4a4 4 0 0 0-4 4v4a6 6 0 0 0 5 5.9V21a3 3 0 0 0 3 3 3 3 0 0 0 3-3v-3.1a6 6 0 0 0 5-5.9V8a4 4 0 0 0-4-4h-1v2h1a2 2 0 0 1 2 2v4a4 4 0 1 1-8 0V8a2 2 0 0 1 2-2h1V4H8zm10.5 11a2.5 2.5 0 1 0 0 5 2.5 2.5 0 0 0 0-5zm0 1.5a1 1 0 1 1 0 2 1 1 0 0 1 0-2z";
    private static final String SVG_SHIELD   = "M12 1L3 5v6c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V5l-9-4z";
    private static final String SVG_CALENDAR = "M19 4h-1V2h-2v2H8V2H6v2H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2zm0 16H5V10h14v10zm0-12H5V6h14v2z";
    private static final String SVG_AUDIT    = "M14 2H6c-1.1 0-1.99.9-1.99 2L4 20c0 1.1.89 2 1.99 2H18c1.1 0 2-.9 2-2V8l-6-6zm-1 7V3.5L18.5 9H13z";
    private static final String SVG_REFRESH  = "M17.65 6.35A7.95 7.95 0 0 0 12 4a8 8 0 1 0 8 8h-2a6 6 0 1 1-6-6c1.66 0 3.14.69 4.22 1.78L13 11h7V4z";
    private static final String SVG_MOON     = "M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z";
    private static final String SVG_SUN      = "M12 7a5 5 0 1 0 5 5 5 5 0 0 0-5-5zm0-5a1 1 0 0 0-1 1v2a1 1 0 0 0 2 0V3a1 1 0 0 0-1-1zm0 17a1 1 0 0 0-1 1v2a1 1 0 0 0 2 0v-2a1 1 0 0 0-1-1zM4.22 5.64a1 1 0 1 0-1.41 1.41l1.41 1.41a1 1 0 1 0 1.41-1.41zM19.78 18.36a1 1 0 1 0-1.41 1.41l1.41 1.41a1 1 0 0 0 1.41-1.41zM3 11H1a1 1 0 0 0 0 2h2a1 1 0 0 0 0-2zm20 0h-2a1 1 0 0 0 0 2h2a1 1 0 0 0 0-2zM5.64 18.36a1 1 0 0 0-1.41 0l-1.41 1.41a1 1 0 1 0 1.41 1.41l1.41-1.41a1 1 0 0 0 0-1.41zm12.72-12.72a1 1 0 0 0 0-1.41l-1.41 1.41a1 1 0 1 0 1.41 1.41z";
    private static final String SVG_LOGOUT   = "M17 7l-1.41 1.41L18.17 11H8v2h10.17l-2.58 2.58L17 17l5-5zM4 5h8V3H4c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h8v-2H4V5z";
    private static final String SVG_SEARCH   = "M15.5 14h-.79l-.28-.27a6.5 6.5 0 1 0-.7.7l.27.28v.79l5 4.99L20.49 19zM9.5 14C7 14 5 12 5 9.5S7 5 9.5 5 14 7 14 9.5 12 14 9.5 14z";

    private VBox buildSidebar() {
        VBox box = new VBox(8);
        box.getStyleClass().add("sidebar");
        box.setAlignment(Pos.TOP_CENTER);
        box.setPrefWidth(70);

        StackPane logo = new StackPane(svgIcon(SVG_LOGO, 22, "white-glyph"));
        logo.getStyleClass().add("brand-logo");

        btnHome     = iconButton(SVG_HOME,     "Tableau de bord");
        btnPatients = iconButton(SVG_USERS,    "Patients");
        btnMedecins = iconButton(SVG_DOCTOR,   "Médecins");
        btnAdmins   = iconButton(SVG_SHIELD,   "Administrateurs");
        btnOps      = iconButton(SVG_CALENDAR, "Opérations & Agenda");
        btnAudit    = iconButton(SVG_AUDIT,    "Journal d'audit");

        menuButtons.add(btnHome); menuButtons.add(btnPatients); menuButtons.add(btnMedecins);
        menuButtons.add(btnAdmins); menuButtons.add(btnOps); menuButtons.add(btnAudit);

        btnHome.setOnAction(e -> openHome());
        btnPatients.setOnAction(e -> swap(btnPatients, "Gestion des patients",
                wrapScroll(new GestionPatientsView()), false));
        btnMedecins.setOnAction(e -> swap(btnMedecins, "Gestion des médecins",
                wrapScroll(new GestionMedecinsView()), false));
        btnAdmins.setOnAction(e -> swap(btnAdmins, "Gestion des administrateurs",
                wrapScroll(new GestionAdminsView()), false));
        btnOps.setOnAction(e -> swap(btnOps, "Opérations & agenda",
                new OperationsHopitalView(), false));
        btnAudit.setOnAction(e -> swap(btnAudit, "Journal d'audit",
                wrapScroll(new AuditView()), false));

        applyRoleVisibility();
        setActive(btnHome);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // Bottom avatar (clickable → profile)
        Label avatar = new Label(initialsOfChip(
                Session.current() == null ? "?" : Session.current().getDisplayName()));
        avatar.getStyleClass().add("sidebar-avatar");
        avatar.setOnMouseClicked(e -> openProfile());
        Tooltip.install(avatar, new Tooltip("Mon profil"));

        box.getChildren().addAll(logo, new Region() {{ setMinHeight(8); }},
                btnHome, btnPatients, btnMedecins, btnAdmins, btnOps, btnAudit,
                spacer, avatar);
        return box;
    }

    private Button iconButton(String svg, String tooltip) {
        Button b = new Button();
        b.setGraphic(svgIcon(svg, 20, "icon-glyph"));
        b.getStyleClass().add("icon-btn");
        b.setTooltip(new Tooltip(tooltip));
        return b;
    }

    private static Region svgIcon(String svg, double size, String styleClass) {
        Region r = new Region();
        SVGPath p = new SVGPath();
        p.setContent(svg);
        r.setShape(p);
        r.setMinSize(size, size);
        r.setPrefSize(size, size);
        r.setMaxSize(size, size);
        r.setPickOnBounds(true);
        if (styleClass != null) r.getStyleClass().add(styleClass);
        return r;
    }

    private void applyRoleVisibility() {
        boolean isAdmin = Session.isAdmin();
        boolean canSeeMedecins = Session.isAdmin() || Session.isMedecin();
        setVisibleManaged(btnMedecins, canSeeMedecins);
        setVisibleManaged(btnAdmins, isAdmin);
        setVisibleManaged(btnAudit, isAdmin);
    }
    private void setVisibleManaged(Node n, boolean v) { n.setVisible(v); n.setManaged(v); }

    private void setActive(Button activeBtn) {
        for (Button b : menuButtons) b.getStyleClass().remove("active");
        if (activeBtn != null && !activeBtn.getStyleClass().contains("active"))
            activeBtn.getStyleClass().add("active");
    }

    /* =========================================================
     * Top bar
     * ========================================================= */
    private HBox buildTopBar() {
        HBox bar = new HBox(10);
        bar.getStyleClass().add("topbar");
        bar.setAlignment(Pos.CENTER_LEFT);

        currentTitle.getStyleClass().add("topbar-title");
        currentTitle.setMaxWidth(380);

        Region spacer1 = new Region(); HBox.setHgrow(spacer1, Priority.ALWAYS);

        // Search trigger with SVG magnifier on the left
        Button searchBtn = new Button("    Search here...");
        searchBtn.setGraphic(svgIcon(SVG_SEARCH, 16, "icon-glyph"));
        searchBtn.setContentDisplay(javafx.scene.control.ContentDisplay.LEFT);
        searchBtn.getStyleClass().add("topbar-search");
        searchBtn.setOnAction(e -> openGlobalSearch());

        Region spacer2 = new Region(); HBox.setHgrow(spacer2, Priority.ALWAYS);

        clock.getStyleClass().add("topbar-clock");

        Button refreshBtn = new Button();
        refreshBtn.setGraphic(svgIcon(SVG_REFRESH, 18, "icon-glyph"));
        refreshBtn.getStyleClass().add("theme-toggle");
        refreshBtn.setTooltip(new Tooltip("Rafraîchir"));
        refreshBtn.setOnAction(e -> {
            if ("Medical Overview".equals(currentTitle.getText()))
                centerHolder.getChildren().setAll(wrapScroll(buildOverview()));
            if (rightPanel != null) rightPanel.getChildren().setAll(rebuildRightPanelContent());
        });

        Button themeBtn = new Button();
        Region themeGlyph = svgIcon(SVG_MOON, 18, "icon-glyph");
        themeBtn.setGraphic(themeGlyph);
        themeBtn.getStyleClass().add("theme-toggle");
        themeBtn.setTooltip(new Tooltip("Thème clair / sombre"));
        themeBtn.setOnAction(e -> {
            StackPane host = getToastHost();
            if (host == null) return;
            boolean dark = host.getStyleClass().contains("dark");
            if (dark) {
                host.getStyleClass().remove("dark");
                themeBtn.setGraphic(svgIcon(SVG_MOON, 18, "icon-glyph"));
            } else {
                host.getStyleClass().add("dark");
                themeBtn.setGraphic(svgIcon(SVG_SUN, 18, "icon-glyph"));
            }
        });

        Button userBtn = new Button();
        userBtn.getStyleClass().add("user-chip");
        userBtn.setTooltip(new Tooltip("Mon profil"));
        if (Session.current() != null) {
            // Keep it short so it doesn't truncate
            userBtn.setText(initialsOfChip(Session.current().getDisplayName())
                    + "    " + Session.current().getDisplayName());
        }
        userBtn.setOnAction(e -> openProfile());

        Button logoutBtn = new Button("Déconnexion");
        logoutBtn.setGraphic(svgIcon(SVG_LOGOUT, 14, "icon-glyph-danger"));
        logoutBtn.setContentDisplay(javafx.scene.control.ContentDisplay.LEFT);
        logoutBtn.getStyleClass().add("logout-btn");
        logoutBtn.setOnAction(e -> doLogout());

        bar.getChildren().addAll(currentTitle, spacer1, searchBtn, spacer2,
                clock, refreshBtn, themeBtn, userBtn, logoutBtn);
        return bar;
    }

    private void doLogout() {
        Audit.log("LOGOUT", "utilisateur",
                Session.current() == null ? null : Session.current().getUsername(), null);
        if (logoutHandler != null) logoutHandler.run();
    }

    private void startClock() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("EEE dd MMM yyyy  ·  HH:mm:ss", Locale.FRENCH);
        Runnable tick = () -> clock.setText(LocalDateTime.now().format(fmt));
        tick.run();
        Timeline tl = new Timeline(new KeyFrame(Duration.seconds(1), e -> tick.run()));
        tl.setCycleCount(Timeline.INDEFINITE);
        tl.play();
    }

    /* =========================================================
     * Status bar
     * ========================================================= */
    private HBox buildStatusBar() {
        HBox sb = new HBox(12);
        sb.getStyleClass().add("statusbar");
        sb.setAlignment(Pos.CENTER_LEFT);
        statusDot.getStyleClass().add("status-dot");
        statusText.getStyleClass().add("status-text");
        statusMono.getStyleClass().add("status-mono");
        Label sep = new Label("·"); sep.getStyleClass().add("status-text");
        Region s = new Region(); HBox.setHgrow(s, Priority.ALWAYS);
        Label hint = new Label("Ctrl+K : recherche  ·  Ctrl+1–5 : modules  ·  Ctrl+L : déconnexion");
        hint.getStyleClass().add("status-text");
        sb.getChildren().addAll(statusDot, statusText, sep, statusMono, s, hint);
        return sb;
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
                    statusText.setText("PostgreSQL · connecté"); statusMono.setText(ms + " ms");
                }
            });
        }, "db-probe").start();
        probe.run();
        Timeline tl = new Timeline(new KeyFrame(Duration.seconds(8), e -> probe.run()));
        tl.setCycleCount(Timeline.INDEFINITE);
        tl.play();
    }

    /* =========================================================
     * Swap + helpers
     * ========================================================= */
    private void swap(Button btn, String title, Node newCenter, boolean keepRight) {
        setActive(btn);
        currentTitle.setText(title);
        newCenter.setOpacity(0);
        centerHolder.getChildren().setAll(newCenter);
        FadeTransition fade = new FadeTransition(Duration.millis(220), newCenter);
        fade.setFromValue(0); fade.setToValue(1);
        fade.play();
        // Right panel visible only on the overview
        setRight(keepRight ? rightPanel : null);
    }

    public void openHome() {
        currentTitle.setText("Medical Overview");
        setActive(btnHome);
        Node n = wrapScroll(buildOverview());
        n.setOpacity(0);
        centerHolder.getChildren().setAll(n);
        if (rightPanel == null) rightPanel = buildRightPanel();
        else rightPanel.getChildren().setAll(rebuildRightPanelContent());
        setRight(rightPanel);
        FadeTransition fade = new FadeTransition(Duration.millis(220), n);
        fade.setFromValue(0); fade.setToValue(1);
        fade.play();
    }

    public void openProfile() {
        setActive(null);
        currentTitle.setText("Mon profil");
        UserProfileView pv = new UserProfileView(this::openHome);
        Node n = wrapScroll(pv);
        n.setOpacity(0);
        centerHolder.getChildren().setAll(n);
        setRight(null);
        FadeTransition fade = new FadeTransition(Duration.millis(220), n);
        fade.setFromValue(0); fade.setToValue(1);
        fade.play();
    }

    public void openPatientDetail(Patient p) {
        if (p == null) return;
        setActive(btnPatients);
        currentTitle.setText("Dossier de " + p.getPrenom() + " " + p.getNom());
        PatientDetailView pd = new PatientDetailView(p,
                () -> swap(btnPatients, "Gestion des patients",
                        wrapScroll(new GestionPatientsView()), false));
        Node n = wrapScroll(pd);
        n.setOpacity(0);
        centerHolder.getChildren().setAll(n);
        setRight(null);
        FadeTransition fade = new FadeTransition(Duration.millis(220), n);
        fade.setFromValue(0); fade.setToValue(1);
        fade.play();
    }

    /* =========================================================
     * Keyboard shortcuts
     * ========================================================= */
    private void installShortcuts(Scene scene) {
        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.K, KeyCombination.CONTROL_DOWN), this::openGlobalSearch);
        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.DIGIT1, KeyCombination.CONTROL_DOWN), this::openHome);
        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.DIGIT2, KeyCombination.CONTROL_DOWN), () -> btnPatients.fire());
        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.DIGIT3, KeyCombination.CONTROL_DOWN), () -> { if (btnMedecins.isVisible()) btnMedecins.fire(); });
        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.DIGIT4, KeyCombination.CONTROL_DOWN), () -> { if (btnAdmins.isVisible()) btnAdmins.fire(); });
        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.DIGIT5, KeyCombination.CONTROL_DOWN), () -> btnOps.fire());
        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.L, KeyCombination.CONTROL_DOWN), this::doLogout);
    }

    /* =========================================================
     * Global search overlay
     * ========================================================= */
    private void openGlobalSearch() {
        StackPane host = getToastHost();
        if (host == null) return;
        for (Node n : host.getChildren())
            if ("search-overlay".equals(n.getId())) return;

        StackPane backdrop = new StackPane();
        backdrop.setId("search-overlay");
        backdrop.setStyle("-fx-background-color: rgba(15,23,42,0.55);");

        VBox card = new VBox(8);
        card.setMaxWidth(560); card.setMaxHeight(420);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 18; -fx-padding: 18;"
                + " -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 28, 0, 0, 10);");
        StackPane.setMargin(card, new Insets(80, 0, 0, 0));
        StackPane.setAlignment(card, Pos.TOP_CENTER);

        Label hint = new Label("Recherche globale");
        hint.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b; -fx-font-weight: 700;");
        TextField search = new TextField();
        search.setPromptText("🔍  Rechercher patient, médecin, administrateur...");
        search.getStyleClass().add("search-box");

        VBox results = new VBox(4);
        ScrollPane sp = new ScrollPane(results);
        sp.setFitToWidth(true);
        sp.setPrefHeight(320);
        sp.getStyleClass().add("scroll-pane-transparent");
        sp.setStyle("-fx-background-color: transparent;");

        var patients = new PatientDao().listerPatients();
        var medecins = new MedecinDao().listerMedecins();
        var admins = new AdministrateurDao().lister();

        Runnable doSearch = () -> {
            results.getChildren().clear();
            String q = search.getText() == null ? "" : search.getText().trim().toLowerCase();
            if (q.isEmpty()) return;
            int hits = 0;
            for (Patient p : patients) {
                if (hits >= 25) break;
                String hay = (p.getId() + " " + p.getNom() + " " + p.getPrenom()).toLowerCase();
                if (hay.contains(q)) {
                    results.getChildren().add(searchHit("👥 Patient",
                            p.getNom() + " " + p.getPrenom() + " · " + p.getId(),
                            () -> { closeSearch(host); openPatientDetail(p); }));
                    hits++;
                }
            }
            for (var m : medecins) {
                if (hits >= 25) break;
                String hay = (m.getId() + " " + m.getNom() + " " + m.getPrenom()
                        + " " + (m.getSpecialite() == null ? "" : m.getSpecialite().name())).toLowerCase();
                if (hay.contains(q)) {
                    results.getChildren().add(searchHit("🩺 Médecin",
                            "Dr. " + m.getNom() + " " + m.getPrenom() + " · "
                              + (m.getSpecialite() == null ? "?" : m.getSpecialite().name()),
                            () -> { closeSearch(host); if (btnMedecins.isVisible()) btnMedecins.fire(); }));
                    hits++;
                }
            }
            for (var a : admins) {
                if (hits >= 25) break;
                String hay = (a.getId() + " " + a.getNom() + " " + a.getPrenom()
                        + " " + (a.getDepartement() == null ? "" : a.getDepartement())).toLowerCase();
                if (hay.contains(q)) {
                    results.getChildren().add(searchHit("🛡 Administrateur",
                            a.getNom() + " " + a.getPrenom() + " · " + a.getDepartement(),
                            () -> { closeSearch(host); if (btnAdmins.isVisible()) btnAdmins.fire(); }));
                    hits++;
                }
            }
            if (hits == 0) {
                Label nope = new Label("Aucun résultat pour « " + search.getText() + " ».");
                nope.setStyle("-fx-text-fill: #64748b; -fx-padding: 12;");
                results.getChildren().add(nope);
            }
        };
        search.textProperty().addListener((o, ov, nv) -> doSearch.run());

        card.getChildren().addAll(hint, search, sp);
        backdrop.getChildren().add(card);
        backdrop.setOnMouseClicked(e -> { if (e.getTarget() == backdrop) closeSearch(host); });
        backdrop.setOnKeyPressed(e -> { if (e.getCode() == KeyCode.ESCAPE) closeSearch(host); });
        host.getChildren().add(backdrop);
        Platform.runLater(search::requestFocus);
    }
    private Node searchHit(String category, String label, Runnable action) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-padding: 10 12 10 12; -fx-background-radius: 10; -fx-cursor: hand;");
        Label cat = new Label(category);
        cat.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b; -fx-font-weight: 700; -fx-min-width: 130;");
        Label lab = new Label(label);
        lab.setStyle("-fx-font-size: 13px; -fx-text-fill: #0f172a;");
        row.getChildren().addAll(cat, lab);
        row.setOnMouseEntered(e -> row.setStyle(row.getStyle() + " -fx-background-color: #f1f5f9;"));
        row.setOnMouseExited(e -> row.setStyle(row.getStyle().replace(" -fx-background-color: #f1f5f9;", "")));
        row.setOnMouseClicked(e -> action.run());
        return row;
    }
    private void closeSearch(StackPane host) {
        host.getChildren().removeIf(n -> "search-overlay".equals(n.getId()));
    }

    /* =========================================================
     * Overview (the "Medical Overview" home)
     * ========================================================= */
    private ScrollPane wrapScroll(Node content) {
        ScrollPane sp = new ScrollPane(content);
        sp.setFitToWidth(true);
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sp.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        sp.setStyle("-fx-background-color: transparent;");
        sp.getStyleClass().add("scroll-pane-transparent");
        return sp;
    }

    private VBox buildOverview() {
        VBox box = new VBox(16);
        box.setPadding(new Insets(4, 24, 24, 24));

        int nPat   = Stats.countTable("patient");
        int nMed   = Stats.countTable("medecin");
        int nRdv   = Stats.countUpcomingRdv();
        int nServ  = Stats.countTable("service_hospitalier");

        // KPI row — 4 tiles
        GridPane kpis = new GridPane();
        kpis.setHgap(16); kpis.setVgap(16);
        for (int i = 0; i < 4; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(25); cc.setHgrow(Priority.ALWAYS);
            kpis.getColumnConstraints().add(cc);
        }
        kpis.add(kpi("Patients",          nPat,  "+11.01%", true, "👥", "kpi-icon-blue"),  0, 0);
        kpis.add(kpi("Médecins",          nMed,  "+9.15%",  true, "🩺", "kpi-icon-mint"),  1, 0);
        kpis.add(kpi("RDV à venir",       nRdv,  "+5.00%",  true, "📅", "kpi-icon-amber"), 2, 0);
        kpis.add(kpi("Services",          nServ, "0.00%",  false, "🛡", "kpi-icon-violet"),3, 0);

        // Chart + mini calendar row
        HBox row2 = new HBox(16);
        VBox chartCard = visitsChartCard();
        VBox calCard = miniCalendarCard();
        chartCard.setMaxWidth(Double.MAX_VALUE);
        calCard.setMinWidth(280); calCard.setPrefWidth(320); calCard.setMaxWidth(320);
        HBox.setHgrow(chartCard, Priority.ALWAYS);
        row2.getChildren().addAll(chartCard, calCard);

        // Bar chart + active services row
        HBox row3 = new HBox(16);
        VBox barCard = barChartCard();
        VBox servicesCard = servicesListCard();
        HBox.setHgrow(barCard, Priority.ALWAYS);
        HBox.setHgrow(servicesCard, Priority.ALWAYS);
        row3.getChildren().addAll(barCard, servicesCard);

        box.getChildren().addAll(kpis, row2, row3);
        return box;
    }

    private VBox kpi(String label, int value, String delta, boolean up, String icon, String bubbleClass) {
        VBox b = new VBox(6);
        b.getStyleClass().add("kpi");
        b.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(b, Priority.ALWAYS);

        Label tag = new Label(label);
        tag.getStyleClass().add("kpi-tag");

        Label valueL = new Label("0");
        valueL.getStyleClass().add("kpi-value");

        Label deltaL = new Label((up ? "↗  " : "↘  ") + delta);
        deltaL.getStyleClass().add(up ? "kpi-delta-up" : "kpi-delta-down");

        VBox left = new VBox(2, tag, valueL, deltaL);
        HBox.setHgrow(left, Priority.ALWAYS);

        Label bubble = new Label(icon);
        bubble.getStyleClass().addAll("kpi-icon-bubble", bubbleClass);

        HBox row = new HBox(12, left, bubble);
        row.setAlignment(Pos.CENTER_LEFT);

        b.getChildren().add(row);

        // count-up
        IntegerProperty prop = new SimpleIntegerProperty(0);
        prop.addListener((o, ov, nv) -> valueL.setText(String.valueOf(nv.intValue())));
        Timeline tl = new Timeline(new KeyFrame(Duration.millis(900),
                new KeyValue(prop, Math.max(0, value))));
        tl.play();
        return b;
    }

    /* ---------- visits area chart + calendar ---------- */

    private VBox visitsChartCard() {
        VBox card = new VBox(10);
        card.getStyleClass().add("card");
        card.setMaxWidth(Double.MAX_VALUE);

        HBox header = new HBox(16);
        header.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("Total Visits");
        title.getStyleClass().add("section-title");
        Label dotCurr = legendDot("#2563eb", "Cette semaine");
        Label dotPrev = legendDot("#93c5fd", "Semaine précédente");
        Region s = new Region(); HBox.setHgrow(s, Priority.ALWAYS);
        header.getChildren().addAll(title, s, dotCurr, dotPrev);

        // Data: counts per day-of-week, current vs previous week, from RDV
        LocalDate weekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        Map<DayOfWeek, Integer> curr = new RendezVousDao().visitesParJourSemaine(weekStart);
        Map<DayOfWeek, Integer> prev = new RendezVousDao().visitesParJourSemaine(weekStart.minusWeeks(1));

        CategoryAxis x = new CategoryAxis();
        x.setCategories(FXCollections.observableArrayList("Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim"));
        NumberAxis y = new NumberAxis();
        y.setMinorTickVisible(false);
        AreaChart<String, Number> chart = new AreaChart<>(x, y);
        chart.setLegendVisible(false);
        chart.setHorizontalGridLinesVisible(true);
        chart.setVerticalGridLinesVisible(false);
        chart.setCreateSymbols(true);
        chart.setAnimated(false);
        chart.setPrefHeight(280);

        XYChart.Series<String, Number> seriesCurr = new XYChart.Series<>();
        XYChart.Series<String, Number> seriesPrev = new XYChart.Series<>();
        String[] labels = {"Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim"};
        DayOfWeek[] days = {DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                            DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY};
        for (int i = 0; i < 7; i++) {
            seriesPrev.getData().add(new XYChart.Data<>(labels[i], prev.getOrDefault(days[i], 0)));
            seriesCurr.getData().add(new XYChart.Data<>(labels[i], curr.getOrDefault(days[i], 0)));
        }
        chart.getData().addAll(seriesCurr, seriesPrev);

        // tooltip on hover
        for (XYChart.Series<String, Number> series : chart.getData()) {
            for (XYChart.Data<String, Number> d : series.getData()) {
                Node node = d.getNode();
                if (node != null) {
                    Tooltip.install(node, new Tooltip(d.getXValue() + ": " + d.getYValue()));
                }
            }
        }

        card.getChildren().addAll(header, chart);
        return card;
    }

    private Label legendDot(String hex, String text) {
        Label l = new Label("●  " + text);
        l.setStyle("-fx-text-fill: " + hex + "; -fx-font-size: 11px; -fx-font-weight: 700;");
        return l;
    }

    /* ---------- mini calendar ---------- */

    private VBox miniCalendarCard() {
        VBox card = new VBox(10);
        card.getStyleClass().add("card");
        Label monthLbl = new Label();
        monthLbl.getStyleClass().add("mini-cal-month");
        LocalDate today = LocalDate.now();
        monthLbl.setText(today.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.FRENCH)));

        // collect upcoming RDV days for dot decoration
        java.util.Set<LocalDate> rdvDays = new java.util.HashSet<>();
        for (RendezVous rv : new RendezVousDao().listerProchains(40)) {
            if (rv.getDateHeure() != null) rdvDays.add(rv.getDateHeure().toLocalDate());
        }

        GridPane g = new GridPane();
        g.getStyleClass().add("mini-cal");
        String[] dow = {"Lu", "Ma", "Me", "Je", "Ve", "Sa", "Di"};
        for (int i = 0; i < 7; i++) {
            Label l = new Label(dow[i]);
            l.getStyleClass().add("mini-cal-dowlbl");
            l.setMaxWidth(Double.MAX_VALUE);
            g.add(l, i, 0);
        }
        LocalDate firstOfMonth = today.withDayOfMonth(1);
        LocalDate gridStart = firstOfMonth.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        for (int week = 0; week < 6; week++) {
            for (int d = 0; d < 7; d++) {
                LocalDate cell = gridStart.plusDays(week * 7L + d);
                Label dayLbl = new Label(String.valueOf(cell.getDayOfMonth()));
                dayLbl.getStyleClass().add("mini-cal-day");
                if (cell.getMonth() != today.getMonth()) dayLbl.getStyleClass().add("muted");
                if (cell.equals(today)) dayLbl.getStyleClass().add("today");
                if (rdvDays.contains(cell) && !cell.equals(today)) dayLbl.getStyleClass().add("with-dot");
                g.add(dayLbl, d, week + 1);
            }
        }
        VBox.setVgrow(g, Priority.ALWAYS);

        // Nearest schedule below
        VBox nearest = new VBox(4);
        Label nh = new Label("Nearest schedule:");
        nh.setStyle("-fx-text-fill: #475569; -fx-font-size: 12px; -fx-font-weight: 700;");
        nearest.getChildren().add(nh);
        List<RendezVous> next = new RendezVousDao().listerProchains(3);
        DateTimeFormatter rfmt = DateTimeFormatter.ofPattern("dd/MM/yyyy  HH:mm");
        for (RendezVous rv : next) {
            Label dot = new Label("●");
            dot.setStyle("-fx-text-fill: #f59e0b; -fx-font-size: 10px;");
            Label info = new Label(rv.getNomPatient() + " · " + rfmt.format(rv.getDateHeure()));
            info.setStyle("-fx-font-size: 11px; -fx-text-fill: #0f172a;");
            HBox row = new HBox(6, dot, info);
            row.setAlignment(Pos.CENTER_LEFT);
            nearest.getChildren().add(row);
        }

        card.getChildren().addAll(monthLbl, g, nearest);
        return card;
    }

    /* ---------- bar chart + services list ---------- */

    private VBox barChartCard() {
        VBox card = new VBox(10);
        card.getStyleClass().add("card");
        card.setMaxWidth(Double.MAX_VALUE);

        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("Médecins par spécialité");
        title.getStyleClass().add("section-title");
        header.getChildren().add(title);

        Map<String, Integer> map = Stats.medecinsBySpecialite();
        CategoryAxis x = new CategoryAxis();
        NumberAxis y = new NumberAxis();
        y.setTickUnit(1); y.setMinorTickVisible(false);
        javafx.scene.chart.BarChart<String, Number> chart = new javafx.scene.chart.BarChart<>(x, y);
        chart.setLegendVisible(false);
        chart.setAnimated(true);
        chart.setPrefHeight(260);
        chart.setBarGap(2);
        chart.setCategoryGap(28);
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (Map.Entry<String, Integer> e : map.entrySet()) {
            String name = e.getKey().substring(0, 1) + e.getKey().substring(1).toLowerCase();
            series.getData().add(new XYChart.Data<>(name, e.getValue()));
        }
        chart.getData().add(series);

        card.getChildren().addAll(header, chart);
        return card;
    }

    private VBox servicesListCard() {
        VBox card = new VBox(10);
        card.getStyleClass().add("card");
        card.setMaxWidth(Double.MAX_VALUE);
        Label title = new Label("Patients par service");
        title.getStyleClass().add("section-title");
        card.getChildren().add(title);

        Map<String, Integer> map = Stats.patientsByService();
        if (map.isEmpty()) {
            Label empty = new Label("Aucun patient affecté à un service.");
            empty.getStyleClass().add("section-subtitle");
            card.getChildren().add(empty);
            return card;
        }
        int max = map.values().stream().mapToInt(Integer::intValue).max().orElse(1);
        for (Map.Entry<String, Integer> e : map.entrySet()) {
            HBox row = new HBox(10);
            row.setAlignment(Pos.CENTER_LEFT);
            Label name = new Label(e.getKey());
            name.setMinWidth(130);
            name.setStyle("-fx-font-size: 12px; -fx-font-weight: 600;");
            Region track = new Region();
            track.setStyle("-fx-background-color: #e2e8f0; -fx-background-radius: 999;");
            track.setPrefHeight(8);
            HBox.setHgrow(track, Priority.ALWAYS);
            double ratio = e.getValue() / (double) Math.max(1, max);
            Region fill = new Region();
            fill.setStyle("-fx-background-color: linear-gradient(to right, #93c5fd, #2563eb); -fx-background-radius: 999;");
            fill.setPrefHeight(8); fill.setMaxWidth(Double.MAX_VALUE);
            StackPane bar = new StackPane(track, fill);
            StackPane.setAlignment(fill, Pos.CENTER_LEFT);
            fill.prefWidthProperty().bind(bar.widthProperty().multiply(ratio));
            HBox.setHgrow(bar, Priority.ALWAYS);
            Label val = new Label(String.valueOf(e.getValue()));
            val.setMinWidth(28);
            val.setStyle("-fx-font-weight: 700; -fx-text-fill: #2563eb;");
            row.getChildren().addAll(name, bar, val);
            card.getChildren().add(row);
        }
        return card;
    }

    /* =========================================================
     * Right panel — Upcoming Appointments
     * ========================================================= */
    private VBox buildRightPanel() {
        VBox panel = new VBox(14);
        panel.getStyleClass().add("right-panel");
        panel.setPrefWidth(330);
        panel.setMinWidth(310);
        panel.getChildren().setAll(rebuildRightPanelContent());
        return panel;
    }

    private List<Node> rebuildRightPanelContent() {
        List<Node> nodes = new ArrayList<>();
        HBox head = new HBox(8);
        head.setAlignment(Pos.CENTER_LEFT);
        Label t = new Label("Prochains rendez-vous");
        t.getStyleClass().add("right-panel-title");
        Region s = new Region(); HBox.setHgrow(s, Priority.ALWAYS);
        Label today = new Label("Aujourd'hui ▾");
        today.getStyleClass().add("right-panel-today");
        head.getChildren().addAll(t, s, today);
        nodes.add(head);

        List<RendezVous> list = new RendezVousDao().listerProchains(6);
        if (list.isEmpty()) {
            Label empty = new Label("Aucun rendez-vous à venir.");
            empty.setStyle("-fx-text-fill: #64748b; -fx-font-size: 12px;");
            nodes.add(empty);
            return nodes;
        }
        DateTimeFormatter fmtTime = DateTimeFormatter.ofPattern("dd MMM ·  HH'h'mm", Locale.FRENCH);
        String[] palette = {"#3b82f6", "#10b981", "#f59e0b", "#ec4899", "#7c3aed", "#ef4444"};
        int i = 0;
        for (RendezVous rv : list) {
            String color = palette[i % palette.length]; i++;
            VBox cardA = new VBox(8);
            cardA.getStyleClass().add("appt-card");
            cardA.setCursor(javafx.scene.Cursor.HAND);

            String name = rv.getNomPatient();
            Patient patient = rv.getPatient();
            String initials = initialsOfChip(name);
            Label avatar = new Label(initials);
            avatar.getStyleClass().add("appt-avatar");
            avatar.setStyle("-fx-background-color: " + color + ";");

            Label nameL = new Label(name + " · " + (rv.getMedecin() == null ? "consultation"
                    : "Dr. " + rv.getMedecin().getNom()));
            nameL.getStyleClass().add("appt-name");
            nameL.setWrapText(true);
            Label timeL = new Label(fmtTime.format(rv.getDateHeure())
                    + (rv.getMedecin() != null && rv.getMedecin().getSpecialite() != null
                    ? "  ·  " + rv.getMedecin().getSpecialite().name() : ""));
            timeL.getStyleClass().add("appt-time");

            VBox txt = new VBox(2, nameL, timeL);
            HBox.setHgrow(txt, Priority.ALWAYS);

            HBox row = new HBox(10, avatar, txt);
            row.setAlignment(Pos.CENTER_LEFT);

            // Clickable chips
            Button chipDossier = new Button("Dossier médical");
            chipDossier.getStyleClass().add("appt-chip");
            chipDossier.setOnAction(e -> {
                e.consume();
                if (patient != null) openPatientDetail(patient);
            });

            Button chipReport = new Button("Rapports");
            chipReport.getStyleClass().addAll("appt-chip", "outline");
            chipReport.setOnAction(e -> {
                e.consume();
                showRdvReport(rv);
            });

            HBox chips = new HBox(8, chipDossier, chipReport);

            // Whole card → patient detail
            cardA.setOnMouseClicked(ev -> {
                if (patient != null) openPatientDetail(patient);
            });

            cardA.getChildren().addAll(row, chips);
            nodes.add(cardA);
        }
        return nodes;
    }

    /** Quick "report" modal — printable summary of an RDV. */
    private void showRdvReport(RendezVous rv) {
        javafx.scene.control.Alert a = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.INFORMATION);
        a.setTitle("Rapport — RDV " + rv.getId());
        a.setHeaderText("Compte-rendu de rendez-vous");
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("EEEE dd MMMM yyyy 'à' HH'h'mm", Locale.FRENCH);
        StringBuilder body = new StringBuilder();
        body.append("Patient   : ").append(rv.getNomPatient()).append("\n");
        body.append("Médecin   : ").append(rv.getNomMedecin());
        if (rv.getMedecin() != null && rv.getMedecin().getSpecialite() != null)
            body.append("  (").append(rv.getMedecin().getSpecialite().name()).append(")");
        body.append("\n");
        body.append("Date      : ").append(fmt.format(rv.getDateHeure())).append("\n");
        body.append("Référence : ").append(rv.getId()).append("\n\n");
        body.append("Motif de consultation : à compléter.\n");
        body.append("Constatations         : à compléter.\n");
        body.append("Diagnostic            : à compléter.\n");
        body.append("Prescriptions         : à compléter.\n\n");
        body.append("Émis le ").append(java.time.LocalDate.now())
            .append(" par ")
            .append(Session.current() == null ? "—" : Session.current().getDisplayName())
            .append(".");
        a.setContentText(body.toString());
        a.getDialogPane().setMinWidth(520);
        a.showAndWait();
    }

    private static String initialsOfChip(String s) {
        if (s == null || s.isBlank()) return "?";
        String[] parts = s.trim().split("\\s+");
        if (parts.length == 1) return parts[0].substring(0, 1).toUpperCase();
        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
    }
}
