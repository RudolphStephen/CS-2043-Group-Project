import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    private Ui ui;  // your UI logic class

    public static void main(String[] args) {
        launch(args); // always correct here
    }

    @Override
    public void start(Stage stage) {
        ui = new Ui(stage);  // give the UI class the primary stage
        ui.showWelcomeScreen();

        stage.setTitle("Submission 3 Tool");
        stage.show();
    }
}
