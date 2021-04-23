package fr.xamez.maze;

import javafx.util.Pair;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class Maze {

    /**
     * The instance of the {@link MazeGame} class
     */
    private final MazeGame mazeGame = MazeGame.getInstance();

    /**
     * The number of cells per row
     */
    private final int wCellNumber;

    /**
     * The number of cells per column
     */
    private final int hCellNumber;

    /**
     * The starting cell
     */
    private final Cell start;

    /**
     * The ending cell
     */
    private final Cell end;

    /**
     * A two-dimensional array containing the cells
     */
    private final Cell[][] CELLS;

    /**
     * Know if the maze has been solve
     */
    private boolean isSolve;

    /**
     * The current treated cell
     */
    private Cell currentCell;

    private final Stack<Cell> STACK = new Stack<>();
    private final Queue<Cell> QUEUE = new LinkedList<>();

    public Maze(int wCellNumber, int hCellNumber, Pair<Integer, Integer> start, Pair<Integer, Integer> end) {
        CELLS = new Cell[wCellNumber][hCellNumber];
        this.wCellNumber = wCellNumber;
        this.hCellNumber = hCellNumber;
        createGrid();
        this.start = CELLS[start.getKey()][start.getValue()];
        this.end = CELLS[end.getKey()][end.getValue()];
        this.isSolve = false;
    }

    /**
     * By default this constructor place the starting on the upper left cell and the ending point on the lower right cell
     * @param wCellNumber The number of cells per row
     * @param hCellNumber The number of cells per column
     */
    public Maze(int wCellNumber, int hCellNumber) {
        this(wCellNumber, hCellNumber, new Pair<>(0, 0), new Pair<>(wCellNumber-1, hCellNumber-1));
    }

    /**
     * This method create a grid of `wCellNumber` x `hCellNumber`
     */
    private void createGrid(){
        for (int x = 0; x < wCellNumber; x++) {
            for (int y = 0; y < hCellNumber; y++) {
                CELLS[x][y] = new Cell(x, y);
            }
        }

    }

    /**
     * The method is used to create the maze <br>
     * It essentially breaks walls
     */
    public void createMaze() {
        currentCell = this.start;
        STACK.push(currentCell);
        while (!STACK.isEmpty()) {
            currentCell.markAsVisited();
            final List<Cell> unVisitedNeighbours = new ArrayList<>();
            for (Cell cell : getNeighbours(currentCell)) {
                if (!cell.isVisited()) {
                    unVisitedNeighbours.add(cell);
                }
            }
            if (!unVisitedNeighbours.isEmpty()) {

                final Cell nextCell = unVisitedNeighbours.get(new Random().nextInt(unVisitedNeighbours.size()));
                removeWalls(nextCell);

                STACK.push(currentCell);
                currentCell = nextCell;
            } else {
                currentCell = STACK.pop();
            }
        }
        mazeGame.draw();
    }

    /**
     * This method solve the maze and put the solution into the `STACK` variable to retrieve later the solution
     */
    private void solve() {
        STACK.clear();
        QUEUE.add(this.start);
        while (!this.isSolve) {
            Cell current = QUEUE.poll();
            if (current == null) current = STACK.pop();
            current.markAsDiscovered();
            final List<Cell> neighbours = getNeighbours(current);
            for (int i = 0; i < neighbours.size(); i++) {
                final Cell neighbor = neighbours.get(i);
                if (canReach(current, neighbor) && !neighbor.isDiscovered()) {
                    QUEUE.add(neighbor);
                    STACK.push(current);
                    if (neighbor == this.end) this.isSolve = true;
                    break;
                }
            }
        }
    }

    /**
     * This method is used to visualize the solution and do severals things:
     * <ul>
     *     <li>This method call the {@link Maze#solve()} method</li>
     *     <li>It reverses the `STACK` to start drawing the solution from the beginning and not the end</li>
     *     <li>It calls every `DELAY` {@link java.util.concurrent.TimeUnit#MILLISECONDS milliseconds}
     *     the {@link MazeGame#drawSolution(Stack)} method</li>
     * </ul>
     *
     */
    public void visualizeSolution(){
        solve();

        final Stack<Cell> reversedStack = new Stack<>();
        reversedStack.push(this.end);
        while (!this.STACK.isEmpty()){
            reversedStack.push(this.STACK.pop());
        }

        mazeGame.getScheduler().scheduleAtFixedRate(() -> {
            if (!reversedStack.isEmpty() && !mazeGame.isStopped()) {
                mazeGame.drawSolution(reversedStack);
            } else if (reversedStack.isEmpty()){
                mazeGame.getScheduler().shutdown();
            }
        }, 0L, mazeGame.getDELAY(), TimeUnit.MILLISECONDS);
    }

    /**
     * It removes the wall between the `currentCell` and the `nextCell`
     * @param nextCell The next cell
     */
    private void removeWalls(Cell nextCell){
        if (this.currentCell.getX() - nextCell.getX() < 0) {
            this.currentCell.getWalls()[2] = false;
            nextCell.getWalls()[3] = false;
        } else if (this.currentCell.getX() - nextCell.getX() > 0){
            this.currentCell.getWalls()[3] = false;
            nextCell.getWalls()[2] = false;
        }
        if (this.currentCell.getY() - nextCell.getY() < 0) {
            this.currentCell.getWalls()[1] = false;
            nextCell.getWalls()[0] = false;
        } else if (this.currentCell.getY() - nextCell.getY() > 0){
            this.currentCell.getWalls()[0] = false;
            nextCell.getWalls()[1] = false;
        }
    }

    /**
     * This method return the neighbours of a given {@link Cell}
     * @param cell The cell that we want whose neighbors
     * @return A {@link List<Cell>} of {@link Cell}
     */
    private List<Cell> getNeighbours(Cell cell){

        final List<Cell> temp_neighbours = new ArrayList<>();
        final int x = cell.getX();
        final int y = cell.getY();

        temp_neighbours.add(getCellAt(x, y-1));
        temp_neighbours.add(getCellAt(x, y+1));
        temp_neighbours.add(getCellAt(x+1, y));
        temp_neighbours.add(getCellAt(x-1, y));

        final List<Cell> neighbours = new ArrayList<>();
        for (Cell newCell : temp_neighbours){
            if (newCell != null){
                neighbours.add(newCell);
            }
        }

        return neighbours;

    }

    /**
     * It returns the cell at specific coordinates
     * @param coordinates x and y coordinates of the cell the we want
     * @return The Cell at the given coordinates or null if coordinates are out of bounds
     */
    public Cell getCellAt(int... coordinates){
        if (coordinates.length != 2) return null;
        final int x = coordinates[0];
        final int y = coordinates[1];
        if (x >= 0 && x < this.wCellNumber && y >= 0 && y < this.hCellNumber){
            return this.CELLS[x][y];
        }
        return null;
    }

    /**
     * Used to know if the `currentCell` can reach or not the `targetCell`
     * The `currentCell` can reach the `targetCell` it mean the there is not wall
     * between both of them
     * @param currentCell The actual cell
     * @param targetCell The cell that we want to reach
     * @return A boolean if the `currentCell` can reach the `targetCell`
     */
    private boolean canReach(Cell currentCell, Cell targetCell){
        if (currentCell.getX() - targetCell.getX() < 0) return !currentCell.getWalls()[2];
        if (currentCell.getX() - targetCell.getX() > 0) return !currentCell.getWalls()[3];
        if (currentCell.getY() - targetCell.getY() > 0) return !currentCell.getWalls()[0];
        if (currentCell.getY() - targetCell.getY() < 0) return !currentCell.getWalls()[1];
        return false;
    }

    /**
     * <b>DO NOT USE</b>
     * This method is used to break random walls to make the maze more difficult to solve
     * Unfortunately it doesn't work, i will try to solve this problem in the next version
     */
    @Deprecated
    private void randomize(){
        final Random r = new Random();
        for (int x = 1; x < this.wCellNumber-1; x++) {
            for (int y = 1; y < this.hCellNumber-1; y++) {
                if (r.nextInt(100) >= 80) {
                    final boolean[] walls = getCellAt(x, y).getWalls();
                    final int randomWall = r.nextInt(4); // there is 4 walls

                    if (walls[randomWall]) {
                        switch (randomWall){
                            case 0 -> {
                                final Cell cell = getCellAt(x, y+1);
                                if (cell != null) {
                                    System.out.println("called 1");
                                    cell.getWalls()[1] = false;
                                }
                            }
                            case 1 -> {
                                final Cell cell = getCellAt(x, y-1);
                                if (cell != null) {
                                    System.out.println("called 2");
                                    cell.getWalls()[0] = false;
                                }
                            }
                            case 2 -> {
                                final Cell cell = getCellAt(x+1, y);
                                if (cell != null) {
                                    System.out.println("called 3");
                                    cell.getWalls()[3] = false;
                                }
                            }
                            case 3 -> {
                                final Cell cell = getCellAt(x-1, y);
                                if (cell != null) {
                                    System.out.println("called 4");
                                    cell.getWalls()[2] = false;
                                }
                            }
                        }
                        walls[randomWall] = false;
                    }
                }
            }
        }
        mazeGame.draw();
    }

    public Cell getStart() {
        return start;
    }

    public Cell getEnd() {
        return end;
    }

    public Cell getCurrentCell() {
        return currentCell;
    }

    public void setCurrentCell(Cell currentCell) {
        this.currentCell = currentCell;
    }

    public Stack<Cell> getSTACK() {
        return STACK;
    }

}
