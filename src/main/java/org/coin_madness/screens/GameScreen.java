package org.coin_madness.screens;

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
import org.coin_madness.messages.GlobalMessage;
import org.coin_madness.messages.StaticEntityMessage;
import org.coin_madness.model.*;
import javafx.scene.text.*;
import org.jspace.ActualField;
import org.jspace.FormalField;

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
    
    public GameScreen(Stage stage, Scene scene, Field[][] map, ImageLibrary graphics, ConnectionManager connectionManager) {

        //TODO: move
        Function<Object[], Coin> createCoin = (o) -> new Coin((int) o[1], (int) o[2]);
        Function<Object[], Chest> createChest = (o) -> new Chest((int) o[1], (int) o[2]);
        Function<Object[], Traphole> createTraphole = (o) -> new Traphole((int) o[1], (int) o[2]);

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

        StaticEntityClient<Coin> coinClient = new StaticEntityClient<>(connectionManager, connectionManager.getCoinSpace(), gameScreenThreads, map, createCoin);
        StaticEntityClient<Chest> chestClient = new StaticEntityClient<>(connectionManager, connectionManager.getChestSpace(), gameScreenThreads, map, createChest);
        StaticEntityClient<Traphole> trapholeClient = new StaticEntityClient<>(connectionManager, connectionManager.getTrapholeSpace(), gameScreenThreads, map, createTraphole);

        coinClient.listenForChanges();
        chestClient.listenForChanges();
        trapholeClient.listenForChanges();

        int id = connectionManager.getClientId();
        Player player = new Player(id, id,3, coinClient, chestClient, trapholeClient);
        map[player.getX()][player.getY()].addEntity(player);
        ///

        new GameController(player, scene, map, connectionManager);
        Group mazeView = new Group();

        HBox topBar = new HBox();
        topBar.getChildren().add(new Text(10,0,"Coins: "));

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
        drawerMap.put(NetworkPlayer.class, playerDrawer);
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
        setTop(topBar);
        setCenter(mazeView);

        resizeStage(scene.getHeight() - topBar.getHeight(), map.length);

        stage.heightProperty().addListener((obs, oldVal, newVal) -> {
            resizeStage(scene.getHeight() - topBar.getHeight(), map.length);
            double cellWidth = mapView.getCellBounds(0,0).getWidth();
            stage.setWidth(cellWidth * map[0].length);
        });

    }

    private void resizeStage(Double sceneHeight, int mazeRows) {
        tileSize = Math.floor(sceneHeight / mazeRows);
        for (FieldView view : views) {
            view.setSideLength(tileSize);
        }
    }
    
}
