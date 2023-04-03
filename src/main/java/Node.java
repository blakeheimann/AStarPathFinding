public class Node {
    private int x;
    private int y;
    private Node parent;
    private double gCost;
    private double hCost;
    private double fCost;
    private boolean isObstacle;

    public Node(int x, int y) {
        this.x = x;
        this.y = y;
        this.isObstacle = false;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Node getParent() {
        return parent;
    }

    public void setParent(Node parent) {
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
        return isObstacle;
    }

    public void setObstacle(boolean isObstacle) {
        this.isObstacle = isObstacle;
    }
}
