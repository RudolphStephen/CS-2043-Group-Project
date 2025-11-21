import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.scene.control.ListView;
import java.io.File;

public class Ui
{
    private Coordinator coordinator;
    private Stage primaryStage;

    // Constructor receives the JavaFX Stage from Main
    public Ui(Stage stage)
    {
        this.primaryStage = stage;
        this.coordinator = new Coordinator();
    }

    // ------------------ UI SCREENS ------------------

    public void showWelcomeScreen()
    {
        Label welcomeLabel = new Label("Welcome to Group 3's Submission 3");
        Button startButton = new Button("Start");

        VBox layout = new VBox(20, welcomeLabel, startButton);
        layout.setStyle("-fx-padding: 50; -fx-alignment: center;");

        Scene scene = new Scene(layout, 500, 300);
        startButton.setOnAction(e -> showFolderSelectionScreen());

        primaryStage.setScene(scene);
    }

    public void showFolderSelectionScreen()
    {
        Label instructionLabel = new Label("Select or enter root folder for student submissions:");
        TextField pathField = new TextField();
        pathField.setPromptText("Enter folder path here...");

        Button browseButton = new Button("Browse");
        Button proceedButton = new Button("Proceed");

        Label errorLabel = new Label("Please select or enter a valid folder.");
        errorLabel.setStyle("-fx-text-fill: red;");
        errorLabel.setVisible(false);

        VBox layout = new VBox(15, instructionLabel, pathField, browseButton, proceedButton, errorLabel);
        layout.setStyle("-fx-padding: 50; -fx-alignment: center;");

        browseButton.setOnAction(e -> {
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle("Select Root Folder");
            File selected = chooser.showDialog(primaryStage);
            if (selected != null)
            {
                pathField.setText(selected.getAbsolutePath());
                errorLabel.setVisible(false);
            }
        });

        proceedButton.setOnAction(e -> {
            String path = pathField.getText();
            if (path != null && !path.isEmpty())
            {
                File folder = new File(path);
                if (folder.exists() && folder.isDirectory())
                {
                    coordinator.setRootFolder(path);
                    showTestSuiteManagementScreen();
                }
                else
                {
                    errorLabel.setText("Folder does not exist. Please select a valid folder.");
                    errorLabel.setVisible(true);
                }
            }
            else
            {
                errorLabel.setText("Please select or enter a valid folder.");
                errorLabel.setVisible(true);
            }
        });

        primaryStage.setScene(new Scene(layout, 600, 400));
    }

    public void showTestSuiteManagementScreen()
    {
        Coordinator coordinator = this.coordinator;

        Button createSuiteButton = new Button("Create New Suite");
        Button selectSuiteButton = new Button("Select Existing Suite");

        ListView<String> testCaseList = new ListView<>();
        Button addCaseButton = new Button("Add Test Case");
        Button editCaseButton = new Button("Edit Selected Case");
        Button removeCaseButton = new Button("Remove Selected Case");
        Button doneButton = new Button("Done");

        VBox layout = new VBox(10,
                createSuiteButton,
                selectSuiteButton,
                new Separator(),
                new Label("Test Cases in Suite:"),
                testCaseList,
                new Separator(),
                addCaseButton,
                editCaseButton,
                removeCaseButton,
                doneButton
        );
        layout.setStyle("-fx-padding: 20; -fx-alignment: center;");

        Scene scene = new Scene(layout, 600, 600);

        createSuiteButton.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Create Test Suite");
            dialog.setHeaderText(null);
            dialog.setContentText("Enter Test Suite Title:");
            dialog.showAndWait().ifPresent(title -> {
                if (!title.isEmpty())
                {
                    coordinator.createTestSuite(title);
                    testCaseList.getItems().clear();
                }
            });
        });

        selectSuiteButton.setOnAction(e -> {
            coordinator.loadTestSuite();
            testCaseList.getItems().clear();

            if (coordinator.getCurrentTestSuite() != null)
            {
                for (TestCase tc : coordinator.getCurrentTestSuite().getTestCases())
                {
                    testCaseList.getItems().add(tc.getTitle());
                }
            }
        });

        addCaseButton.setOnAction(e -> {
            TestCase tc = promptTestCase(null);
            if (tc != null)
            {
                coordinator.getCurrentTestSuite().addTestCase(tc);
                testCaseList.getItems().add(tc.getTitle());
            }
        });

        editCaseButton.setOnAction(e -> {
            String selected = testCaseList.getSelectionModel().getSelectedItem();
            if (selected != null)
            {
                TestCase tc = coordinator.getCurrentTestSuite().getTestCases().stream()
                        .filter(t -> t.getTitle().equals(selected))
                        .findFirst()
                        .orElse(null);

                if (tc != null)
                {
                    TestCase edited = promptTestCase(tc);
                    if (edited != null)
                    {
                        tc.setTitle(edited.getTitle());
                        tc.setInputData(edited.getInputData());
                        tc.setExpectedOutput(edited.getExpectedOutput());

                        testCaseList.getItems().set(
                                testCaseList.getSelectionModel().getSelectedIndex(),
                                edited.getTitle()
                        );
                    }
                }
            }
        });

        removeCaseButton.setOnAction(e -> {
            String selected = testCaseList.getSelectionModel().getSelectedItem();
            if (selected != null)
            {
                TestCase tc = coordinator.getCurrentTestSuite().getTestCases().stream()
                        .filter(t -> t.getTitle().equals(selected))
                        .findFirst()
                        .orElse(null);

                if (tc != null)
                {
                    coordinator.getCurrentTestSuite().removeTestCase(tc);
                    testCaseList.getItems().remove(selected);
                }
            }
        });

        doneButton.setOnAction(e -> {
            System.out.println("Test Suite finalized: " +
                    coordinator.getCurrentTestSuite().getTitle());
        });

        primaryStage.setScene(scene);
    }

    private TestCase promptTestCase(TestCase existing)
    {
        Dialog<TestCase> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Add Test Case" : "Edit Test Case");

        ButtonType okButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

        TextField titleField = new TextField();
        TextField inputField = new TextField();
        TextField expectedField = new TextField();

        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll("Boolean", "Int", "Double", "String");

        if (existing != null)
        {
            titleField.setText(existing.getTitle());
            inputField.setText(existing.getInputData());
            expectedField.setText(existing.getExpectedOutput());
            typeCombo.setValue(existing.getType());
        }

        VBox content = new VBox(10,
                new Label("Title:"), titleField,
                new Label("Input:"), inputField,
                new Label("Expected Output:"), expectedField,
                new Label("Type:"), typeCombo
        );
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(button -> {
            if (button == okButtonType)
            {
                return new TestCase(
                        titleField.getText(),
                        inputField.getText(),
                        expectedField.getText(),
                        typeCombo.getValue()
                );
            }
            return null;
        });

        return dialog.showAndWait().orElse(null);
    }
}
