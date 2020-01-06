package org.culpan.mastertools;

import javafx.application.Application;
import javafx.stage.Stage;
import org.culpan.mastertools.dao.BaseDao;

import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;

public class MainApp extends Application {
    @Override
    public void start(Stage primaryStage) throws IOException {
        final Toolkit defaultToolkit = Toolkit.getDefaultToolkit();
        final URL imageResource = MainApp.class.getClassLoader().getResource("icon_64x64.png");
        final Image image = defaultToolkit.getImage(imageResource);

        //this is new since JDK 9
        final Taskbar taskbar = Taskbar.getTaskbar();

        try {
            //set icon for mac os (and other systems which do support this method)
            taskbar.setIconImage(image);
        } catch (final UnsupportedOperationException e) {
            System.out.println("The os does not support: 'taskbar.setIconImage'");
        } catch (final SecurityException e) {
            System.out.println("There was a security exception for: 'taskbar.setIconImage'");
        }

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

