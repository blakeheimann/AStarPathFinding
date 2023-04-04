import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.util.List;
import java.util.Random;

public class Visualization extends Application {
    private static final int CELL_SIZE = 20;
    private static final int GRID_WIDTH = 60;
    private static final int GRID_HEIGHT = 60;
    private static final int BUTTON_AREA_HEIGHT = 40;

    private Grid grid;
    private AStarPathFinder pathFinder;
    private Pane root;
    private Node startNode;
    private Node endNode;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        grid = new Grid(GRID_WIDTH, GRID_HEIGHT);
        pathFinder = new AStarPathFinder(grid);

        root = new Pane();
        Scene scene = new Scene(root, GRID_WIDTH * CELL_SIZE, GRID_HEIGHT * CELL_SIZE + BUTTON_AREA_HEIGHT);

        // Initialize the grid
        drawGrid();

        // Add mouse event listeners
        scene.setOnMousePressed(event -> {
            int x = (int) (event.getX() / CELL_SIZE);
            int y = (int) (event.getY() / CELL_SIZE);

            if (event.getButton() == MouseButton.PRIMARY) {
                setStartOrEndNode(x, y);
            } else if (event.getButton() == MouseButton.SECONDARY) {
                toggleObstacle(x, y);
            }
        });

        scene.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.MIDDLE) {
                findAndDrawPath();
            }
        });

        // Add a clear button
        Button clearButton = new Button("Clear");
        clearButton.setLayoutX(GRID_WIDTH * CELL_SIZE - 50);
        clearButton.setLayoutY(GRID_HEIGHT * CELL_SIZE);
        clearButton.setOnAction(e -> {
            grid.clear();
            startNode = null;
            endNode = null;
            drawGrid();
        });

        Button randomObstaclesButton = new Button("Add Random Obstacles");
        randomObstaclesButton.setLayoutX(GRID_WIDTH * CELL_SIZE / 2 - 100);
        randomObstaclesButton.setLayoutY(GRID_HEIGHT * CELL_SIZE + 10);
        randomObstaclesButton.setOnAction(event -> addRandomObstacles(50)); // Change the number of obstacles to your preference

        root.getChildren().add(clearButton);
        root.getChildren().add(randomObstaclesButton);


        primaryStage.setTitle("A* Pathfinding Visualization");
        primaryStage.setScene(scene);
        primaryStage.show();
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

    private void setStartOrEndNode(int x, int y) {
        if (startNode == null) {
            startNode = grid.getNode(x, y);
            drawCell(x, y, Color.GREEN);
        } else if (endNode == null) {
            endNode = grid.getNode(x, y);
            drawCell(x, y, Color.RED);
        }
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

}
