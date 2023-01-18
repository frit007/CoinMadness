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
import org.coin_madness.messages.GlobalMessage;
import org.coin_madness.model.*;
import org.jspace.ActualField;
import org.jspace.FormalField;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class GameScreen extends BorderPane {

    private static final int INTITIAL_AMOUNT_OF_CHESTS = 4;
    private static final int INTITIAL_AMOUNT_OF_COINS = 25;
    private static final int INTITIAL_AMOUNT_OF_TRAPHOLES = 10;
    public static final Color BACKGROUND = Color.GRAY;
    private ScrollPane scrollPane;
    private GridPane mapView;
    private double tileSize;
    ArrayList<FieldView> views = new ArrayList<>();
    private GameStatusBar gameStatusBar;
    private Scene scene;
    private Field[][] map;
    protected GameState gameState = new GameState();
    public GameScreen(Stage stage, Scene scene, GameSettings gameSettings,Field[][] map, ImageLibrary graphics, ConnectionManager connectionManager, Consumer<GameState> onGameEnd, Consumer<String> onGameError) {
        this.scene = scene;
        this.map = map;
        gameState.settings = gameSettings;
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
        connectionManager.setOnClientDisconnect((disconnectedPlayerId, reason) -> {
            System.out.println("Player " + disconnectedPlayerId + " timed out");
            gameState.deathClient.sendDeathToEveryOne(disconnectedPlayerId);
        });

        //TODO: move
        Function<Object[], Coin> createCoin = (o) -> new Coin((int) o[1], (int) o[2], gameState.coinClient);
        Function<Object[], Chest> createChest = (o) -> new Chest((int) o[1], (int) o[2], gameState.chestClient);
        Function<Object[], Traphole> createTraphole = (o) -> new Traphole((int) o[1], (int) o[2], gameState);

        gameState.coinClient = new CoinClient(connectionManager.getCoinSpace(), gameState, createCoin);
        gameState.chestClient = new ChestClient(connectionManager.getChestSpace(), gameState, createChest);
        gameState.trapholeClient = new StaticEntityClient<>(connectionManager.getTrapholeSpace(), gameState, createTraphole);
        gameState.deathClient = new DeathClient(gameState, onGameEnd);
        gameState.enemyClient = new EnemyClient(gameState);

        gameState.coinClient.listenForChanges();
        gameState.chestClient.listenForChanges();
        gameState.trapholeClient.listenForChanges();
        gameState.enemyClient.listenForChanges();

        if (connectionManager.isHost()) {
            StaticEntityPlacer placer = new StaticEntityPlacer(gameState);
            List<Chest> placedChests = placer.placeChests(INTITIAL_AMOUNT_OF_CHESTS);
            List<Coin> placedCoins = placer.placeCoins(INTITIAL_AMOUNT_OF_COINS);
            List<Traphole> placedTrapholes = placer.placeTrapholes(INTITIAL_AMOUNT_OF_TRAPHOLES);

            Servers servers = new Servers();

            servers.enemyServer = new EnemyServer(gameState);
            servers.coinServer = new CoinServer(gameState, connectionManager.getCoinSpace(), createCoin);
            servers.chestServer = new ChestServer(gameState, connectionManager.getChestSpace(), createChest, servers);
            servers.trapholeServer = new StaticEntityServer<>(gameState, connectionManager.getTrapholeSpace(), createTraphole);

            servers.coinServer.listenForCoinRequests();
            servers.chestServer.listenForChestRequests();
            servers.enemyServer.createEnemies();

            gameState.gameThreads.startHandledThread("Place coins, chest and traps",() -> {
                servers.coinServer.add(placedCoins);
                servers.chestServer.add(placedChests);
                servers.trapholeServer.add(placedTrapholes);
            });
        }
        ///

        new GameController(scene, connectionManager, gameState);
        Group mazeView = new Group();

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
        drawerMap.put(Chest.class, new ChestDrawer(graphics, mazeView));
        drawerMap.put(Traphole.class, new TrapholeDrawer(graphics));
        drawerMap.put(Player.class, new MoveableEntityDrawer(gameState, graphics, mazeView));
        drawerMap.put(Enemy.class, new MoveableEntityDrawer(gameState, graphics, mazeView));

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
