package fr.xamez.maze;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.Stack;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Generate and solve a maze
 * @author Xamez
 */
public class MazeGame extends Application {

    private static MazeGame instance;

    /**
     * The width in pixel of the window, by default the value is <b>{@value WIDTH}</b>
     */
    private final int WIDTH = 900;

    /**
     * The height in pixel of the window, by default the value is <b>{@value HEIGHT}</b>
     */
    private final int HEIGHT = 900;

    /**
     * The size in pixel of a each cell, by default the value is <b>{@value CELL_SIZE}</b>
     */
    private final int CELL_SIZE = WIDTH/100;

    /**
     * The width of a line, by default the value is <b>{@value LINE_WIDTH}</b>
     */
    private final int LINE_WIDTH = CELL_SIZE/10;

    /**
     * The number of cells per row, by default the value is <b>{@value W_CELL_NUMBER}</b>
     */
    private final int W_CELL_NUMBER = WIDTH / CELL_SIZE;

    /**
     * The number of cells per column, by default the value is <b>{@value H_CELL_NUMBER}</b>
     */
    private final int H_CELL_NUMBER = HEIGHT / CELL_SIZE;

    /**
     * The {@link GraphicsContext} of the {@link Canvas}
     */
    private GraphicsContext ctx;

    /**
     * The delay in {@link java.util.concurrent.TimeUnit#MILLISECONDS milliseconds} between
     * each iterations to draw the solution of the maze
     */
    private final long DELAY = 10L;

    private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    /**
     * This variable is used to know if visualization of the maze is running or not
     */
    private boolean isStopped = true;

    /**
     * The actual {@link Maze} display on the windows
     */
    private Maze maze;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

        instance = this;

        final StackPane pane = new StackPane();
        final Canvas canvas = new Canvas(WIDTH, HEIGHT);
        pane.getChildren().add(canvas);

        Scene scene = new Scene(pane, WIDTH, HEIGHT);

        primaryStage.setScene(scene);
        primaryStage.setTitle("Maze generator");
        primaryStage.setResizable(false);
        primaryStage.show();
        primaryStage.centerOnScreen();

        primaryStage.setOnCloseRequest(event -> {
            try {
                System.exit(0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        this.ctx = canvas.getGraphicsContext2D();

        canvas.setFocusTraversable(true);
        canvas.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.SPACE) isStopped = !isStopped;
        });

        pane.setOnMouseClicked(e -> {
            if (this.scheduler.isShutdown() || isStopped) {
                this.scheduler.shutdown();
                this.scheduler = Executors.newSingleThreadScheduledExecutor();
                this.isStopped = true;
                proceed();
            }
        });

        proceed();
    }

    /**
     * This method does several things:
     * <ul>
     *     <li>It creates a {@link Maze}</li>
     *     <li>It call {@link Maze#visualizeSolution()} to visualize the solution</li>
     * </ul>
     */
    private void proceed(){
        this.maze = new Maze(W_CELL_NUMBER, H_CELL_NUMBER);
        //this.maze = new Maze(W_CELL_NUMBER, H_CELL_NUMBER, new Pair<>(0, 0), new Pair<>(W_CELL_NUMBER-1, H_CELL_NUMBER-1));
        this.maze.createMaze();
        this.maze.visualizeSolution();
    }

    /**
     * This method is call every {@value DELAY} millisecond to display the sortest path from the current {@link Maze}
     * @param stack The reversed {@link Maze#getSTACK() stack}
     *              She's reverse because we start drawing the solution from the beginning and not the end
     */
    public void drawSolution(Stack<Cell> stack){
        final Cell target = stack.pop();
        this.ctx.setStroke(Color.LIME);
        this.ctx.setLineWidth(LINE_WIDTH*3);
        this.ctx.strokeLine(this.maze.getCurrentCell().getX()*CELL_SIZE + CELL_SIZE/2d, this.maze.getCurrentCell().getY()*CELL_SIZE + CELL_SIZE/2d,
                target.getX()*CELL_SIZE + CELL_SIZE/2d, target.getY()*CELL_SIZE+ CELL_SIZE/2d);
        this.maze.setCurrentCell(target);
    }

    /**
     * This method draw the maze
     */
    public void draw(){
        this.ctx.setFill(Color.rgb(13, 13, 13));
        this.ctx.fillRect(0, 0, WIDTH, HEIGHT);
        this.ctx.setFill(Color.FUCHSIA);
        this.ctx.fillRect(this.maze.getStart().getX()*CELL_SIZE, this.maze.getStart().getY()*CELL_SIZE, CELL_SIZE, CELL_SIZE);
        this.ctx.setFill(Color.ORANGERED);
        this.ctx.fillRect(this.maze.getEnd().getX()*CELL_SIZE, this.maze.getEnd().getY()*CELL_SIZE, CELL_SIZE, CELL_SIZE);

        for (int x = 0; x < W_CELL_NUMBER; x++) {
            for (int y = 0; y < H_CELL_NUMBER; y++) {
                final Cell cell = this.maze.getCellAt(x, y);

                this.ctx.setLineWidth(LINE_WIDTH);
                this.ctx.setStroke(Color.WHITE);
                // top
                if (cell.getWalls()[0]) ctx.strokeLine(x*CELL_SIZE, y*CELL_SIZE, (x+1)*CELL_SIZE, y*CELL_SIZE);
                // bottom
                if (cell.getWalls()[1]) ctx.strokeLine(x*CELL_SIZE, (y+1)*CELL_SIZE, (x+1)*CELL_SIZE, (y+1)*CELL_SIZE);
                // right
                if (cell.getWalls()[2]) ctx.strokeLine((x+1)*CELL_SIZE, y*CELL_SIZE, (x+1)*CELL_SIZE, (y+1)*CELL_SIZE);
                // left
                if (cell.getWalls()[3]) ctx.strokeLine(x*CELL_SIZE, y*CELL_SIZE, x*CELL_SIZE, (y+1)*CELL_SIZE);
            }
        }
    }

    public static MazeGame getInstance() {
        return instance;
    }

    public ScheduledExecutorService getScheduler() {
        return scheduler;
    }

    public long getDELAY() {
        return DELAY;
    }

    public boolean isStopped() {
        return isStopped;
    }

}