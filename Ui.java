import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.scene.control.ListView;
import java.io.File;
import java.util.List;

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

        Label saveFolderLabel = new Label("Save Folder: Not set");
        Button browseSaveFolderButton = new Button("Browse Save Folder");
        Button createSuiteButton = new Button("Create New Suite");
        Button selectSuiteButton = new Button("Select Existing Suite");
        Button createCaseButton = new Button("Create New Test Case");
        Button manageCasesButton = new Button("Manage Test Cases");

        ListView<String> testCaseList = new ListView<>();
        Label suiteLabel = new Label("Selected Suite: None");
        Button addCaseButton = new Button("Add Test Case to Suite");
        Button removeCaseButton = new Button("Remove Test Case from Suite");
        Button saveSuiteButton = new Button("Save Suite");
        Button doneButton = new Button("Done");

        VBox layout = new VBox(10,
                saveFolderLabel,
                browseSaveFolderButton,
                new Separator(),
                createSuiteButton,
                selectSuiteButton,
                new Separator(),
                suiteLabel,
                new Separator(),
                createCaseButton,
                manageCasesButton,
                new Separator(),
                new Label("Test Cases in Suite:"),
                testCaseList,
                new Separator(),
                addCaseButton,
                removeCaseButton,
                saveSuiteButton,
                doneButton
        );
        layout.setStyle("-fx-padding: 20; -fx-alignment: center;");

        Scene scene = new Scene(layout, 600, 750);

        // Update save folder label
        Runnable updateSaveFolderLabel = () -> {
            String saveFolder = coordinator.getSaveFolder();
            if (saveFolder != null && !saveFolder.isEmpty())
            {
                saveFolderLabel.setText("Save Folder: " + saveFolder);
            }
            else
            {
                saveFolderLabel.setText("Save Folder: Not set");
            }
        };
        updateSaveFolderLabel.run();

        // Refresh the test case list for the current suite
        Runnable refreshSuiteCaseList = () -> {
            testCaseList.getItems().clear();
            if (coordinator.getCurrentTestSuite() != null)
            {
                suiteLabel.setText("Selected Suite: " + coordinator.getCurrentTestSuite().getTitle());
                for (String filename : coordinator.getCurrentTestSuite().getTestCaseFilenames())
                {
                    TestCase tc = coordinator.getTestCaseByFilename(filename);
                    if (tc != null)
                    {
                        testCaseList.getItems().add(tc.getTitle() + " (" + filename + ")");
                    }
                    else
                    {
                        testCaseList.getItems().add(filename + " (not found)");
                    }
                }
            }
            else
            {
                suiteLabel.setText("Selected Suite: None");
            }
        };

        browseSaveFolderButton.setOnAction(e -> {
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle("Select Folder for Test Cases and Suites");
            File selected = chooser.showDialog(primaryStage);
            if (selected != null)
            {
                coordinator.setSaveFolder(selected.getAbsolutePath());
                updateSaveFolderLabel.run();
                refreshSuiteCaseList.run();
            }
        });

        createSuiteButton.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Create Test Suite");
            dialog.setHeaderText(null);
            dialog.setContentText("Enter Test Suite Title:");
            dialog.showAndWait().ifPresent(title -> {
                if (!title.isEmpty())
                {
                    coordinator.createTestSuite(title);
                    refreshSuiteCaseList.run();
                }
            });
        });

        selectSuiteButton.setOnAction(e -> {
            List<TestSuite> suites = coordinator.getAllTestSuites();
            if (suites.isEmpty())
            {
                showErrorDialog("No Suites Available", "No test suites found. Please create a test suite first or set the save folder.");
                return;
            }

            List<String> suiteTitles = new java.util.ArrayList<>();
            for (TestSuite suite : suites)
            {
                suiteTitles.add(suite.getTitle());
            }

            ChoiceDialog<String> dialog = new ChoiceDialog<>(suiteTitles.get(0), suiteTitles);
            dialog.setTitle("Select Test Suite");
            dialog.setHeaderText(null);
            dialog.setContentText("Select test suite:");
            dialog.showAndWait().ifPresent(title -> {
                TestSuite selectedSuite = coordinator.getListOfTestSuites().findSuiteByTitle(title);
                if (selectedSuite != null)
                {
                    coordinator.setCurrentTestSuite(selectedSuite);
                    refreshSuiteCaseList.run();
                }
            });
        });

        createCaseButton.setOnAction(e -> {
            TestCase tc = promptTestCase(null);
            if (tc != null)
            {
                try
                {
                    coordinator.createAndSaveTestCase(tc);
                    showInfoDialog("Test Case Created", "Test case '" + tc.getTitle() + "' has been created and saved.");
                }
                catch (Exception ex)
                {
                    showErrorDialog("Error", "Failed to save test case: " + ex.getMessage());
                }
            }
        });

        manageCasesButton.setOnAction(e -> {
            showTestCaseManagementScreen();
        });

        addCaseButton.setOnAction(e -> {
            if (coordinator.getCurrentTestSuite() == null)
            {
                showErrorDialog("No Suite Selected", "Please create or select a test suite first.");
                return;
            }

            List<String> availableFilenames = coordinator.getAvailableTestCaseFilenames();
            if (availableFilenames.isEmpty())
            {
                showErrorDialog("No Test Cases", "No test cases available. Please create a test case first.");
                return;
            }

            ChoiceDialog<String> dialog = new ChoiceDialog<>(availableFilenames.get(0), availableFilenames);
            dialog.setTitle("Add Test Case to Suite");
            dialog.setHeaderText(null);
            dialog.setContentText("Select test case to add:");
            dialog.showAndWait().ifPresent(filename -> {
                coordinator.getCurrentTestSuite().addTestCaseFilename(filename);
                refreshSuiteCaseList.run();
            });
        });

        removeCaseButton.setOnAction(e -> {
            String selected = testCaseList.getSelectionModel().getSelectedItem();
            if (selected != null && coordinator.getCurrentTestSuite() != null)
            {
                // Extract filename from the display string
                String filename = extractFilenameFromDisplay(selected);
                coordinator.getCurrentTestSuite().removeTestCaseFilename(filename);
                refreshSuiteCaseList.run();
            }
        });

        saveSuiteButton.setOnAction(e -> {
            if (coordinator.getCurrentTestSuite() == null)
            {
                showErrorDialog("No Suite Selected", "Please create or select a test suite first.");
                return;
            }

            try
            {
                coordinator.saveTestSuite(coordinator.getCurrentTestSuite());
                showInfoDialog("Suite Saved", "Test suite '" + coordinator.getCurrentTestSuite().getTitle() + "' has been saved.");
            }
            catch (Exception ex)
            {
                showErrorDialog("Error", "Failed to save test suite: " + ex.getMessage());
            }
        });

        doneButton.setOnAction(e -> {
            System.out.println("Test Suite finalized: " +
                    (coordinator.getCurrentTestSuite() != null ? coordinator.getCurrentTestSuite().getTitle() : "none"));
        });

        refreshSuiteCaseList.run();
        primaryStage.setScene(scene);
    }

    private void showTestCaseManagementScreen()
    {
        Coordinator coordinator = this.coordinator;

        ListView<String> testCaseList = new ListView<>();
        Button editCaseButton = new Button("Edit Selected Test Case");
        Button deleteCaseButton = new Button("Delete Selected Test Case");
        Button backButton = new Button("Back");

        VBox layout = new VBox(10,
                new Label("All Test Cases:"),
                testCaseList,
                new Separator(),
                editCaseButton,
                deleteCaseButton,
                backButton
        );
        layout.setStyle("-fx-padding: 20; -fx-alignment: center;");

        Scene scene = new Scene(layout, 600, 500);

        Runnable refreshCaseList = () -> {
            testCaseList.getItems().clear();
            for (TestCase tc : coordinator.getListOfTestCases().getTestCases())
            {
                testCaseList.getItems().add(tc.getTitle() + " (" + tc.getFilename() + ")");
            }
        };

        editCaseButton.setOnAction(e -> {
            String selected = testCaseList.getSelectionModel().getSelectedItem();
            if (selected != null)
            {
                String filename = extractFilenameFromDisplay(selected);
                TestCase tc = coordinator.getTestCaseByFilename(filename);
                if (tc != null)
                {
                    TestCase edited = promptTestCase(tc);
                    if (edited != null)
                    {
                        try
                        {
                            // Update the test case
                            tc.setTitle(edited.getTitle());
                            tc.setInputData(edited.getInputData());
                            tc.setExpectedOutput(edited.getExpectedOutput());
                            tc.setType(edited.getType());
                            
                            // Save the updated test case
                            coordinator.createAndSaveTestCase(tc);
                            refreshCaseList.run();
                            showInfoDialog("Test Case Updated", "Test case has been updated and saved.");
                        }
                        catch (Exception ex)
                        {
                            showErrorDialog("Error", "Failed to save test case: " + ex.getMessage());
                        }
                    }
                }
            }
        });

        deleteCaseButton.setOnAction(e -> {
            String selected = testCaseList.getSelectionModel().getSelectedItem();
            if (selected != null)
            {
                String filename = extractFilenameFromDisplay(selected);
                TestCase tc = coordinator.getTestCaseByFilename(filename);
                if (tc != null)
                {
                    // Remove from list
                    coordinator.getListOfTestCases().removeTestCase(tc);
                    
                    // Delete file
                    try
                    {
                        String saveFolder = coordinator.getSaveFolder();
                        if (saveFolder != null && !saveFolder.isEmpty())
                        {
                            File testCaseFile = new File(new File(saveFolder, "test-cases"), filename);
                            if (testCaseFile.exists())
                            {
                                testCaseFile.delete();
                            }
                        }
                    }
                    catch (Exception ex)
                    {
                        System.err.println("Error deleting test case file: " + ex.getMessage());
                    }
                    
                    refreshCaseList.run();
                }
            }
        });

        backButton.setOnAction(e -> {
            showTestSuiteManagementScreen();
        });

        refreshCaseList.run();
        primaryStage.setScene(scene);
    }

    private String extractFilenameFromDisplay(String displayString)
    {
        // Extract filename from display string like "Title (filename.testcase)"
        int start = displayString.indexOf("(");
        int end = displayString.indexOf(")");
        if (start != -1 && end != -1 && end > start)
        {
            return displayString.substring(start + 1, end);
        }
        return displayString;
    }

    private void showInfoDialog(String title, String message)
    {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showErrorDialog(String title, String message)
    {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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

