import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Random;

public class Visualization extends Application {
    private static final int CELL_SIZE = 20;
    private static final int GRID_WIDTH = 30;
    private static final int GRID_HEIGHT = 30;
    private static final int BUTTON_AREA_HEIGHT = 40;

    private Grid grid;
    private AStarPathFinder pathFinder;
    private Pane root;
    private Node startNode;
    private Node endNode;

    private enum Mode {
        PLACE_START, PLACE_END, PLACE_OBSTACLE, DRAW_OBSTACLES
    }


    private Mode currentMode = Mode.PLACE_START;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        grid = new Grid(GRID_WIDTH, GRID_HEIGHT);
        pathFinder = new AStarPathFinder(grid);

        root = new Pane();
        Scene scene = new Scene(root, GRID_WIDTH * CELL_SIZE, GRID_HEIGHT * CELL_SIZE + 120);

        // Initialize the grid
        drawGrid();

        // Add mouse event listeners
        scene.setOnMousePressed(event -> {
            int x = (int) (event.getX() / CELL_SIZE);
            int y = (int) (event.getY() / CELL_SIZE);

            // Ensure the click is within the grid bounds
            if (x >= 0 && x < GRID_WIDTH && y >= 0 && y < GRID_HEIGHT) {
                if (event.getButton() == MouseButton.PRIMARY) {
                    handlePrimaryClick(x, y);
                }
            }
        });

        scene.setOnMouseDragged(event -> {
            if (currentMode == Mode.DRAW_OBSTACLES && event.getButton() == MouseButton.PRIMARY) {
                int x = (int) (event.getX() / CELL_SIZE);
                int y = (int) (event.getY() / CELL_SIZE);

                if (x >= 0 && x < GRID_WIDTH && y >= 0 && y < GRID_HEIGHT) {
                    setObstacle(x, y);
                }
            }
        });


        // Add UI buttons
        Button placeStartButton = new Button("Place Start");
        placeStartButton.setLayoutX(GRID_WIDTH * CELL_SIZE / 2 - 260);
        placeStartButton.setLayoutY(GRID_HEIGHT * CELL_SIZE + 10);
        placeStartButton.setOnAction(e -> currentMode = Mode.PLACE_START);

        Button placeEndButton = new Button("Place End");
        placeEndButton.setLayoutX(GRID_WIDTH * CELL_SIZE / 2 - 175);
        placeEndButton.setLayoutY(GRID_HEIGHT * CELL_SIZE + 10);
        placeEndButton.setOnAction(e -> currentMode = Mode.PLACE_END);

        Button placeObstacleButton = new Button("Place Obstacle");
        placeObstacleButton.setLayoutX(GRID_WIDTH * CELL_SIZE / 2 - 80);
        placeObstacleButton.setLayoutY(GRID_HEIGHT * CELL_SIZE + 10);
        placeObstacleButton.setOnAction(e -> currentMode = Mode.PLACE_OBSTACLE);

        Button startAlgorithmButton = new Button("Start Algorithm");
        startAlgorithmButton.setLayoutX(GRID_WIDTH * CELL_SIZE / 2 + 20);
        startAlgorithmButton.setLayoutY(GRID_HEIGHT * CELL_SIZE + 10);
        startAlgorithmButton.setOnAction(e -> findAndDrawPath());

        Label sliderLabel = new Label("Number of obstacles:");
        Slider obstaclesSlider = new Slider(5, GRID_WIDTH * GRID_HEIGHT * 0.05, GRID_WIDTH * GRID_HEIGHT * 0.005);
        obstaclesSlider.setShowTickLabels(true);
        obstaclesSlider.setShowTickMarks(true);
        obstaclesSlider.setMajorTickUnit(50);
        obstaclesSlider.setBlockIncrement(1);

        Button addRandomObstaclesButton = new Button("Add Random Obstacles");
        addRandomObstaclesButton.setOnAction(e -> addRandomObstacles((int) obstaclesSlider.getValue()));

        Button clearButton = new Button("Clear");
        clearButton.setOnAction(e -> {
            grid.clear();
            startNode = null;
            endNode = null;
            drawGrid();
        });

        Button drawObstaclesButton = new Button("Draw Obstacles");
        drawObstaclesButton.setOnAction(e -> currentMode = Mode.DRAW_OBSTACLES);

        HBox topButtonsContainer = new HBox(10);
        topButtonsContainer.setAlignment(Pos.CENTER);
        topButtonsContainer.getChildren().addAll(placeStartButton, placeEndButton, placeObstacleButton, addRandomObstaclesButton);

        HBox bottomButtonsContainer = new HBox(10);
        bottomButtonsContainer.setAlignment(Pos.CENTER);
        bottomButtonsContainer.getChildren().addAll(clearButton, startAlgorithmButton, drawObstaclesButton);

        VBox sliderContainer = new VBox(5);
        sliderContainer.setAlignment(Pos.CENTER);
        sliderContainer.getChildren().addAll(sliderLabel, obstaclesSlider);

        HBox bottomRowContainer = new HBox(10);
        bottomRowContainer.setAlignment(Pos.CENTER);
        bottomRowContainer.getChildren().addAll(bottomButtonsContainer, sliderContainer);

        VBox buttonsContainer = new VBox(10);
        buttonsContainer.setPadding(new Insets(10, 0, 0, 0));
        buttonsContainer.setAlignment(Pos.CENTER);
        buttonsContainer.setLayoutX(0);
        buttonsContainer.setLayoutY(GRID_HEIGHT * CELL_SIZE);
        buttonsContainer.setPrefWidth(GRID_WIDTH * CELL_SIZE);
        buttonsContainer.getChildren().addAll(topButtonsContainer, bottomRowContainer);

        root.getChildren().add(buttonsContainer);

//        root.getChildren().addAll(placeStartButton, placeEndButton, placeObstacleButton, startAlgorithmButton);

        primaryStage.setTitle("A* Pathfinding Visualization");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void handlePrimaryClick(int x, int y) {
        if (currentMode == Mode.PLACE_START) {
            setStartNode(x, y);
        } else if (currentMode == Mode.PLACE_END) {
            setEndNode(x, y);
        } else if (currentMode == Mode.PLACE_OBSTACLE) {
            toggleObstacle(x, y);
        }
    }

    private void setStartNode(int x, int y) {
        if (startNode != null) {
            // Clear the previous start node
            drawCell(startNode.getX(), startNode.getY(), Color.WHITE);
        }
        startNode = grid.getNode(x, y);
        drawCell(x, y, Color.GREEN);
    }

    private void setEndNode(int x, int y) {
        if (endNode != null) {
            // Clear the previous end node
            drawCell(endNode.getX(), endNode.getY(), Color.WHITE);
        }
        endNode = grid.getNode(x, y);
        drawCell(x, y, Color.RED);
    }

    private void drawGrid() {
        for (int x = 0; x < GRID_WIDTH; x++) {
            for (int y = 0; y < GRID_HEIGHT; y++) {
                Node node = grid.getNode(x, y);
                drawCell(x, y, node.isObstacle() ? Color.BLACK : Color.WHITE);
            }
        }
    }

    private void drawCell(int x, int y, Color color) {
        Rectangle rect = new Rectangle(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
        rect.setFill(color);
        rect.setStroke(Color.GRAY);
        root.getChildren().add(rect);
    }

    private void toggleObstacle(int x, int y) {
        Node node = grid.getNode(x, y);
        if (node == startNode || node == endNode) return;

        grid.setObstacle(x, y, !node.isObstacle());
        drawCell(x, y, node.isObstacle() ? Color.BLACK : Color.WHITE);
    }

    private void clearPath() {
        for (int x = 0; x < GRID_WIDTH; x++) {
            for (int y = 0; y < GRID_HEIGHT; y++) {
                Node node = grid.getNode(x, y);
                if (!node.isObstacle() && node != startNode && node != endNode) {
                    drawCell(x, y, Color.WHITE);
                }
            }
        }
    }

    private void findAndDrawPath() {
        clearPath();
        if (startNode != null && endNode != null) {
            java.util.List<Node> path = pathFinder.findPath(startNode.getX(), startNode.getY(), endNode.getX(), endNode.getY());
            colorVisitedNodes(pathFinder.getVisitedNodes());
            if (path != null) {
                for (Node node : path) {
                    if (node != startNode && node != endNode) {
                        drawCell(node.getX(), node.getY(), Color.BLUE);
                    }
                }
            }
        }
    }

    private void colorVisitedNodes(List<Node> visitedNodes) {
        if (visitedNodes.isEmpty()) {
            return;
        }

        double minFCost = Double.MAX_VALUE;
        double maxFCost = Double.MIN_VALUE;

        for (Node node : visitedNodes) {
            double fCost = node.getFCost();
            minFCost = Math.min(minFCost, fCost);
            maxFCost = Math.max(maxFCost, fCost);
        }

        for (Node node : visitedNodes) {
            if (node != startNode && node != endNode) {
                double normalizedFCost = (node.getFCost() - minFCost) / (maxFCost - minFCost);
                Color gradientColor = getColorFromGradient(normalizedFCost);
                drawCell(node.getX(), node.getY(), gradientColor);
            }
        }
    }

    private Color getColorFromGradient(double ratio) {
        Color startColor = Color.YELLOW;
        Color endColor = Color.PURPLE;

        double red = startColor.getRed() + (endColor.getRed() - startColor.getRed()) * ratio;
        double green = startColor.getGreen() + (endColor.getGreen() - startColor.getGreen()) * ratio;
        double blue = startColor.getBlue() + (endColor.getBlue() - startColor.getBlue()) * ratio;

        return new Color(red, green, blue, 1.0);
    }

    private void addRandomObstacles(int numObstacles) {
        Random random = new Random();

        for (int i = 0; i < numObstacles; i++) {
            int x, y;
            Node node;
            do {
                x = random.nextInt(GRID_WIDTH);
                y = random.nextInt(GRID_HEIGHT);
                node = grid.getNode(x, y);
            } while (node.isObstacle() || node == startNode || node == endNode);

            grid.setObstacle(x, y, true);
            drawCell(x, y, Color.BLACK);
        }
    }

    private void setObstacle(int x, int y) {
        Node node = grid.getNode(x, y);
        if (node == startNode || node == endNode) return;

        if (!node.isObstacle()) {
            grid.setObstacle(x, y, true);
            drawCell(x, y, Color.BLACK);
        }
    }

}
