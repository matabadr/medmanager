package main;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import model.Utilisateur;
import ui.DashboardView;
import ui.LoginView;
import ui.PatientPortalView;
import ui.Session;

public class Main extends Application {

    private Stage stage;
    private StackPane root;

    @Override
    public void start(Stage primaryStage) {
        this.stage = primaryStage;
        this.root = new StackPane();
        root.getStyleClass().add("app-root");
        DashboardView.setToastHost(root);

        // DAO audit layer needs to know who is logged in
        dao.Audit.setCurrentUserSupplier(ui.Session::current);

        Scene scene = new Scene(root, 1180, 740);
        String css = getClass().getResource("/resources/styles.css") != null
                ? getClass().getResource("/resources/styles.css").toExternalForm()
                : null;
        if (css != null) scene.getStylesheets().add(css);
        else Logger.warn("styles.css introuvable — l'application va s'afficher sans thème");

        primaryStage.setTitle("MedManager v1.2 — Connexion");
        primaryStage.setMinWidth(1000);
        primaryStage.setMinHeight(640);
        primaryStage.setScene(scene);
        primaryStage.show();

        showLogin();
    }

    private void showLogin() {
        stage.setTitle("MedManager v1.2 — Connexion");
        Session.clear();
        LoginView login = new LoginView(user -> showDashboard());
        root.getChildren().setAll(login);
    }

    private void showDashboard() {
        Utilisateur u = Session.current();
        if (u != null && u.getRole() == Utilisateur.Role.PATIENT) {
            stage.setTitle("MedManager v1.2 — Espace patient · " + u.getDisplayName());
            PatientPortalView portal = new PatientPortalView();
            portal.setLogoutHandler(this::showLogin);
            root.getChildren().setAll(portal);
        } else {
            stage.setTitle("MedManager v1.2 — " + (u == null ? "" : u.getDisplayName()));
            DashboardView dash = new DashboardView();
            dash.setLogoutHandler(this::showLogin);
            root.getChildren().setAll(dash);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
