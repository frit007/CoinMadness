package org.coin_madness.screens;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.coin_madness.components.*;
import org.coin_madness.controller.GameController;
import org.coin_madness.helpers.ImageLibrary;
import org.coin_madness.model.*;
import javafx.scene.text.*;

import java.util.*;

public class GameScreen extends BorderPane {

    private static final int AMOUNT_OF_COINS = 50;
    private static final int AMOUNT_OF_TRAPHOLES = 10;
    public static final Color BACKGROUND = Color.GRAY;
    private ScrollPane scrollPane;
    private GridPane mapView;
    private double tileSize;
    ArrayList<FieldView> views = new ArrayList<>();
    private List<Coin> coins = new ArrayList<>();
    private List<Chest> chests = new ArrayList<>();
    private List<Traphole> trapholes = new ArrayList<>();

    public GameScreen(Stage stage, Scene scene, Player player, Field[][] map, ImageLibrary graphics) {

        new GameController(player, scene, map);
        Group mazeView = new Group();

        HBox topBar = new HBox();
        topBar.getChildren().add(new Text(10,0,"Coins: "));

        mapView = new GridPane();
        mapView.setAlignment(Pos.CENTER);
        mapView.setSnapToPixel(false);
        mapView.setBackground(new Background(new BackgroundFill(BACKGROUND, CornerRadii.EMPTY, Insets.EMPTY)));

        String[] playerIds = {"0"};
        HashMap<Class, Drawer> drawerMap = new HashMap<>();
        drawerMap.put(Coin.class, new CoinDrawer(graphics));
        drawerMap.put(Chest.class, new ChestDrawer(graphics));
        drawerMap.put(Traphole.class, new TrapholeDrawer(graphics));
        drawerMap.put(Player.class, new PlayerDrawer(playerIds, graphics, mazeView));

        for (Field[] row : map) {
            for(Field field : row) {
                FieldView fieldView = new FieldView(field, graphics, drawerMap);
                fieldView.updateView();
                views.add(fieldView);
                mapView.add(fieldView, field.getX(), field.getY());
            }
        }

        StaticEntityPlacer placer = new StaticEntityPlacer();
        coins = placer.placeCoins(map, AMOUNT_OF_COINS);
        trapholes = placer.placeTrapholes(map, AMOUNT_OF_TRAPHOLES);
        chests = placer.placeChests(map);
        map[player.getX()][player.getY()].addEntity(player);

        scrollPane = new ScrollPane(mapView);
        scrollPane.getStyleClass().add("edge-to-edge");
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setSnapToPixel(false);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        mazeView.getChildren().add(scrollPane);

        setAlignment(mazeView, Pos.CENTER);
        setTop(topBar);
        setCenter(mazeView);

        resizeStage(scene.getHeight() - topBar.getHeight(), map.length);

        stage.heightProperty().addListener((obs, oldVal, newVal) -> {
            resizeStage(scene.getHeight() - topBar.getHeight(), map.length);
            stage.setWidth((tileSize + 1) * map[0].length);
        });

    }

    private void resizeStage(Double sceneHeight, int mazeRows) {
        tileSize = Math.floor(sceneHeight / mazeRows);
        for (FieldView view : views) {
            view.setSideLength(tileSize);
        }
    }

}
