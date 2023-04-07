import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class Node {
    private int x;
    private int y;
    private Node parent;
    private double gCost;
    private double hCost;
    private double fCost;
    private Rectangle rect;
    public enum State {
        BLANK, OBSTACLE, START, END, CLOSED_SET, OPEN_SET, PATH
    }

    private State state;
    private static final int CELL_SIZE = Visualization.CELL_SIZE;

    public Node(int x, int y, State state) {
        this.x = x;
        this.y = y;
        this.state = state;
        rect = new Rectangle(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
        rect.setStroke(Color.GRAY);
        updateRect();
    }

    private void updateRect() {
        Color color;
        switch (this.state) {
            case START:
                color = Color.GREEN;
                break;
            case END:
                color = Color.RED;
                break;
            case CLOSED_SET:
                color = Color.LIGHTGREEN;
                break;
            case OPEN_SET:
                color = Color.LIGHTBLUE;
                break;
            case PATH:
                color = Color.BLUE;
                break;
            case OBSTACLE:
                color = Color.BLACK;
                break;
            default:
                color = Color.WHITE;
        }
        rect.setFill(color);
    }

    public int getX() {
        return x;
    }

    public Rectangle getRect(){
        return rect;
    }

    public int getY() {
        return y;
    }

    public Node getParent() {
        return parent;
    }

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


    public double getGCost() {
        return gCost;
    }

    public void setGCost(double gCost) {
        this.gCost = gCost;
        updateFCost();
    }

    public double getHCost() {
        return hCost;
    }

    public void setHCost(double hCost) {
        this.hCost = hCost;
        updateFCost();
    }

    public double getFCost() {
        return fCost;
    }

    private void updateFCost() {
        this.fCost = this.gCost + this.hCost;
    }

    public boolean isObstacle() {
        return this.state.equals(State.OBSTACLE);
    }

    public void setObstacle() {
        this.state = State.OBSTACLE;
        updateRect();
    }
    public boolean isBlank(){
        return this.state.equals(State.BLANK);
    }
    public void setBlank(){
        this.state = State.BLANK;
        this.parent = null;
        updateRect();
    }

    public void setStart(){
        this.state = State.START;
        updateRect();
    }
    public void setEnd(){
        this.state = State.END;
        updateRect();
    }

    public boolean isClosedSet(){
        return this.state.equals(State.CLOSED_SET);
    }
    public void setClosedSet(){
        this.state = State.CLOSED_SET;
        updateRect();
    }
    public boolean isOpenSet(){
        return this.state.equals(State.OPEN_SET);
    }
    public void setOpenSet(){
        this.state = State.OPEN_SET;
        updateRect();
    }

    public boolean isPath(){
        return this.state.equals(State.PATH);
    }
    public void setPath(){
        this.state = State.PATH;
        updateRect();
    }

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
