package fr.xamez.maze;

public class Main {

    /**
     * We must use as main class, the one that will be run at application startup,
     * a class that does not extend Application, otherwise, because JavaFx is a Gradle plugin,
     * an error occurs, "JavaFX runtime components are missing, and are required to run this application"
     */
    public static void main(String[] args) {
        MazeGame.main(args);
    }

}
