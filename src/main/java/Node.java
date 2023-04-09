import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 * Represents a node in the grid for A* pathfinding.
 */
public class Node {
    private final int x;
    private final int y;
    private final Rectangle rect;
    private final int cellSize;
    private Node parent;
    private double gCost;
    private double hCost;
    private double fCost;
    private State state;

    public enum State {
        BLANK, OBSTACLE, START, END, CLOSED_SET, OPEN_SET, PATH
    }

    /**
     * Constructs a Node object with the specified coordinates, state, and cell size.
     *
     * @param x        The x coordinate of the node.
     * @param y        The y coordinate of the node.
     * @param state    The initial state of the node.
     * @param cellSize The size of the cell for visualization purposes.
     */
    public Node(int x, int y, State state, int cellSize) {
        this.x = x;
        this.y = y;
        this.state = state;
        this.cellSize = cellSize;
        rect = new Rectangle(x * cellSize, y * cellSize, cellSize, cellSize);
        rect.setStroke(Color.GRAY);
        updateRect();
    }

    /**
     * Updates the color of the rectangle based on the node's state.
     */
    private void updateRect() {
        Color color;
        switch (this.state) {
            case START:
                color = Color.LIMEGREEN;
                break;
            case END:
                color = Color.RED;
                break;
            case CLOSED_SET:
                color = Color.LIGHTBLUE;
                break;
            case OPEN_SET:
                color = Color.LIGHTGREEN;
                break;
            case PATH:
                color = Color.GOLD;
                break;
            case OBSTACLE:
                color = Color.BLACK;
                break;
            default:
                color = Color.WHITE;
        }
        rect.setFill(color);
    }

    /**
     * Returns the Rectangle object representing the node's visual representation.
     *
     * @return The Rectangle object representing the node's visual representation.
     */
    public Rectangle getRect() {
        return rect;
    }

    /**
     * Returns the x coordinate of the node.
     *
     * @return The x coordinate of the node.
     */
    public int getX() {
        return x;
    }

    /**
     * Returns the y coordinate of the node.
     *
     * @return The y coordinate of the node.
     */
    public int getY() {
        return y;
    }

    /**
     * Returns the parent node of the current node.
     *
     * @return The parent node of the current node.
     */
    public Node getParent() {
        return parent;
    }

    /**
     * Sets the parent node of the current node, ensuring that the parent node is not the current node or one of its ancestors.
     *
     * @param parent The new parent node.
     * @throws IllegalArgumentException If the specified parent node is the current node or one of its ancestors.
     */
    public void setParent(Node parent) {
        Node currentNode = parent;
        while (currentNode != null) {
            if (currentNode.equals(this)) {
                throw new IllegalArgumentException("Cannot set parent to self or an ancestor");
            }
            currentNode = currentNode.getParent();
        }
        this.parent = parent;
    }

    /**
     * Sets the h-cost (estimated movement cost to the end node) of the current node and updates the f-cost.
     *
     * @param hCost The new h-cost.
     */
    public void setHCost(double hCost) {
        this.hCost = hCost;
        updateFCost();
    }

    /**
     * Returns the g-cost (movement cost from the start node) of the current node.
     *
     * @return The g-cost of the current node.
     */
    public double getGCost() {
        return gCost;
    }

    /**
     * Sets the g-cost (movement cost from the start node) of the current node and updates the f-cost.
     *
     * @param gCost The new g-cost.
     */
    public void setGCost(double gCost) {
        this.gCost = gCost;
        updateFCost();
    }

    /**
     * Returns the f-cost (sum of g-cost and h-cost) of the current node.
     *
     * @return The f-cost of the current node.
     */
    public double getFCost() {
        return fCost;
    }

    /**
     * Updates the f-cost (sum of g-cost and h-cost) of the current node.
     */
    private void updateFCost() {
        this.fCost = this.gCost + this.hCost;
    }

    /**
     * Sets the current node's state to BLANK, clears the parent, and updates its visual representation.
     */
    public void setBlank() {
        this.state = State.BLANK;
        this.parent = null;
        updateRect();
    }

    /**
     * Checks if the current node is an obstacle.
     *
     * @return True if the current node is an obstacle, otherwise false.
     */
    public boolean isObstacle() {
        return this.state.equals(State.OBSTACLE);
    }

    /**
     * Sets the current node's state to OBSTACLE and updates its visual representation.
     */
    public void setObstacle() {
        this.state = State.OBSTACLE;
        updateRect();
    }

    /**
     * Checks if the current node is the start node.
     *
     * @return True if the current node is the start node, otherwise false.
     */
    public boolean isStart() {
        return this.state.equals(State.START);
    }

    /**
     * Sets the current node's state to START, clears the parent, and updates its visual representation.
     */
    public void setStart() {
        this.state = State.START;
        this.parent = null;
        updateRect();
    }

    /**
     * Checks if the current node is the end node.
     *
     * @return True if the current node is the end node, otherwise false.
     */
    public boolean isEnd() {
        return this.state.equals(State.END);
    }

    /**
     * Sets the current node's state to END, clears the parent, and updates its visual representation.
     */
    public void setEnd() {
        this.state = State.END;
        this.parent = null;
        updateRect();
    }

    /**
     * Checks if the current node is in the closed set.
     *
     * @return True if the current node is in the closed set, otherwise false.
     */
    public boolean isClosedSet() {
        return this.state.equals(State.CLOSED_SET);
    }

    /**
     * Sets the current node's state to CLOSED_SET and updates its visual representation.
     */
    public void setClosedSet() {
        this.state = State.CLOSED_SET;
        updateRect();
    }

    /**
     * Checks if the current node is in the open set.
     *
     * @return True if the current node is in the open set, otherwise false.
     */
    public boolean isOpenSet() {
        return this.state.equals(State.OPEN_SET);
    }

    /**
     * Sets the current node's state to OPEN_SET and updates its visual representation.
     */
    public void setOpenSet() {
        this.state = State.OPEN_SET;
        updateRect();
    }

    /**
     * Checks if the current node is part of the path.
     *
     * @return True if the current node is part of the path, otherwise false.
     */
    public boolean isPath() {
        return this.state.equals(State.PATH);
    }

    /**
     * Sets the current node's state to PATH and updates its visual representation.
     */
    public void setPath() {
        this.state = State.PATH;
        updateRect();
    }

    /**
     * Determines if the current node is equal to the specified object.
     * Two nodes are considered equal if they have the same x and y coordinates.
     *
     * @param obj The object to compare with the current node.
     * @return True if the specified object is equal to the current node, otherwise false.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Node node = (Node) obj;
        return x == node.x && y == node.y;
    }

}
