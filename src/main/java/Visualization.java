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
import javafx.stage.Stage;

import java.util.*;

public class Visualization extends Application {
    public static final int CELL_SIZE = 20;
    private static final int GRID_WIDTH = 30;
    private static final int GRID_HEIGHT = 30;
    private static final int THREAD_SLEEP_MILLIS = 10;
    private Grid grid;
    private Pane root;
    private Node startNode;
    private Node endNode;
    private PriorityQueue<Node> openSet = new PriorityQueue<>(Comparator.comparingDouble(Node::getFCost));
    private Set<Node> closedSet = new HashSet<>();
    private List<Node> path = new ArrayList<>();
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
        scene.setOnMousePressed(this::handleMouseClick);
        scene.setOnMouseDragged(this::handleMouseDrag);

        // Add UI buttons
        root.getChildren().add(getButtons());

        // Set the start and end nodes based on the given conditions
        int startY = GRID_HEIGHT / 2;
        int startX = GRID_WIDTH / 3;
        int endX = (2 * GRID_WIDTH) / 3;
        setStartNode(grid.getNode(startX, startY));
        setEndNode(grid.getNode(endX, startY));

        primaryStage.setTitle("A* Pathfinding Visualization");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void drawGrid() {
        for (Node[] nodesArray : grid.nodes) {
            for (Node node : nodesArray) {
                root.getChildren().add(node.getRect());
            }
        }
    }

    // sets all nodes in the sets to blank
    private void clearPath() {
        for (Node[] nodesArray : grid.nodes) {
            for (Node node : nodesArray) {
                if (node.isPath() || node.isClosedSet() || node.isOpenSet()) {
                    node.setBlank();
                }
            }
        }
    }

    // sets all nodes to blank except for start and end node
    private void clearGrid() {
        openSet = new PriorityQueue<>(Comparator.comparingDouble(Node::getFCost));
        closedSet = new HashSet<>();
        path = new ArrayList<>();
        grid.clear();
        if (startNode != null) {
            setStartNode(startNode);
        }
        if (endNode != null) {
            setEndNode(endNode);
        }
    }

    private void findAndDrawPath() {
        clearPath();
        openSet = new PriorityQueue<>(Comparator.comparingDouble(Node::getFCost));
        closedSet = new HashSet<>();
        path = new ArrayList<>();
        openSet.add(startNode);
        if (startNode != null && endNode != null) {
            currentMode = Mode.RUNNING_ALGORITHM;
            Task<Void> pathfindingTask = createPathfindingTask();
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
                        Thread.sleep(THREAD_SLEEP_MILLIS);
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
            path = AStarPathFinder.reconstructPath(endNode);
            for (Node node : path) {
                if (node != startNode && node != endNode) {
                    node.setPath();
                }
            }
            currentMode = Mode.PLACE_START;
            return;
        }
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
        updateSets(openSet, closedSet);
    }

    //adds random obstacles
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

            node.setObstacle();
        }
    }

    private void setObstacle(Node node) {
        if (node == startNode || node == endNode) return;

        if (!node.isObstacle()) {
            node.setObstacle();
        }
    }

    private void removeObstacle(Node node) {
        if (node == startNode || node == endNode) return;

        if (node.isObstacle()) {
            node.setBlank();
        }
    }

    private void setStartNode(Node node) {
        if (node.isObstacle() || node.isEnd()) {
            return;
        }
        if (startNode != null) {
            // Clear the previous start node
            if (path.contains(startNode)) {
                startNode.setPath();
            } else if (openSet.contains(startNode)) {
                startNode.setOpenSet();
            } else if (closedSet.contains(startNode)) {
                startNode.setClosedSet();
            } else {
                startNode.setBlank();
            }
        }
        startNode = node;
        node.setStart();
    }

    private void setEndNode(Node node) {
        if (node.isObstacle() || node.isStart()) {
            return;
        }
        if (endNode != null) {
            // Clear the previous end node
            if (path.contains(endNode)) {
                endNode.setPath();
            } else if (openSet.contains(endNode)) {
                endNode.setOpenSet();
            } else if (closedSet.contains(endNode)) {
                endNode.setClosedSet();
            } else {
                endNode.setBlank();
            }
        }
        endNode = node;
        node.setEnd();
    }

    public void updateSets(PriorityQueue<Node> openSet, Set<Node> closedSet) {
        if (openSet != null) {
            for (Node node : openSet) {
                if (node != startNode && node != endNode) {
                    node.setOpenSet();
                }
            }
        }
        if (closedSet != null) {
            for (Node node : closedSet) {
                if (node != startNode && node != endNode) {
                    node.setClosedSet();
                }
            }
        }
    }

    private void handleMouseClick(MouseEvent event) {
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
                removeObstacle(clickedNode);
            } else {
                currentMode = Mode.SET_OBSTACLE;
                setObstacle(clickedNode);
            }
        }
    }

    ////////////////////

    private void handleMouseDrag(MouseEvent event) {
        int x = (int) (event.getX() / CELL_SIZE);
        int y = (int) (event.getY() / CELL_SIZE);

        if (x >= 0 && x < GRID_WIDTH && y >= 0 && y < GRID_HEIGHT) {
            Node draggedNode = grid.getNode(x, y);
            if (currentMode == Mode.PLACE_START) {
                setStartNode(draggedNode);
            } else if (currentMode == Mode.PLACE_END) {
                setEndNode(draggedNode);
            } else if (currentMode == Mode.REMOVE_OBSTACLE) {
                removeObstacle(draggedNode);
            } else {
                setObstacle(draggedNode);
            }
        }
    }

    private VBox getButtons() {
        Button startAlgorithmButton = new Button("Start Algorithm");
        startAlgorithmButton.setLayoutX((double) (GRID_WIDTH * CELL_SIZE) / 2 + 20);
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

    private enum Mode {
        PLACE_START, PLACE_END, REMOVE_OBSTACLE, SET_OBSTACLE, RUNNING_ALGORITHM
    }
}