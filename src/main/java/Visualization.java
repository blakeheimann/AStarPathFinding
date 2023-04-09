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

/**
 * Visualization is a JavaFX application that visualizes the A* pathfinding algorithm.
 * The application displays a grid where users can set the start and end nodes, add or remove
 * obstacles, and watch the algorithm find the shortest path between the start and end nodes.
 * The visualization updates in real-time to show the progress of the algorithm.
 * <p>
 * The class provides an interactive interface with buttons to start the algorithm, clear the grid,
 * and add random obstacles. Users can also click and drag nodes on the grid to change their
 * positions or status.
 * <p>
 * Dependencies: JavaFX, Grid, Node, and AStarPathFinder classes.
 * <p>
 * Usage:
 * - Compile the code.
 * - Run the Visualization class.
 * - Interact with the application through the GUI.
 */
public class Visualization extends Application {
    public static final int CELL_SIZE = 30;
    private static final int GRID_WIDTH = 50;
    private static final int GRID_HEIGHT = 25;
    private static final int THREAD_SLEEP_MILLIS = 10;
    private static final double OBSTACLES_SLIDER_MIN = 5;
    private static final double OBSTACLES_SLIDER_MAX = GRID_WIDTH * GRID_HEIGHT * 0.05;
    private static final double OBSTACLES_SLIDER_INIT = GRID_WIDTH * GRID_HEIGHT * 0.005;

    private Grid grid;
    private Pane root;
    private Node startNode;
    private Node endNode;
    private PriorityQueue<Node> openSet = new PriorityQueue<>(Comparator.comparingDouble(Node::getFCost));
    private Set<Node> closedSet = new HashSet<>();
    private List<Node> path = new ArrayList<>();
    private InteractionMode currentInteractionMode = InteractionMode.PLACE_START;

    private enum InteractionMode {
        PLACE_START, PLACE_END, REMOVE_OBSTACLE, SET_OBSTACLE, RUNNING_ALGORITHM
    }

    /**
     * The main method that launches the JavaFX application.
     *
     * @param args Command-line arguments (not used).
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Initializes the grid, scene, event listeners, and UI buttons.
     * Called when the JavaFX application is started.
     *
     * @param primaryStage The primary stage for this application, onto which the application scene can be set.
     */
    @Override
    public void start(Stage primaryStage) {
        grid = new Grid(GRID_WIDTH, GRID_HEIGHT, CELL_SIZE);
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

    // Draw the grid by adding all nodes to the root Pane
    private void drawGrid() {
        for (Node[] nodesArray : grid.getNodes()) {
            for (Node node : nodesArray) {
                root.getChildren().add(node.getRect());
            }
        }
    }

    /**
     * Clears the path, open set, and closed set from the grid.
     */
    private void clearPath() {
        for (Node[] nodesArray : grid.getNodes()) {
            for (Node node : nodesArray) {
                if (node.isPath() || node.isClosedSet() || node.isOpenSet()) {
                    node.setBlank();
                }
            }
        }
    }

    /**
     * Clears the entire grid, resetting the open set, closed set, and path.
     */
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

    /**
     * Finds and draws the shortest path between the start and end nodes using the A* algorithm.
     */
    private void findAndDrawPath() {
        clearPath();
        openSet = new PriorityQueue<>(Comparator.comparingDouble(Node::getFCost));
        closedSet = new HashSet<>();
        path = new ArrayList<>();
        openSet.add(startNode);
        if (startNode != null && endNode != null) {
            currentInteractionMode = InteractionMode.RUNNING_ALGORITHM;
            Task<Void> pathfindingTask = createPathfindingTask();
            new Thread(pathfindingTask).start();
        }
    }

    /**
     * Creates and returns a new Task for running the A* algorithm in the background.
     *
     * @return A Task<Void> that represents the A* pathfinding algorithm running in the background.
     */
    private Task<Void> createPathfindingTask() {
        return new Task<Void>() {
            @Override
            protected Void call() {
                while (currentInteractionMode == InteractionMode.RUNNING_ALGORITHM) {
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

    /**
     * Performs one step of the A* algorithm and updates the grid accordingly.
     */
    private void findAndDrawPathStep() {
        if (startNode == null || endNode == null || startNode.isObstacle() || endNode.isObstacle()) {
            currentInteractionMode = InteractionMode.PLACE_START;
            return;
        }

        if (openSet.isEmpty()) {
            currentInteractionMode = InteractionMode.PLACE_START;
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
            currentInteractionMode = InteractionMode.PLACE_START;
            return;
        }
        closedSet.add(currentNode);
        List<Node> neighbors = grid.getNeighbors(currentNode);
        for (Node neighbor : neighbors) {
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

    /**
     * Adds the specified number of random obstacles to the grid.
     *
     * @param numObstacles The number of obstacles to add to the grid.
     */
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

    /**
     * Sets the given node as an obstacle if it's not a start or end node.
     *
     * @param node The node to set as an obstacle.
     */
    private void setObstacle(Node node) {
        if (node == startNode || node == endNode) return;

        if (!node.isObstacle()) {
            node.setObstacle();
        }
    }

    /**
     * Removes the obstacle from the given node if it's not a start or end node.
     *
     * @param node The node to remove the obstacle from.
     */
    private void removeObstacle(Node node) {
        if (node == startNode || node == endNode) return;

        if (node.isObstacle()) {
            node.setBlank();
        }
    }

    /**
     * Sets the given node as the start node and updates the grid accordingly.
     *
     * @param node The node to set as the start node.
     */
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

    /**
     * Sets the given node as the end node and updates the grid accordingly.
     *
     * @param node The node to set as the end node.
     */
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

    /**
     * Updates the open set and closed set on the grid.
     *
     * @param openSet The PriorityQueue of nodes in the open set.
     * @param closedSet The Set of nodes in the closed set.
     */
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

    /**
     * Handles mouse click events for selecting nodes or obstacles.
     *
     * @param event The MouseEvent that represents the mouse click event.
     */
    private void handleMouseClick(MouseEvent event) {
        int x = (int) (event.getX() / CELL_SIZE);
        int y = (int) (event.getY() / CELL_SIZE);

        // Ensure the click is within the grid bounds
        if (x >= 0 && x < GRID_WIDTH && y >= 0 && y < GRID_HEIGHT) {
            Node clickedNode = grid.getNode(x, y);
            if (clickedNode == startNode) {
                currentInteractionMode = InteractionMode.PLACE_START;
            } else if (clickedNode == endNode) {
                currentInteractionMode = InteractionMode.PLACE_END;
            } else if (clickedNode.isObstacle()) {
                currentInteractionMode = InteractionMode.REMOVE_OBSTACLE;
                removeObstacle(clickedNode);
            } else {
                currentInteractionMode = InteractionMode.SET_OBSTACLE;
                setObstacle(clickedNode);
            }
        }
    }

    /**
     * Handles mouse drag events for moving start and end nodes, or setting/removing obstacles.
     *
     * @param event The MouseEvent that represents the mouse drag event.
     */
    private void handleMouseDrag(MouseEvent event) {
        int x = (int) (event.getX() / CELL_SIZE);
        int y = (int) (event.getY() / CELL_SIZE);

        if (x >= 0 && x < GRID_WIDTH && y >= 0 && y < GRID_HEIGHT) {
            Node draggedNode = grid.getNode(x, y);
            switch (currentInteractionMode) {
                case PLACE_START:
                    setStartNode(draggedNode);
                    break;
                case PLACE_END:
                    setEndNode(draggedNode);
                    break;
                case REMOVE_OBSTACLE:
                    removeObstacle(draggedNode);
                    break;
                default: // SET_OBSTACLE
                    setObstacle(draggedNode);
                    break;
            }
        }
    }


    /**
     * Creates and returns a VBox containing the UI buttons for the application.
     *
     * @return A VBox containing the UI buttons.
     */
    private VBox getButtons() {
        Button startAlgorithmButton = new Button("Start Algorithm");
        startAlgorithmButton.setLayoutX((double) (GRID_WIDTH * CELL_SIZE) / 2 + 20);
        startAlgorithmButton.setLayoutY(GRID_HEIGHT * CELL_SIZE + 10);
        startAlgorithmButton.setOnAction(e -> {
            findAndDrawPath();
        });

        Label sliderLabel = new Label("Number of obstacles:");
        Slider obstaclesSlider = new Slider(OBSTACLES_SLIDER_MIN, OBSTACLES_SLIDER_MAX, OBSTACLES_SLIDER_INIT);
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