module medmanagerv1_1 {
    requires java.sql;
    requires javafx.graphics;
    requires javafx.controls;

    exports ui;
    exports main;
    opens model to javafx.base;
}
