module nasar.mustafa.warehub {
    requires javafx.fxml;

    requires com.dlsc.formsfx;
    requires java.sql;
    requires commons.dbutils;
    requires org.mybatis;
    requires org.controlsfx.controls;
    requires java.desktop;

    opens nasar.mustafa.warehub to javafx.fxml;
    exports nasar.mustafa.warehub;
    exports nasar.mustafa.warehub.controllers;
    opens nasar.mustafa.warehub.controllers to javafx.fxml;
    exports nasar.mustafa.warehub.tableViewModels;
    opens nasar.mustafa.warehub.tableViewModels to javafx.fxml;
}