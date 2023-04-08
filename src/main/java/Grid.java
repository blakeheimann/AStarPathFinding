import java.util.ArrayList;
import java.util.List;

public class Grid {
    private final int width;
    private final int height;
    public Node[][] nodes;

    public Grid(int width, int height) {
        this.width = width;
        this.height = height;
        this.nodes = new Node[width][height];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                nodes[x][y] = new Node(x, y, Node.State.BLANK);
            }
        }
    }

    public Node getNode(int x, int y) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            return nodes[x][y];
        }
        return null;
    }

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

    public void clear() {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                nodes[x][y].setBlank();
            }
        }
    }
}
