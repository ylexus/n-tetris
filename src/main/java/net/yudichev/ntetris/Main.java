package net.yudichev.ntetris;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import net.yudichev.ntetris.canvas.javafx.JavaFxGameCanvas;
import net.yudichev.ntetris.game.NTetris;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.unmodifiableList;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

public final class Main extends Application {
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(Main.class);
    private AnimationTimer animationTimer;

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage primaryStage) {
        var settings = Settings.builder()
                .setPlayerZoneHeightInBlocks(20)
                .setPlayerZoneWidthInBlocks(10)
                .build();

        Pane root = new Pane();
        Scene theScene = new Scene(root);
        primaryStage.setScene(theScene);

        List<KeyEvent> keyEventQueue = new ArrayList<>();
        List<KeyEvent> keyEventQueueUnmodifiable = unmodifiableList(keyEventQueue);
        theScene.setOnKeyPressed(keyEventQueue::add);

        var canvasWidth = 20 * settings.playerZoneWidthInBlocks() * 2 + 1;
        var canvasHeight = 20 * settings.playerZoneHeightInBlocks();
        root.setPrefWidth(canvasWidth);
        root.setPrefHeight(canvasHeight);
        Canvas canvas = new Canvas();
        canvas.widthProperty().bind(root.widthProperty());
        canvas.heightProperty().bind(root.heightProperty());
        root.getChildren().add(canvas);

        GraphicsContext gc = canvas.getGraphicsContext2D();

        JavaFxGameCanvas gameCanvas = new JavaFxGameCanvas(gc, settings);
        gameCanvas.setSize(canvasWidth, canvasHeight);
        ChangeListener<Number> sizeChangeListener = (observable, oldValue, newValue) -> gameCanvas.setSize(canvas.getWidth(), canvas.getHeight());
        primaryStage.widthProperty().addListener(sizeChangeListener);
        primaryStage.heightProperty().addListener(sizeChangeListener);
        Game game = new NTetris(settings);
        animationTimer = new AnimationTimer() {
            long startNanoTime = Long.MIN_VALUE;

            public void handle(long currentNanoTime) {
                loop(currentNanoTime);
            }

            private void loop(long currentNanoTime) {
                logger.debug("animation timer");
                if (startNanoTime == Long.MIN_VALUE) {
                    startNanoTime = currentNanoTime;
                }
                long gameTimeMillis = NANOSECONDS.toMillis(currentNanoTime - startNanoTime);
                gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
                game.render(gameTimeMillis, gameCanvas, keyEventQueueUnmodifiable);
                keyEventQueue.clear();
            }
        };
        animationTimer.start();

        primaryStage.show();
    }
}
