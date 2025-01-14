module nasar.mustafa.warehub {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.dlsc.formsfx;
    requires javafx.graphics;
    requires java.sql;
    requires commons.dbutils;
    requires java.desktop;

    opens nasar.mustafa.warehub to javafx.fxml;
    exports nasar.mustafa.warehub;
}