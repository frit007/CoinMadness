package org.openjfx;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.jspace.QueueSpace;
import org.jspace.SequentialSpace;
import org.jspace.Space;


/**
 * JavaFX App
 */
public class App extends Application {

    @Override
    public void start(Stage stage) {
        Space space = new QueueSpace();

        var javaVersion = SystemInfo.javaVersion();
        var javafxVersion = SystemInfo.javafxVersion();

        var label = new Label("Hello, JavaFX " + javafxVersion + ", running on Java " + javaVersion + ".");
        Group stackPane = new Group(label);
        var scene = new Scene(stackPane, 640, 480);

        var image = new Image("tmp.png");
        ImageView imageView = new ImageView(image);
        imageView.setX(10);
        stackPane.getChildren().add(imageView);

        stage.setScene(scene);
        stage.show();

        new Thread(() -> {
            double x = 0;
            while( true) {
                try {
                    double finalX = x;
                    Platform.runLater(() -> {
                        System.out.println(finalX);
                        imageView.setX(finalX);
                    });
                    x += 0.1;
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    public static void main(String[] args) {
        launch();
    }

}