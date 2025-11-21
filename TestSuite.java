import java.util.ArrayList;
import java.util.List;
import java.io.*;
import java.nio.file.Files;

public class TestSuite
{
    private String title;
    private List<String> testCaseFilenames; // Store references to test case files

    public TestSuite(String title)
    {
        this.title = title;
        this.testCaseFilenames = new ArrayList<>();
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public List<String> getTestCaseFilenames()
    {
        return testCaseFilenames;
    }

    public void addTestCaseFilename(String filename)
    {
        if (filename != null && !filename.isEmpty() && !testCaseFilenames.contains(filename))
        {
            testCaseFilenames.add(filename);
        }
    }

    public void removeTestCaseFilename(String filename)
    {
        testCaseFilenames.remove(filename);
    }

    // Save test suite to a file
    public void saveToFile(String rootFolder) throws IOException
    {
        File suitesFolder = new File(rootFolder, "test-suites");
        if (!suitesFolder.exists())
        {
            suitesFolder.mkdirs();
        }

        String filename = sanitizeFilename(title) + ".suite";
        File suiteFile = new File(suitesFolder, filename);

        try (PrintWriter writer = new PrintWriter(new FileWriter(suiteFile)))
        {
            writer.println(title);
            for (String testCaseFilename : testCaseFilenames)
            {
                writer.println(testCaseFilename);
            }
        }
    }

    // Load test suite from a file
    public static TestSuite loadFromFile(File suiteFile) throws IOException
    {
        List<String> lines = Files.readAllLines(suiteFile.toPath());
        if (lines.isEmpty())
        {
            throw new IOException("Empty test suite file");
        }

        TestSuite suite = new TestSuite(lines.get(0));
        for (int i = 1; i < lines.size(); i++)
        {
            String filename = lines.get(i).trim();
            if (!filename.isEmpty())
            {
                suite.addTestCaseFilename(filename);
            }
        }
        return suite;
    }

    private String sanitizeFilename(String name)
    {
        return name.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
