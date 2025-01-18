module nasar.mustafa.warehub {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.dlsc.formsfx;
    requires javafx.graphics;
    requires java.sql;
    requires commons.dbutils;
    requires java.desktop;
    requires org.mybatis;

    opens nasar.mustafa.warehub to javafx.fxml;
    exports nasar.mustafa.warehub;
    exports nasar.mustafa.warehub.controllers;
    opens nasar.mustafa.warehub.controllers to javafx.fxml;
}