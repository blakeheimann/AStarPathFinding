import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.util.*;

public class Visualization extends Application {
    private static final int CELL_SIZE = 20;
    private static final int GRID_WIDTH = 30;
    private static final int GRID_HEIGHT = 30;
    private Grid grid;
    private Pane root;
    private Node startNode;
    private Node endNode;
    private Task<Void> pathfindingTask;
    private PriorityQueue<Node> openSet = new PriorityQueue<>(Comparator.comparingDouble(Node::getFCost));
    private Set<Node> closedSet = new HashSet<>();
    private Rectangle[][] cellRectangles = new Rectangle[GRID_WIDTH][GRID_HEIGHT];

    private enum Mode {
        PLACE_START, PLACE_END, REMOVE_OBSTACLE, SET_OBSTACLE, RUNNING_ALGORITHM
    }

    private Mode currentMode = Mode.PLACE_START;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        grid = new Grid(GRID_WIDTH, GRID_HEIGHT);
        root = new Pane();
        Scene scene = new Scene(root, GRID_WIDTH * CELL_SIZE, GRID_HEIGHT * CELL_SIZE + 120);

        // Initialize the grid
        drawGrid();

        // Add mouse event listeners
        scene.setOnMousePressed(event -> handleMouseClick(event));
        scene.setOnMouseDragged(event -> handleMouseDrag(event));

        // Add UI buttons
        root.getChildren().add(getButtons());

        // Set the start and end nodes based on the given conditions
        int startY = GRID_HEIGHT / 2;
        int startX = GRID_WIDTH / 3;
        int endX = (2 * GRID_WIDTH) / 3;
        setStartNode(startX, startY);
        setEndNode(endX, startY);

        primaryStage.setTitle("A* Pathfinding Visualization");
        primaryStage.setScene(scene);
        primaryStage.show();
    }



    private void drawGrid() {
        for (int x = 0; x < GRID_WIDTH; x++) {
            for (int y = 0; y < GRID_HEIGHT; y++) {
                Node node = grid.getNode(x, y);
                cellRectangles[x][y] = createCell(x, y, node.isObstacle() ? Color.BLACK : Color.WHITE);
            }
        }
    }

    private Rectangle createCell(int x, int y, Color color) {
        Rectangle rect = new Rectangle(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
        rect.setFill(color);
        rect.setStroke(Color.GRAY);
        root.getChildren().add(rect);
        return rect;
    }

    private void drawCell(int x, int y, Color color) {
        Rectangle rect = cellRectangles[x][y];
        if (rect != null) {
            rect.setFill(color);
        }
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

    private void clearGrid() {
        grid.clear();
        drawGrid();
        if (startNode != null) {
            setStartNode(startNode.getX(), startNode.getY());
        }
        if (endNode != null) {
            setEndNode(endNode.getX(), endNode.getY());
        }
    }

    private void findAndDrawPath() {
        clearPath();
        openSet = new PriorityQueue<>(Comparator.comparingDouble(Node::getFCost));
        closedSet = new HashSet<>();
        openSet.add(startNode);
        if (startNode != null && endNode != null) {
            currentMode = Mode.RUNNING_ALGORITHM;
            pathfindingTask = createPathfindingTask();
            new Thread(pathfindingTask).start();
        }
    }

    private Task<Void> createPathfindingTask() {
        return new Task<Void>() {
            @Override
            protected Void call() {
                while (currentMode == Mode.RUNNING_ALGORITHM) {
                    findAndDrawPathStep();
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
                return null;
            }
        };
    }

    private void findAndDrawPathStep() {
        if (startNode == null || endNode == null || startNode.isObstacle() || endNode.isObstacle()) {
            currentMode = Mode.PLACE_START;
            return;
        }

        if (openSet.isEmpty()) {
            currentMode = Mode.PLACE_START;
            return;
        }

        Node currentNode = openSet.poll();
        if (currentNode.equals(endNode)) {
            List<Node> path = AStarPathFinder.reconstructPath(endNode);
            if (path != null) {
                for (Node node : path) {
                    if (node != startNode && node != endNode) {
                        drawCell(node.getX(), node.getY(), Color.BLUE);
                    }
                }
            }
            currentMode = Mode.PLACE_START;
            return;
        }
        updateSets(openSet, closedSet);
        closedSet.add(currentNode);
        for (Node neighbor : grid.getNeighbors(currentNode)) {
            if (closedSet.contains(neighbor)) {
                continue;
            }

            double tentativeGCost = currentNode.getGCost() + AStarPathFinder.distance(currentNode, neighbor);
            if (tentativeGCost < neighbor.getGCost() || !openSet.contains(neighbor)) {
                neighbor.setParent(currentNode);
                neighbor.setGCost(tentativeGCost);
                neighbor.setHCost(AStarPathFinder.distance(neighbor, endNode));

                if (!openSet.contains(neighbor)) {
                    openSet.add(neighbor);
                }
            }
        }
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

    private void removeObstacle(int x, int y) {
        Node node = grid.getNode(x, y);
        if (node == startNode || node == endNode) return;

        if (node.isObstacle()) {
            grid.setObstacle(x, y, false);
            drawCell(x, y, Color.WHITE);
        }
    }

    private void setStartNode(int x, int y) {
        if(grid.getNode(x,y).isObstacle()){
            return;
        }
        if (startNode != null) {
            // Clear the previous start node
            drawCell(startNode.getX(), startNode.getY(), Color.WHITE);
        }
        startNode = grid.getNode(x, y);
        drawCell(x, y, Color.GREEN);
    }

    private void setEndNode(int x, int y) {
        if(grid.getNode(x,y).isObstacle()){
            return;
        }
        if (endNode != null) {
            // Clear the previous end node
            drawCell(endNode.getX(), endNode.getY(), Color.WHITE);
        }
        endNode = grid.getNode(x, y);
        drawCell(x, y, Color.RED);
    }

    public void updateSets(PriorityQueue<Node> openSet, Set<Node> closedSet) {
        if (openSet != null) {
            for (Node node : openSet) {
                if (node != startNode && node != endNode) {
                    drawCell(node.getX(), node.getY(), Color.LIGHTGREEN);
                }
            }
        }
        if (closedSet != null) {
            for (Node node : closedSet) {
                if (node != startNode && node != endNode) {
                    drawCell(node.getX(), node.getY(), Color.LIGHTBLUE);
                }
            }
        }
    }






    ////////////////////


    private void handleMouseClick(MouseEvent event){
        int x = (int) (event.getX() / CELL_SIZE);
        int y = (int) (event.getY() / CELL_SIZE);

        // Ensure the click is within the grid bounds
        if (x >= 0 && x < GRID_WIDTH && y >= 0 && y < GRID_HEIGHT) {
            Node clickedNode = grid.getNode(x, y);
            if (clickedNode == startNode) {
                currentMode = Mode.PLACE_START;
            } else if (clickedNode == endNode) {
                currentMode = Mode.PLACE_END;
            } else if (clickedNode.isObstacle()) {
                currentMode = Mode.REMOVE_OBSTACLE;
                removeObstacle(x, y);
            } else {
                currentMode = Mode.SET_OBSTACLE;
                setObstacle(x, y);
            }
        }
    }


    private void handleMouseDrag(MouseEvent event){
        int x = (int) (event.getX() / CELL_SIZE);
        int y = (int) (event.getY() / CELL_SIZE);

        if (x >= 0 && x < GRID_WIDTH && y >= 0 && y < GRID_HEIGHT) {
            if (currentMode == Mode.PLACE_START) {
                setStartNode(x, y);
            } else if (currentMode == Mode.PLACE_END) {
                setEndNode(x, y);
            } else if (currentMode == Mode.REMOVE_OBSTACLE) {
                removeObstacle(x, y);
            } else {
                setObstacle(x, y);
            }
        }
    }

    private VBox getButtons(){
        Button startAlgorithmButton = new Button("Start Algorithm");
        startAlgorithmButton.setLayoutX(GRID_WIDTH * CELL_SIZE / 2 + 20);
        startAlgorithmButton.setLayoutY(GRID_HEIGHT * CELL_SIZE + 10);
        startAlgorithmButton.setOnAction(e -> {
            findAndDrawPath();
        });

        Label sliderLabel = new Label("Number of obstacles:");
        Slider obstaclesSlider = new Slider(5, GRID_WIDTH * GRID_HEIGHT * 0.05, GRID_WIDTH * GRID_HEIGHT * 0.005);
        obstaclesSlider.setShowTickLabels(true);
        obstaclesSlider.setShowTickMarks(true);
        obstaclesSlider.setMajorTickUnit(50);
        obstaclesSlider.setBlockIncrement(1);

        Button addRandomObstaclesButton = new Button("Add Random Obstacles");
        addRandomObstaclesButton.setOnAction(e -> addRandomObstacles((int) obstaclesSlider.getValue()));

        Button clearButton = new Button("Clear");
        clearButton.setOnAction(e -> clearGrid());

        HBox topButtonsContainer = new HBox(10);
        topButtonsContainer.setAlignment(Pos.CENTER);
        topButtonsContainer.getChildren().addAll(addRandomObstaclesButton);

        HBox bottomButtonsContainer = new HBox(10);
        bottomButtonsContainer.setAlignment(Pos.CENTER);
        bottomButtonsContainer.getChildren().addAll(clearButton, startAlgorithmButton);

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

        return buttonsContainer;
    }
}