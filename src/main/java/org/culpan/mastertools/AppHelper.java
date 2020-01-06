package org.culpan.mastertools;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
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

    public static void showExceptionInfo(Throwable t, String title) {
        showExceptionInfo(t, title,"");
    }

    public static void showExceptionInfo(Throwable t, String title, String header) {
        t.printStackTrace();
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(t.getLocalizedMessage());

// Create expandable Exception.
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        String exceptionText = sw.toString();

        Label label = new Label("The exception stacktrace was:");

        TextArea textArea = new TextArea(exceptionText);
        textArea.setEditable(false);
        textArea.setWrapText(true);

        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(label, 0, 0);
        expContent.add(textArea, 0, 1);

// Set expandable Exception into the dialog pane.
        alert.getDialogPane().setExpandableContent(expContent);

        alert.showAndWait();
    }
}
