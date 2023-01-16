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
import org.coin_madness.messages.GlobalMessage;
import org.coin_madness.model.*;
import javafx.scene.text.*;
import org.jspace.ActualField;
import org.jspace.FormalField;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class GameScreen extends BorderPane {

    private static final int AMOUNT_OF_COINS = 50;
    private static final int AMOUNT_OF_TRAPHOLES = 10;
    public static final Color BACKGROUND = Color.GRAY;
    private ScrollPane scrollPane;
    private GridPane mapView;
    private double tileSize;
    ArrayList<FieldView> views = new ArrayList<>();
    private GameStatusBar gameStatusBar;
    private Scene scene;
    private Field[][] map;
    protected GameState gameState = new GameState();
    public GameScreen(Stage stage, Scene scene, Field[][] map, ImageLibrary graphics, ConnectionManager connectionManager, Consumer<GameState> onGameEnd, Consumer<String> onGameError) {
        this.scene = scene;
        this.map = map;
        gameStatusBar = new GameStatusBar(graphics);

        gameState.connectionManager = connectionManager;
        gameState.map = map;

        createPlayers();

        connectionManager.setOnClientTimeout((disconnectReason) -> {
            Platform.runLater(() -> {
                onGameError.accept("Sorry, lost connection to the server");
            });
            connectionManager.setOnClientDisconnect(null);
            new Thread(() -> {
                gameState.gameThreads.cleanup();
                connectionManager.stop();
            }).start();
        });

        //TODO: move
        Function<Object[], Coin> createCoin = (o) -> new Coin((int) o[1], (int) o[2], gameState.coinClient);
        Function<Object[], Chest> createChest = (o) -> new Chest((int) o[1], (int) o[2]);
        Function<Object[], Traphole> createTraphole = (o) -> new Traphole((int) o[1], (int) o[2], gameState);

        gameState.coinClient = new CoinClient(connectionManager.getCoinSpace(), gameState, createCoin);
        gameState.chestClient = new StaticEntityClient<>(connectionManager.getChestSpace(), gameState, createChest);
        gameState.trapholeClient = new StaticEntityClient<>(connectionManager.getTrapholeSpace(), gameState, createTraphole);
        gameState.deathClient = new DeathClient(gameState, onGameEnd);
        gameState.enemyClient = new EnemyClient(gameState);

        gameState.coinClient.listenForChanges();
        gameState.chestClient.listenForChanges();
        gameState.trapholeClient.listenForChanges();
        gameState.enemyClient.listenForChanges();

        if (connectionManager.isHost()) {
            StaticEntityPlacer placer = new StaticEntityPlacer();
            List<Coin> placedCoins = placer.placeCoins(map, AMOUNT_OF_COINS);
            List<Chest> placedChests = placer.placeChests(map);
            List<Traphole> placedTrapholes = placer.placeTrapholes(gameState, AMOUNT_OF_TRAPHOLES);

            StaticEntityServer<Coin> coinServer = new StaticEntityServer<>(gameState, connectionManager.getCoinSpace(), createCoin);
            StaticEntityServer<Chest> chestServer = new StaticEntityServer<>(gameState, connectionManager.getChestSpace(), createChest);
            StaticEntityServer<Traphole> trapholeServer = new StaticEntityServer<>(gameState, connectionManager.getTrapholeSpace(), createTraphole);
            EnemyServer enemyServer = new EnemyServer(gameState);

            coinServer.listenForEntityRequests(placedCoins);
            chestServer.listenForEntityRequests(placedChests);
            trapholeServer.listenForEntityRequests(placedTrapholes);
            enemyServer.createEnemies();

            gameState.gameThreads.startHandledThread("Place coins, chest and traps",() -> {
                coinServer.add(placedCoins);
                chestServer.add(placedChests);
                trapholeServer.add(placedTrapholes);
            });
        }
        ///

        new GameController(scene, connectionManager, gameState);
        Group mazeView = new Group();

        // HBox topBar = new HBox();
        // topBar.getChildren().add(new Text(10,0,"Coins: "));
        int preferredGameStatusBarHeight = 30;
        gameStatusBar.setPrefHeight(preferredGameStatusBarHeight);
        gameStatusBar.addPlayer(gameState.localPlayer);
        for (Player networkPlayer: gameState.networkedPlayers.values()) {
            gameStatusBar.addPlayer(networkPlayer);
        }

        mapView = new GridPane();
        mapView.setAlignment(Pos.CENTER);
        mapView.setSnapToPixel(false);
        mapView.setBackground(new Background(new BackgroundFill(BACKGROUND, CornerRadii.EMPTY, Insets.EMPTY)));

        HashMap<Class, Drawer> drawerMap = new HashMap<>();
        drawerMap.put(Coin.class, new CoinDrawer(graphics));
        drawerMap.put(Chest.class, new ChestDrawer(graphics));
        drawerMap.put(Traphole.class, new TrapholeDrawer(graphics));
        drawerMap.put(Player.class, new PlayerDrawer(graphics, mazeView));
        drawerMap.put(Enemy.class, new PlayerDrawer(graphics, mazeView));

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
    private void createPlayers() {
        try {
            List<Object[]> players = gameState.connectionManager.getLobby().queryAll(
                    new ActualField(GlobalMessage.CLIENTS),
                    new FormalField(Integer.class),
                    new FormalField(Integer.class)
            );
            for(Object[] playerInfo : players) {
                int clientId = (int) playerInfo[1];
                int spriteId = (int) playerInfo[2];
                boolean localPlayer = clientId == gameState.connectionManager.getClientId();
                Player player = new Player(clientId, clientId, 3, spriteId, localPlayer);

                if(localPlayer) {
                    gameState.localPlayer = player;
                } else {
                    gameState.networkedPlayers.put(clientId, player);
                }

                map[player.getX()][player.getY()].addEntity(player);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    private void resizeStage(Double sceneHeight, int mazeRows) {

        tileSize = Math.floor(sceneHeight / mazeRows);
        gameStatusBar.setPrefHeight(scene.getHeight() - map[0].length * tileSize);
        for (FieldView view : views) {
            view.setSideLength(tileSize);
        }


    }
    
}
