module nasar.mustafa.warehub {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.dlsc.formsfx;

    opens nasar.mustafa.warehub to javafx.fxml;
    exports nasar.mustafa.warehub;
}