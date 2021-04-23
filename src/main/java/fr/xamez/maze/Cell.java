package fr.xamez.maze;

public class Cell {

    /**
     * The x coordinate of the {@link Cell}
     */
    private final int x;

    /**
     * The y coordinate of the {@link Cell}
     */
    private final int y;

    /**
     * Know if the cell has been visited or not <br>
     * This variable is used to generate the maze
     */
    private boolean visited;

    /**
     * An array of boolean representing the walls, construct like this: <br>
     * [top, bottom, right, left] <br>
     * By default, each wall is present
     */
    private final boolean[] walls;

    /**
     * Know if the cell has been discovered or not <br>
     * This variable is used to solve the maze
     */
    private boolean discovered;

    public Cell(int x, int y) {
        this.x = x;
        this.y = y;
        this.visited = false;
        this.walls = new boolean[] { true, true, true, true}; // top, bottom, right, left
        this.discovered = false;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public boolean isVisited() {
        return visited;
    }

    public void markAsVisited() {
        this.visited = true;
    }

    public boolean[] getWalls() {
        return walls;
    }

    public boolean isDiscovered() {
        return discovered;
    }

    public void markAsDiscovered() {
        this.discovered = true;
    }
}
