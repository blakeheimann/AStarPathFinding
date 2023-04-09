import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * AStarPathFinder is a utility class that provides helper methods for the A* pathfinding algorithm.
 */
public class AStarPathFinder {

    // Private constructor to prevent instantiation of the utility class
    private AStarPathFinder() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Reconstructs the path from the start node to the end node using the parent pointers.
     *
     * @param endNode The end node of the path.
     * @return A list of nodes representing the path, excluding the start and end nodes.
     */
    public static List<Node> reconstructPath(Node endNode) {
        List<Node> path = new ArrayList<>();
        Node currentNode = endNode;

        while (currentNode != null) {
            path.add(currentNode);
            currentNode = currentNode.getParent();
        }

        Collections.reverse(path);
        path.remove(0);
        path.remove(path.size() - 1);
        return path;
    }

    /**
     * Calculates the Euclidean distance between two nodes.
     *
     * @param a The first node.
     * @param b The second node.
     * @return The Euclidean distance between the two nodes.
     */
    public static double distance(Node a, Node b) {
        return Math.sqrt(Math.pow(a.getX() - b.getX(), 2) + Math.pow(a.getY() - b.getY(), 2));
    }
}
