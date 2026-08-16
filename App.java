import javafx.application.Application;
import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;
import javafx.scene.paint.Color;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class App extends Application {
    record Cell(int x, int y) {
    }

    static List<Set<Cell>> life(Set<Cell> world, int generations) {
        List<Set<Cell>> worlds = new ArrayList<>();
        Set<Cell> currentWorld = new HashSet<>(world);
        worlds.add(currentWorld);
        for (int generation = 0; generation < generations; generation++) {
            currentWorld = nextGeneration(currentWorld);
            worlds.add(currentWorld);
        }
        return worlds;
    }

    static Set<Cell> nextGeneration(Set<Cell> world) {
        Set<Cell> nextWorld = new HashSet<>();
        for (Map.Entry<Cell, Integer> entry : neighborCounts(world).entrySet()) {
            int count = entry.getValue();
            if (count == 3 || (count == 2 && world.contains(entry.getKey()))) {
                nextWorld.add(entry.getKey());
            }
        }
        return nextWorld;
    }

    static Set<Cell> nextGeneration(Set<Cell> world, int columns, int rows) {
        Set<Cell> nextWorld = new HashSet<>();
        for (Map.Entry<Cell, Integer> entry : neighborCounts(world, columns, rows).entrySet()) {
            int count = entry.getValue();
            if (count == 3 || (count == 2 && world.contains(entry.getKey()))) {
                nextWorld.add(entry.getKey());
            }
        }
        return nextWorld;
    }

    static Map<Cell, Integer> neighborCounts(Set<Cell> world) {
        Map<Cell, Integer> counts = new HashMap<>();
        for (Cell cell : world) {
            for (Cell neighbor : neighbors(cell)) {
                counts.merge(neighbor, 1, Integer::sum);
            }
        }
        return counts;
    }

    static Map<Cell, Integer> neighborCounts(Set<Cell> world, int columns, int rows) {
        Map<Cell, Integer> counts = new HashMap<>();
        for (Cell cell : world) {
            for (Cell neighbor : neighbors(cell)) {
                Cell wrappedNeighbor = new Cell(
                        wrapCoordinate(neighbor.x(), columns),
                        wrapCoordinate(neighbor.y(), rows));
                counts.merge(wrappedNeighbor, 1, Integer::sum);
            }
        }
        return counts;
    }

    static int wrapCoordinate(int coordinate, int size) {
        return (coordinate % size + size) % size;
    }

    static List<Cell> neighbors(Cell cell) {
        int x = cell.x();
        int y = cell.y();
        return List.of(
                new Cell(x - 1, y - 1), new Cell(x, y - 1), new Cell(x + 1, y - 1),
                new Cell(x - 1, y),                         new Cell(x + 1, y),
                new Cell(x - 1, y + 1), new Cell(x, y + 1), new Cell(x + 1, y + 1));
    }

    static void drawWorld(GraphicsContext graphics, Set<Cell> world, int cellSize,
                          int columns, int rows, double width, double height) {
        graphics.setFill(Color.BLACK);
        graphics.fillRect(0, 0, width, height);
        graphics.setFill(Color.rgb(204, 85, 0));
        for (Cell cell : world) {
            if (cell.x() >= 0 && cell.x() < columns && cell.y() >= 0 && cell.y() < rows) {
                graphics.fillRect(cell.x() * cellSize, cell.y() * cellSize,
                        cellSize - 1, cellSize - 1);
            }
        }
    }

    public static void main(String[] args) {
        launch(App.class, args);
    }
    @Override
    public void start(Stage stage) {
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        double windowWidth = screenBounds.getWidth() / 2;
        double windowHeight = screenBounds.getHeight() / 2;
        int cellSize = 4;
        double statusBarHeight = 28;
        double boardHeight = windowHeight - statusBarHeight;
        int columns = (int) windowWidth / cellSize;
        int rows = (int) boardHeight / cellSize;
        int totalCells = columns * rows;
        int aliveCellCount = totalCells / 5;
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int[] cellPositions = new int[totalCells];
        Set<Cell> world = new HashSet<>();
        for (int i = 0; i < totalCells; i++) {
            cellPositions[i] = i;
        }

        Canvas canvas = new Canvas(windowWidth, boardHeight);
        GraphicsContext graphics = canvas.getGraphicsContext2D();
        for (int i = 0; i < aliveCellCount; i++) {
            int selectedIndex = random.nextInt(i, totalCells);
            int selectedPosition = cellPositions[selectedIndex];
            cellPositions[selectedIndex] = cellPositions[i];
            cellPositions[i] = selectedPosition;
            int x = selectedPosition % columns;
            int y = selectedPosition / columns;
            world.add(new Cell(x, y));
        }
        drawWorld(graphics, world, cellSize, columns, rows, windowWidth, boardHeight);

        Label generationLabel = new Label("Generation: 0");
        generationLabel.setMaxWidth(Double.MAX_VALUE);
        generationLabel.setPrefHeight(statusBarHeight);
        generationLabel.setAlignment(Pos.CENTER_LEFT);
        generationLabel.setStyle(
            "-fx-background-color: #1a1a1a; -fx-text-fill: #CC5500; -fx-padding: 0 10 0 10;");
        int[] generation = {0};

        AnimationTimer animation = new AnimationTimer() {
            private static final long FRAME_INTERVAL = 100_000_000L;
            private long lastUpdate;
            private Set<Cell> currentWorld = world;

            @Override
            public void handle(long now) {
                if (now - lastUpdate < FRAME_INTERVAL) {
                    return;
                }
                lastUpdate = now;
                currentWorld = nextGeneration(currentWorld, columns, rows);
                generation[0]++;
                generationLabel.setText("Generation: " + generation[0]);
                drawWorld(graphics, currentWorld, cellSize, columns, rows,
                    windowWidth, boardHeight);
            }
        };

            VBox root = new VBox(canvas, generationLabel);
            root.setAlignment(Pos.TOP_CENTER);
            root.setFocusTraversable(true);
            root.setStyle("-fx-background-color: black;");
        stage.setTitle("JavaFX Life");
        Scene scene = new Scene(root, windowWidth, windowHeight);
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                animation.stop();
                stage.close();
            }
        });
        stage.setScene(scene);
        stage.setX(screenBounds.getMinX() + (screenBounds.getWidth() - windowWidth) / 2);
        stage.setY(screenBounds.getMinY() + (screenBounds.getHeight() - windowHeight) / 2);
        stage.show();
        root.requestFocus();
        animation.start();
    }
}
