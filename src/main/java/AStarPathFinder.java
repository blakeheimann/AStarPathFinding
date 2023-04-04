import java.util.*;

public class AStarPathFinder {
    private Grid grid;
    private PriorityQueue<Node> openSet;
    private Set<Node> closedSet;

    public AStarPathFinder(Grid grid) {
        this.grid = grid;
    }

    public List<Node> findPath(int startX, int startY, int endX, int endY) {
        Node startNode = grid.getNode(startX, startY);
        Node endNode = grid.getNode(endX, endY);

        if (startNode == null || endNode == null || startNode.isObstacle() || endNode.isObstacle()) {
            return null;
        }

        openSet = new PriorityQueue<>(Comparator.comparingDouble(Node::getFCost));
        closedSet = new HashSet<>();

        openSet.add(startNode);

        while (!openSet.isEmpty()) {
            Node currentNode = openSet.poll();

            if (currentNode.equals(endNode)) {
                return reconstructPath(endNode);
            }

            closedSet.add(currentNode);

            for (Node neighbor : grid.getNeighbors(currentNode)) {
                if (closedSet.contains(neighbor)) {
                    continue;
                }

                double tentativeGCost = currentNode.getGCost() + distance(currentNode, neighbor);

                if (tentativeGCost < neighbor.getGCost() || !openSet.contains(neighbor)) {
                    neighbor.setParent(currentNode);
                    neighbor.setGCost(tentativeGCost);
                    neighbor.setHCost(distance(neighbor, endNode));

                    if (!openSet.contains(neighbor)) {
                        openSet.add(neighbor);
                    }
                }
            }
        }

        return null;
    }

    public List<Node> getVisitedNodes() {
        List<Node> visitedNodes = new ArrayList<>(openSet);
        visitedNodes.addAll(closedSet);
        return visitedNodes;
    }

    private List<Node> reconstructPath(Node endNode) {
        List<Node> path = new ArrayList<>();
        Node currentNode = endNode;

        while (currentNode != null) {
            path.add(currentNode);
            currentNode = currentNode.getParent();
        }

        Collections.reverse(path);
        return path;
    }

    private double distance(Node a, Node b) {
        return Math.sqrt(Math.pow(a.getX() - b.getX(), 2) + Math.pow(a.getY() - b.getY(), 2));
    }
}
