import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class Visualization extends Application {
    private static final int CELL_SIZE = 20;
    private static final int GRID_WIDTH = 20;
    private static final int GRID_HEIGHT = 20;

    private Grid grid;
    private AStarPathFinder pathFinder;
    private Pane root;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        grid = new Grid(GRID_WIDTH, GRID_HEIGHT);
        pathFinder = new AStarPathFinder(grid);

        root = new Pane();
        Scene scene = new Scene(root, GRID_WIDTH * CELL_SIZE, GRID_HEIGHT * CELL_SIZE);

        drawGrid();

        // Set start and end nodes, and any obstacles
        grid.setObstacle(4, 4, true);
        grid.setObstacle(4, 5, true);
        grid.setObstacle(4, 6, true);

        // Find the path
        java.util.List<Node> path = pathFinder.findPath(0, 0, GRID_WIDTH - 1, GRID_HEIGHT - 1);

        // Draw the path
        if (path != null) {
            for (Node node : path) {
                drawCell(node.getX(), node.getY(), Color.BLUE);
            }
        }

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
}
