import java.io.*;
import java.nio.file.Files;

public class TestCase
{
    private String title;
    private String inputData;
    private String expectedOutput;
    private String type;

    public TestCase(String title, String inputData, String expectedOutput, String type)
    {
        this.title = title;
        this.inputData = inputData;
        this.expectedOutput = expectedOutput;
        this.type = type;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getInputData()
    {
        return inputData;
    }

    public void setInputData(String inputData)
    {
        this.inputData = inputData;
    }

    public String getExpectedOutput()
    {
        return expectedOutput;
    }

    public void setExpectedOutput(String expectedOutput)
    {
        this.expectedOutput = expectedOutput;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    // Get the filename for this test case
    public String getFilename()
    {
        return sanitizeFilename(title) + ".testcase";
    }

    // Save test case to a file
    public void saveToFile(String rootFolder) throws IOException
    {
        File testCasesFolder = new File(rootFolder, "test-cases");
        if (!testCasesFolder.exists())
        {
            testCasesFolder.mkdirs();
        }

        File testCaseFile = new File(testCasesFolder, getFilename());

        try (PrintWriter writer = new PrintWriter(new FileWriter(testCaseFile)))
        {
            writer.println(title);
            writer.println(type);
            writer.println(inputData);
            writer.println(expectedOutput);
        }
    }

    // Load test case from a file
    public static TestCase loadFromFile(File testCaseFile) throws IOException
    {
        java.util.List<String> lines = Files.readAllLines(testCaseFile.toPath());
        if (lines.size() < 4)
        {
            throw new IOException("Invalid test case file format");
        }

        return new TestCase(
            lines.get(0), // title
            lines.get(2), // inputData
            lines.get(3), // expectedOutput
            lines.get(1)  // type
        );
    }

    private String sanitizeFilename(String name)
    {
        return name.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
