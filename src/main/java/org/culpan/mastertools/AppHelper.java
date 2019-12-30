package org.culpan.mastertools;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class AppHelper {
    public static Stage loadFxmlDialog(Stage primaryStage, URL xmlUrl, String title, int width, int height, boolean modal) throws IOException {
        primaryStage.setTitle(title);
        primaryStage.setWidth(width);
        primaryStage.setHeight(height);

        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(xmlUrl);
        Parent root = loader.load();

        if (modal) {
            primaryStage.initModality(Modality.APPLICATION_MODAL);
        }

        primaryStage.setScene(new Scene(root));
        return primaryStage;
    }
}
