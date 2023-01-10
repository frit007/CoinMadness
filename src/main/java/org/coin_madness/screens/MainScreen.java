package org.coin_madness.screens;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import org.coin_madness.helpers.ConnectionManager;
import org.coin_madness.helpers.Action;

import java.io.IOException;

import static java.lang.Long.MAX_VALUE;

public class MainScreen extends GridPane {

    private ConnectionManager connectionManager;
    private Action onEnterLobby;

    public MainScreen(ConnectionManager connectionManager, String errorMessage) {
        this.connectionManager = connectionManager;
        init(errorMessage);
    }
    public void setOnEnterLobby(Action onEnterLobby) {
        this.onEnterLobby = onEnterLobby;
    }

    public void init(String errorMessage) {
        //Title
        Label title = new Label("Coin Madness");
        title.setTextFill(Color.BLACK);
        title.setStyle("-fx-font: 40px arial;");
        title.setPadding(new Insets(20,60,20,60));


        Text error = new Text(errorMessage);
        error.setFill(Color.RED);
        error.setStyle("-fx-font: 12px arial;");


        //Setting the vertical and horizontal gaps between the columns
        setVgap(6);
        setHgap(6);

        //Setting the Grid alignment
        setAlignment(Pos.CENTER);

        Label ipLabel = new Label("Remote ip:");
        ipLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 15));

        TextField ip = new TextField();
        ip.setText("127.0.0.1");
        ip.setPromptText("ip address");
        ip.setFont(Font.font("Verdana", FontWeight.BOLD, 20));
        ip.setMinWidth(300);

        Button hostButton = createButton("Host");

        hostButton.setOnMouseClicked(mouse -> {
            connectionManager.host();
            onEnterLobby.handle();
        });

        Button joinButton = createButton("Join");
        joinButton.setOnMouseClicked(mouse -> {
            try {
                connectionManager.join(ip.getText());
                onEnterLobby.handle();
            } catch (IOException e) {
                e.printStackTrace();
                error.setText("Unable to connect. Is the ip correct?");
            }
        });


        add(title, 0, 0, 5, 1);
        GridPane.setHalignment(title, HPos.CENTER);
        add(error, 0, 1, 5, 1);
        GridPane.setHalignment(error, HPos.CENTER);
        joinButton.prefWidthProperty().bind(ip.widthProperty());
        hostButton.prefWidthProperty().bind(ip.widthProperty());

        add(ipLabel,3,3);
        add(ip, 3,4);
        add(joinButton,3,5);
        add(hostButton,1,5);

    }

    private Button createButton(String text) {
        Button button = new Button(text);
        button.setPadding(new Insets(15,15,15,15));
        button.setStyle("-fx-font-size: 20px;-fx-background-color: #4447fe; ");
        button.setTextFill(Color.WHITE);

        return button;
    }



}
