package org.coin_madness.screens;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.coin_madness.components.*;
import org.coin_madness.controller.GameController;
import org.coin_madness.helpers.ConnectionManager;
import org.coin_madness.helpers.ImageLibrary;
import org.coin_madness.helpers.ScopedThreads;
import org.coin_madness.model.*;
import javafx.scene.text.*;

import java.util.*;
import java.util.function.Function;

public class GameScreen extends BorderPane {

    private static final int AMOUNT_OF_COINS = 50;
    private static final int AMOUNT_OF_TRAPHOLES = 10;
    public static final Color BACKGROUND = Color.GRAY;
    private ScrollPane scrollPane;
    private GridPane mapView;
    private double tileSize;
    ArrayList<FieldView> views = new ArrayList<>();
    ScopedThreads gameScreenThreads = new ScopedThreads(() -> {});
    private StaticEntityClient<Coin> coinClient;
    private GameStatusBar gameStatusBar;
    private Scene scene;
    private Field[][] map;
    private Stage stage;
 
    
    public GameScreen(Stage stage, Scene scene, Field[][] map, ImageLibrary graphics, ConnectionManager connectionManager) {
        this.scene = scene;
        this.map = map;
        this.stage = stage;
        gameStatusBar = new GameStatusBar(graphics);

        //TODO: move
        Function<Object[], Coin> createCoin = (o) -> new Coin((int) o[1], (int) o[2], coinClient);
        Function<Object[], Chest> createChest = (o) -> new Chest((int) o[1], (int) o[2]);
        Function<Object[], Traphole> createTraphole = (o) -> new Traphole((int) o[1], (int) o[2]);

        coinClient = new StaticEntityClient<>(connectionManager, connectionManager.getCoinSpace(), gameScreenThreads, map, createCoin);
        StaticEntityClient<Chest> chestClient = new StaticEntityClient<>(connectionManager, connectionManager.getChestSpace(), gameScreenThreads, map, createChest);
        StaticEntityClient<Traphole> trapholeClient = new StaticEntityClient<>(connectionManager, connectionManager.getTrapholeSpace(), gameScreenThreads, map, createTraphole);

        coinClient.listenForChanges();
        chestClient.listenForChanges();
        trapholeClient.listenForChanges();

        int id = connectionManager.getClientId();
        Player player = new Player(id, id,3, true);
        map[player.getX()][player.getY()].addEntity(player);

        if (connectionManager.isHost()) {
            StaticEntityPlacer placer = new StaticEntityPlacer();
            List<Coin> placedCoins = placer.placeCoins(map, AMOUNT_OF_COINS);
            List<Chest> placedChests = placer.placeChests(map);
            List<Traphole> placedTrapholes = placer.placeTrapholes(map, AMOUNT_OF_TRAPHOLES);

            StaticEntityServer<Coin> coinServer = new StaticEntityServer<>(connectionManager, connectionManager.getCoinSpace(), gameScreenThreads, createCoin);
            StaticEntityServer<Chest> chestServer = new StaticEntityServer<>(connectionManager, connectionManager.getChestSpace(), gameScreenThreads, createChest);
            StaticEntityServer<Traphole> trapholeServer = new StaticEntityServer<>(connectionManager, connectionManager.getTrapholeSpace(), gameScreenThreads, createTraphole);

            coinServer.listenForEntityRequests(placedCoins);
            chestServer.listenForEntityRequests(placedChests);
            trapholeServer.listenForEntityRequests(placedTrapholes);

            gameScreenThreads.startHandledThread(() -> {
                coinServer.add(placedCoins);
                chestServer.add(placedChests);
                trapholeServer.add(placedTrapholes);
            });
        }
        ///

        new GameController(player, scene, map, connectionManager, gameStatusBar);
        Group mazeView = new Group();

        // HBox topBar = new HBox();
        // topBar.getChildren().add(new Text(10,0,"Coins: "));
        int preferredGameStatusBarHeight = 30;
        gameStatusBar.setPrefHeight(preferredGameStatusBarHeight);
        gameStatusBar.addPlayer(player);

        mapView = new GridPane();
        mapView.setAlignment(Pos.CENTER);
        mapView.setSnapToPixel(false);
        mapView.setBackground(new Background(new BackgroundFill(BACKGROUND, CornerRadii.EMPTY, Insets.EMPTY)));

        HashMap<Class, Drawer> drawerMap = new HashMap<>();
        drawerMap.put(Coin.class, new CoinDrawer(graphics));
        drawerMap.put(Chest.class, new ChestDrawer(graphics));
        drawerMap.put(Traphole.class, new TrapholeDrawer(graphics));
        PlayerDrawer playerDrawer = new PlayerDrawer(graphics, mazeView);
        drawerMap.put(Player.class, playerDrawer);
        drawerMap.put(Enemy.class, playerDrawer);

        for (Field[] row : map) {
            for(Field field : row) {
                FieldView fieldView = new FieldView(field, graphics, drawerMap);
                fieldView.updateView();
                views.add(fieldView);
                mapView.add(fieldView, field.getX(), field.getY());
            }
        }

        scrollPane = new ScrollPane(mapView);
        scrollPane.getStyleClass().add("edge-to-edge");
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setSnapToPixel(false);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        mazeView.getChildren().add(scrollPane);
        setAlignment(mazeView, Pos.CENTER);
        setTop(gameStatusBar);
        setCenter(mazeView);

        stage.heightProperty().addListener((obs, oldVal, newVal) -> {
            resizeStage(scene.getHeight() - preferredGameStatusBarHeight, map.length);
            double cellWidth = mapView.getCellBounds(0,0).getWidth();
            stage.setWidth(cellWidth * map[0].length);
            gameStatusBar.root.setPrefWidth(cellWidth * map[0].length);
        });

        Platform.runLater(() -> {
            resizeStage(scene.getHeight() - preferredGameStatusBarHeight, map.length);
            gameStatusBar.root.setPrefWidth(scene.getWidth());
        });

    }

    private void resizeStage(Double sceneHeight, int mazeRows) {

        tileSize = Math.floor(sceneHeight / mazeRows);
        gameStatusBar.setPrefHeight(scene.getHeight() - map[0].length * tileSize);
        for (FieldView view : views) {
            view.setSideLength(tileSize);
        }


    }
    
}
