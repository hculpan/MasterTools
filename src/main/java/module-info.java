module MasterTools.main {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires java.sql;
    requires sqlite.jdbc;

    exports org.culpan.mastertools.controllers;
    opens org.culpan.mastertools.controllers to javafx.fxml;
    opens org.culpan.mastertools.model to javafx.base;
    opens org.culpan.mastertools to javafx.graphics;
}