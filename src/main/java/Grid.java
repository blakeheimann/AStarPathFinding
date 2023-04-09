import java.util.ArrayList;
import java.util.List;

/**
 * Grid represents a two-dimensional grid of nodes used in pathfinding.
 */
public class Grid {
    private final int width;
    private final int height;
    private final Node[][] nodes;

    /**
     * Constructs a new Grid with the specified width and height.
     *
     * @param width The width of the grid.
     * @param height The height of the grid.
     */
    public Grid(int width, int height, int cellSize) {
        this.width = width;
        this.height = height;
        this.nodes = new Node[width][height];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                nodes[x][y] = new Node(x, y, Node.State.BLANK, cellSize);
            }
        }
    }

    /**
     * Retrieves the node at the specified coordinates.
     *
     * @param x The x coordinate.
     * @param y The y coordinate.
     * @return The node at the specified coordinates, or null if the coordinates are out of bounds.
     */
    public Node getNode(int x, int y) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            return nodes[x][y];
        }
        return null;
    }

    /**
     * Retrieves the neighbors of the given node.
     *
     * @param node The node for which to find neighbors.
     * @return A list of neighboring nodes.
     */
    public List<Node> getNeighbors(Node node) {
        List<Node> neighbors = new ArrayList<>();

        int[][] directions = {
                {-1, 0}, {1, 0}, {0, -1}, {0, 1}
        };

        for (int[] direction : directions) {
            int newX = node.getX() + direction[0];
            int newY = node.getY() + direction[1];
            Node neighbor = getNode(newX, newY);

            if (neighbor != null && !neighbor.isObstacle()) {
                neighbors.add(neighbor);
            }
        }

        return neighbors;
    }

    /**
     * Resets the state of all nodes in the grid to blank.
     */
    public void clear() {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                nodes[x][y].setBlank();
            }
        }
    }

    /**
     * Retrieves the Node[][] array representing the grid.
     *
     * @return The Node[][] array.
     */
    public Node[][] getNodes() {
        return nodes;
    }
}

