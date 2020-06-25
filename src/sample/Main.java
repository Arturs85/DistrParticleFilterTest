package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Main extends Application {
    static Canvas canvas = new Canvas(1500, 1500);
    Space2D board = new Space2D(500, 500);
    PublicPartOfAgent testAgent;
    Simulation simulation = new Simulation(board);

    @Override
    public void start(Stage primaryStage) throws Exception{
        ScrollPane scrollPane = new ScrollPane(canvas);
        BorderPane root = new BorderPane(scrollPane);
        board.draw(canvas);
        simulation.createAgent();
        simulation.createAgent();
        simulation.createAgent();

        testAgent = simulation.createAgent();

        testAgent.moveForwardBy(300);

        Button makeMoveButton = new Button("Move");
        Button stopMoveButton = new Button("Stop");

        makeMoveButton.setOnAction(event -> {
            simulation.isPaused =false;

        });

        makeMoveButton.setPrefWidth(130);


        Button turnRightButton = new Button(">>");
        Button turnLeftButton = new Button("<<");

        HBox robotControlPanel = new HBox(turnLeftButton, makeMoveButton,stopMoveButton, turnRightButton);

        VBox buttonBar = new VBox(20, robotControlPanel);
        buttonBar.setPadding(new Insets(5, 5, 5, 5));
        buttonBar.setAlignment(Pos.CENTER);

        root.setRight(buttonBar);


        primaryStage.setTitle("Triangulation agents");
        primaryStage.setScene(new Scene(root, 1000, 575));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
