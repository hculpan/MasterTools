package org.culpan.mastertools;

import javafx.application.Application;
import javafx.stage.Stage;
import org.culpan.mastertools.dao.BaseDao;

import java.io.IOException;
import java.sql.SQLException;

public class MainApp extends Application {
    @Override
    public void start(Stage primaryStage) throws IOException {
        AppHelper.loadFxmlDialog(
                primaryStage,
                getClass().getResource("/MainDialog.fxml"),
                "Master Tools",
                1105,
                658,
                false);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) {
        try {
            launch();
        } catch (Throwable t) {
            t.printStackTrace();
        }

        try {
            BaseDao.closeDb();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

}

