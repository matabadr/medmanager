package ui;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.function.Function;

/**
 * Tiny UI helpers reused across all views: styled buttons, labeled fields,
 * a searchable table wrapper, and a slide-in toast notification.
 */
public final class Components {

    private Components() {}

    /* -------------------- buttons -------------------- */

    public static Button primary(String text) {
        Button b = new Button(text);
        b.getStyleClass().addAll("button", "primary-button");
        return b;
    }

    public static Button ghost(String text) {
        Button b = new Button(text);
        b.getStyleClass().addAll("button", "ghost-button");
        return b;
    }

    public static Button danger(String text) {
        Button b = new Button(text);
        b.getStyleClass().addAll("button", "danger-button");
        return b;
    }

    /* -------------------- field with label -------------------- */

    /** Vertical stack: small uppercase label on top, control below. */
    public static VBox labeled(String label, javafx.scene.Node control) {
        Label l = new Label(label.toUpperCase());
        l.getStyleClass().add("field-label");
        VBox box = new VBox(4, l, control);
        if (control instanceof javafx.scene.layout.Region r) {
            HBox.setHgrow(box, Priority.ALWAYS);
            r.setMaxWidth(Double.MAX_VALUE);
        }
        return box;
    }

    /* -------------------- searchable table -------------------- */

    /**
     * Binds a TextField to a TableView so typing filters the rows.
     * The keyExtractor builds the searchable string for each row
     * (e.g. p -> p.getNom() + " " + p.getPrenom()).
     */
    public static <T> TextField attachSearch(TableView<T> table,
                                             ObservableList<T> source,
                                             Function<T, String> keyExtractor) {
        TextField search = new TextField();
        search.setPromptText("🔍  Rechercher...");
        search.getStyleClass().add("search-box");
        HBox.setHgrow(search, Priority.ALWAYS);

        FilteredList<T> filtered = new FilteredList<>(source, x -> true);
        search.textProperty().addListener((obs, oldV, newV) -> {
            String needle = newV == null ? "" : newV.trim().toLowerCase();
            filtered.setPredicate(item -> {
                if (needle.isEmpty()) return true;
                String hay = keyExtractor.apply(item);
                return hay != null && hay.toLowerCase().contains(needle);
            });
        });

        SortedList<T> sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(table.comparatorProperty());
        table.setItems(sorted);
        return search;
    }

    /* -------------------- toast -------------------- */

    /** Pops a transient notification at the bottom of the supplied StackPane root. */
    public static void toast(StackPane root, String message, ToastKind kind) {
        Label l = new Label(message);
        l.getStyleClass().add("toast");
        l.getStyleClass().add(kind.cssClass);
        StackPane.setAlignment(l, Pos.BOTTOM_CENTER);
        StackPane.setMargin(l, new Insets(0, 0, 28, 0));
        l.setOpacity(0);
        l.setTranslateY(20);

        root.getChildren().add(l);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(180), l);
        fadeIn.setFromValue(0); fadeIn.setToValue(1);
        TranslateTransition slideIn = new TranslateTransition(Duration.millis(220), l);
        slideIn.setFromY(20); slideIn.setToY(0);
        PauseTransition hold = new PauseTransition(Duration.millis(2200));
        FadeTransition fadeOut = new FadeTransition(Duration.millis(260), l);
        fadeOut.setFromValue(1); fadeOut.setToValue(0);

        SequentialTransition seq = new SequentialTransition(
            new javafx.animation.ParallelTransition(fadeIn, slideIn),
            hold, fadeOut);
        seq.setOnFinished(e -> root.getChildren().remove(l));
        seq.play();
    }

    public enum ToastKind {
        INFO(""), SUCCESS("success"), ERROR("error");
        final String cssClass;
        ToastKind(String c) { this.cssClass = c; }
    }

    /* -------------------- validation -------------------- */

    /** Toggles the .error style class on a TextField based on a predicate. */
    public static boolean validateRequired(TextField field) {
        boolean ok = field.getText() != null && !field.getText().trim().isEmpty();
        markError(field, !ok);
        return ok;
    }

    public static void markError(javafx.scene.control.Control c, boolean isError) {
        c.getStyleClass().remove("error");
        if (isError) c.getStyleClass().add("error");
    }

    public static void clearErrors(javafx.scene.control.Control... fields) {
        for (javafx.scene.control.Control c : fields) c.getStyleClass().remove("error");
    }

    /* -------------------- observable list shortcut -------------------- */

    public static <T> ObservableList<T> obs(Iterable<T> items) {
        ObservableList<T> list = FXCollections.observableArrayList();
        items.forEach(list::add);
        return list;
    }

    /* -------------------- stats badge -------------------- */

    public static Label statBadge(String text) {
        Label l = new Label(text);
        l.getStyleClass().add("stat-badge");
        return l;
    }

    /* -------------------- confirmation dialog -------------------- */

    public static boolean confirm(String header, String body) {
        javafx.scene.control.Alert a = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.CONFIRMATION);
        a.setTitle("Confirmation");
        a.setHeaderText(header);
        a.setContentText(body);
        return a.showAndWait()
                .filter(b -> b == javafx.scene.control.ButtonType.OK)
                .isPresent();
    }

    /* -------------------- CSV export -------------------- */

    /**
     * Export the rows visible in a TableView to a CSV file chosen by the user.
     * Uses the column headers + PropertyValueFactory mapping.
     */
    public static <T> void exportCsv(javafx.scene.control.TableView<T> table,
                                     String defaultName,
                                     javafx.stage.Window owner) {
        javafx.stage.FileChooser fc = new javafx.stage.FileChooser();
        fc.setTitle("Exporter en CSV");
        fc.setInitialFileName(defaultName);
        fc.getExtensionFilters().add(
                new javafx.stage.FileChooser.ExtensionFilter("Fichier CSV (*.csv)", "*.csv"));
        java.io.File file = fc.showSaveDialog(owner);
        if (file == null) return;

        try (java.io.PrintWriter pw = new java.io.PrintWriter(
                new java.io.OutputStreamWriter(new java.io.FileOutputStream(file),
                        java.nio.charset.StandardCharsets.UTF_8))) {
            // BOM so Excel detects UTF-8
            pw.write('﻿');
            // Header
            StringBuilder header = new StringBuilder();
            for (int i = 0; i < table.getColumns().size(); i++) {
                if (i > 0) header.append(';');
                header.append(escape(table.getColumns().get(i).getText()));
            }
            pw.println(header);

            // Rows
            for (T item : table.getItems()) {
                StringBuilder row = new StringBuilder();
                for (int i = 0; i < table.getColumns().size(); i++) {
                    if (i > 0) row.append(';');
                    Object v = table.getColumns().get(i).getCellData(item);
                    row.append(escape(v == null ? "" : v.toString()));
                }
                pw.println(row);
            }
        } catch (java.io.IOException e) {
            javafx.scene.control.Alert err = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.ERROR,
                    "Échec de l'export CSV : " + e.getMessage());
            err.showAndWait();
        }
    }

    private static String escape(String s) {
        if (s == null) return "";
        boolean needsQuotes = s.indexOf(';') >= 0 || s.indexOf('"') >= 0
                || s.indexOf('\n') >= 0 || s.indexOf('\r') >= 0;
        if (needsQuotes) return "\"" + s.replace("\"", "\"\"") + "\"";
        return s;
    }
}
