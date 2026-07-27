package ui;

import dao.Audit;
import dao.UtilisateurDao;
import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import model.Utilisateur;

import java.util.Optional;
import java.util.function.Consumer;

public class LoginView extends StackPane {

    private final UtilisateurDao dao = new UtilisateurDao();

    public LoginView(Consumer<Utilisateur> onSuccess) {
        getStyleClass().add("login-bg");
        setPadding(new Insets(20));

        VBox card = new VBox(14);
        card.getStyleClass().add("login-card");
        card.setAlignment(Pos.TOP_LEFT);
        card.setMaxWidth(420);
        card.setMaxHeight(Region.USE_PREF_SIZE);

        Label logo = new Label("⚕");
        logo.getStyleClass().add("login-logo");
        Label brand = new Label("MedManager");
        brand.getStyleClass().add("login-brand");
        Label sub = new Label("Système de gestion hospitalière · v1.2");
        sub.getStyleClass().add("login-sub");

        Label title = new Label("Connexion");
        title.getStyleClass().add("login-title");
        Label hint = new Label("Veuillez saisir vos identifiants pour accéder à l'application.");
        hint.getStyleClass().add("login-hint");
        hint.setWrapText(true);

        Label userLbl = new Label("NOM D'UTILISATEUR");
        userLbl.getStyleClass().add("field-label");
        TextField userField = new TextField();
        userField.setPromptText("admin");
        userField.setMaxWidth(Double.MAX_VALUE);

        Label passLbl = new Label("MOT DE PASSE");
        passLbl.getStyleClass().add("field-label");
        PasswordField passField = new PasswordField();
        passField.setPromptText("••••••••");
        passField.setMaxWidth(Double.MAX_VALUE);

        TextField passVisible = new TextField();
        passVisible.setPromptText("••••••••");
        passVisible.setMaxWidth(Double.MAX_VALUE);
        passVisible.setManaged(false);
        passVisible.setVisible(false);
        passVisible.textProperty().bindBidirectional(passField.textProperty());

        CheckBox showPwd = new CheckBox("Afficher le mot de passe");
        showPwd.getStyleClass().add("login-check");
        showPwd.selectedProperty().addListener((o, ov, nv) -> {
            passField.setManaged(!nv); passField.setVisible(!nv);
            passVisible.setManaged(nv); passVisible.setVisible(nv);
        });

        Label error = new Label();
        error.getStyleClass().add("login-error");
        error.setVisible(false);
        error.setManaged(false);
        error.setWrapText(true);

        Button btnLogin = Components.primary("Se connecter →");
        btnLogin.setMaxWidth(Double.MAX_VALUE);
        btnLogin.setDefaultButton(true);

        Runnable doLogin = () -> {
            error.setVisible(false); error.setManaged(false);
            Components.clearErrors(userField, passField);
            String u = userField.getText() == null ? "" : userField.getText().trim();
            String p = passField.getText() == null ? "" : passField.getText();
            if (u.isEmpty()) Components.markError(userField, true);
            if (p.isEmpty()) Components.markError(passField, true);
            if (u.isEmpty() || p.isEmpty()) {
                showError(error, "Saisissez un nom d'utilisateur et un mot de passe.");
                return;
            }
            btnLogin.setDisable(true);
            btnLogin.setText("Connexion...");
            new Thread(() -> {
                Optional<Utilisateur> opt = dao.authenticate(u, p);
                javafx.application.Platform.runLater(() -> {
                    btnLogin.setDisable(false);
                    btnLogin.setText("Se connecter →");
                    if (opt.isPresent()) {
                        Session.set(opt.get());
                        Audit.log("LOGIN", "utilisateur", opt.get().getUsername(), null);
                        onSuccess.accept(opt.get());
                    } else {
                        Components.markError(userField, true);
                        Components.markError(passField, true);
                        passField.clear();
                        showError(error, "Identifiants incorrects. Réessayez.");
                    }
                });
            }, "login").start();
        };
        btnLogin.setOnAction(e -> doLogin.run());
        passField.setOnAction(e -> doLogin.run());
        passVisible.setOnAction(e -> doLogin.run());

        // Demo credentials block
        VBox demo = new VBox(2);
        demo.getStyleClass().add("login-demo");
        Label demoTitle = new Label("Identifiants de démonstration");
        demoTitle.getStyleClass().add("login-demo-title");
        Label demo1 = new Label("admin / admin123  ·  rôle ADMIN");
        Label demo2 = new Label("saidi / saidi123  ·  rôle MÉDECIN");
        Label demo3 = new Label("nurse / nurse123  ·  rôle INFIRMIER");
        Label demo4 = new Label("sara  / sara123   ·  rôle PATIENT");
        for (Node n : new Node[]{demo1, demo2, demo3, demo4}) {
            ((Label) n).getStyleClass().add("login-demo-line");
        }
        demo.getChildren().addAll(demoTitle, demo1, demo2, demo3, demo4);

        HBox header = new HBox(12, logo, new VBox(0, brand, sub));
        header.setAlignment(Pos.CENTER_LEFT);

        card.getChildren().addAll(
                header,
                new Region() {{ setMinHeight(6); }},
                title, hint,
                userLbl, userField,
                passLbl, passField, passVisible,
                showPwd,
                error,
                btnLogin,
                new Region() {{ setMinHeight(4); }},
                demo
        );

        getChildren().add(card);
        StackPane.setAlignment(card, Pos.CENTER);

        // Subtle fade-in
        setOpacity(0);
        FadeTransition ft = new FadeTransition(Duration.millis(280), this);
        ft.setFromValue(0); ft.setToValue(1);
        ft.play();

        // Autofocus username
        javafx.application.Platform.runLater(userField::requestFocus);
    }

    private static void showError(Label l, String msg) {
        l.setText("⚠  " + msg);
        l.setVisible(true);
        l.setManaged(true);
    }
}
