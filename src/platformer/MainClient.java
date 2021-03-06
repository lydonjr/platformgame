package platformer;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import platformer.connection.NetworkClient;
import platformer.connection.packets.PlayerConnectPacket;
import platformer.connection.packets.PlayerDisconnectPacket;
import platformer.world.Location;
import platformer.world.World;
import platformer.world.entity.PlayerEntity;

import java.io.IOException;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

public class MainClient extends Application {
    public static World WORLD;
    public static PlayerEntity PLAYER;
    public static int PLAYER_ID;
    public static double screenWidth, screenHeight;
    private static NetworkClient client;
    private static Timer timer;
    private static double furthestDistance;
    private static Text scoreText;
    public static Image HOUSE_IMAGE;
    public static Image HOSTILE_ENTITY_IMAGE;

    //Window for application
    public static Pane root;

    //First Scene
    public static Scene scene;

    public static Stage stage;

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter IP: ");
        client = new NetworkClient(scanner.nextLine());

        System.out.println("Enter username: ");
        client.sendPacket(client.getSocket(), new PlayerConnectPacket(scanner.nextLine()));
//        client.sendPacket(client.getSocket(), new PlayerConnectPacket("Test"));
        // should receive confirmation

        launch(args);
    }

    public static void setFurthestDistance(double current) {
        current = Math.abs(current);
        if (current > furthestDistance) {
            furthestDistance = current;
        }
    }

    public static void reconnect() throws IOException {
        client = new NetworkClient(client.getSocket().getLocalAddress());
        client.sendPacket(client.getSocket(), new PlayerConnectPacket("Test"));
    }

    public static void initScene() {
//        VBox vBox = new VBox();
//        root = vBox;
        root = new Pane();
        scene = new Scene(root);
        root.setBackground(new Background(new BackgroundFill(Color.LIGHTBLUE, CornerRadii.EMPTY, Insets.EMPTY)));
        scoreText.setScaleX(2);
        scoreText.setScaleY(2);
        scoreText.setFill(Color.RED);
        scoreText.setX(60);
        scoreText.setY(20);
        root.getChildren().add(scoreText);
        stage.setScene(scene);
        PlayerEntity.setKeyListener(scene);
    }

    @Override
    public void start(Stage primaryStage) {
        HOUSE_IMAGE = new Image("house.png");
       // HOSTILE_ENTITY_IMAGE = new Image("warrior.png");
        stage = primaryStage;
        scoreText = new Text("Furthest distance: 0");
        initScene();

        primaryStage.widthProperty().addListener((observable, oldValue, newValue) -> screenWidth = newValue.doubleValue());
        primaryStage.heightProperty().addListener((observable, oldValue, newValue) -> screenHeight = newValue.doubleValue());

        primaryStage.setHeight(480);
        primaryStage.setWidth(720);

        primaryStage.show();


        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (client.isClosed())
                    Platform.exit();
                else {
                    client.update();
                    scoreText.setText("Furthest distance: " + furthestDistance);
                }
            }
        }, 0, 16L); // every 16 ms is ~60 fps

        primaryStage.setOnCloseRequest(event -> Platform.exit());
    }

    @Override
    public void stop() throws IOException {
        if (!client.isClosed()) {
            client.sendPacket(client.getSocket(), new PlayerDisconnectPacket());
            client.close();
        }
        timer.cancel();
    }

    public static NetworkClient getClient() {
        return client;
    }

    public static Location getScreenLocation() {
        return new Location(PLAYER.getLocation().getX() + screenWidth / 2, PLAYER.getLocation().getY() + 3 * screenHeight / 5);
    }
}
