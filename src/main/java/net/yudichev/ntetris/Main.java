package net.yudichev.ntetris;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import net.yudichev.ntetris.canvas.javafx.JavaFxGameCanvas;
import net.yudichev.ntetris.game.NTetris;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.concurrent.TimeUnit.NANOSECONDS;

public final class Main extends Application {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage primaryStage) {
        var settings = Settings.builder()
                .setPlayerZoneHeightInBlocks(10)
                .setPlayerZoneWidthInBlocks(30)
                .build();

        var root = new Pane();
        var theScene = new Scene(root);
        primaryStage.setScene(theScene);

        var canvasWidth = 20 * settings.playerZoneWidthInBlocks() * 2 + 1;
        var canvasHeight = 20 * settings.playerZoneHeightInBlocks();
        root.setPrefWidth(canvasWidth);
        root.setPrefHeight(canvasHeight);
        var canvas = new Canvas();
        canvas.widthProperty().bind(root.widthProperty());
        canvas.heightProperty().bind(root.heightProperty());
        root.getChildren().add(canvas);

        var gc = canvas.getGraphicsContext2D();

        var gameCanvas = new JavaFxGameCanvas(gc, settings);
        gameCanvas.setSize(canvasWidth, canvasHeight);
        ChangeListener<Number> sizeChangeListener = (observable, oldValue, newValue) -> gameCanvas.setSize(canvas.getWidth(), canvas.getHeight());
        primaryStage.widthProperty().addListener(sizeChangeListener);
        primaryStage.heightProperty().addListener(sizeChangeListener);
        var keyPressTracker = new KeyPressTracker(theScene);
        Game game = new NTetris(settings, gameCanvas, keyPressTracker);
        var animationTimer = new AnimationTimer() {
            long startNanoTime = Long.MIN_VALUE;

            @Override
            public void handle(@SuppressWarnings("ParameterNameDiffersFromOverriddenParameter") long currentNanoTime) {
                loop(currentNanoTime);
            }

            private void loop(long currentNanoTime) {
                logger.debug("animation timer");
                if (startNanoTime == Long.MIN_VALUE) {
                    startNanoTime = currentNanoTime;
                }
                var gameTimeMillis = NANOSECONDS.toMillis(currentNanoTime - startNanoTime);
                gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
                game.render(gameTimeMillis);
            }
        };
        animationTimer.start();

        primaryStage.show();
    }
}
