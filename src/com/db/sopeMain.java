package com.db;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class sopeMain extends Application {

    private static Stage primaryStage;
    public static Stage getPrimaryStage() {
        return sopeMain.primaryStage;
    }
    public sopeMain() {
    }

    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(this.getClass().getResource("base.fxml"));
        primaryStage.setTitle("Sales Operations Console");
        primaryStage.setScene(new Scene(root, 500, 250));
        Image icon = new Image(getClass().getResourceAsStream("/img/soap.png"));
        primaryStage.getIcons().add(icon);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
