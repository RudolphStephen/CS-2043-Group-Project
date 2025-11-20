import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.DirectoryChooser;
import java.io.File;

public class Ui extends Application
{
    private Coordinator coordinator; // Handles all logic

    @Override
    public void start(Stage primaryStage)
    {
        // Initialize coordinator
        coordinator = new Coordinator();

        // --- Welcome Screen ---
        Label welcomeLabel = new Label("Welcome to Group 3's Submission 3");
        Button startButton = new Button("Start");

        VBox welcomeLayout = new VBox(20, welcomeLabel, startButton);
        welcomeLayout.setStyle("-fx-padding: 50; -fx-alignment: center;");

        Scene welcomeScene = new Scene(welcomeLayout, 500, 300);

        // --- Input Screen (file destination) ---
        Label instructionLabel = new Label("Select or enter root folder for student submissions:");
        TextField pathField = new TextField();
        pathField.setPromptText("Enter folder path here...");

        Button browseButton = new Button("Browse");
        Button proceedButton = new Button("Proceed");

        VBox inputLayout = new VBox(15, instructionLabel, pathField, browseButton, proceedButton);
        inputLayout.setStyle("-fx-padding: 50; -fx-alignment: center;");

        Scene inputScene = new Scene(inputLayout, 600, 400);

        // --- Button Actions ---
        startButton.setOnAction(e -> primaryStage.setScene(inputScene));

        browseButton.setOnAction(e -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("Select Root Folder");
            File selectedDirectory = directoryChooser.showDialog(primaryStage);
            if (selectedDirectory != null)
            {
                pathField.setText(selectedDirectory.getAbsolutePath());
            }
        });

        proceedButton.setOnAction(e -> {
            String path = pathField.getText();
            if (path != null && !path.isEmpty())
            {
                coordinator.setRootFolder(path); // Send data to coordinator
                System.out.println("Root folder set to: " + path);
                // TODO: Transition to next screen (Test Suite selection/creation)
            }
            else
            {
                System.out.println("Please select or enter a valid folder.");
            }
        });

        primaryStage.setTitle("Submission 3 Tool");
        primaryStage.setScene(welcomeScene);
        primaryStage.show();
    }
}
