import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by wenbotan on 10/7/17.
 */
public class TestcaseNameUniqueCheck {

    private static Map<String, String> testcasenameMap = new HashMap<>();
    private static Map<String, List<String>> duplicateTestcasenames = new HashMap<>();
    private static String rootPath = "";

    private static void checkScenario(File file) {

        String featureName = "";
        int lineNum = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                lineNum++;
                line = line.trim();
                if (isComment(line)) {
                    continue;
                }

                if (line.startsWith("Feature:")) {
                    featureName = line.substring(8);
                    featureName = featureName.trim();
                    continue;
                }

                if (line.startsWith("Scenario:")) {
                    String scenarioName = line.substring(9);
                    scenarioName = scenarioName.trim();

                    String testcaseName = featureName + ";" + scenarioName;
                    String location = getRelativePath(file.getAbsolutePath(), rootPath) + ":" + lineNum;
                    if (testcasenameMap.containsKey(testcaseName)) {
                        if (duplicateTestcasenames.containsKey(testcaseName)) {
                            duplicateTestcasenames.get(testcaseName).add(location);
                        } else {
                            List<String> locations = new ArrayList<>();
                            locations.add(testcasenameMap.get(testcaseName));
                            locations.add(location);
                            duplicateTestcasenames.put(testcaseName, locations);
                        }
                    } else {
                        testcasenameMap.put(testcaseName, location);
                    }
                }
            }

        } catch (FileNotFoundException fnf) {
            System.out.println("File not found: " + file.getAbsolutePath() + " Message: " + fnf.getMessage());
        } catch (IOException ioe) {
            System.out.println("File loading error: " + file.getAbsolutePath() + " Message: " + ioe.getMessage());
        }
    }

    public static void main(String[] args) {
        if (args == null || args.length != 1) {
            throw new IllegalArgumentException("Expect one argument: feature file folder");
        }
        String featureFileDir = args[0];
        rootPath = new File(featureFileDir).getAbsolutePath();
        walk(featureFileDir);

        if (duplicateTestcasenames.size() != 0) {
            printDup(duplicateTestcasenames);
            System.exit(-1);
        }
    }

    private static void printDup(Map<String, List<String>> dup) {
        for (Map.Entry<String, List<String>> entry : dup.entrySet()) {
            System.out.println(entry.getKey());
            for (String loc : entry.getValue()) {
                System.out.println("\t" + loc);
            }
        }
    }

    private static void walk(String path) {

        File root = new File(path);
        File[] list = root.listFiles();

        if (list == null) return;

        for (File f : list) {
            if (f.isDirectory()) {
                walk(f.getAbsolutePath());
            } else {
                if (!isFeatureFile(f.getName())) {
                    System.out.println("Non feature file: " + f.getAbsolutePath());
                    continue;
                }

                checkScenario(f);
            }
        }
    }

    private static boolean isFeatureFile(String filename) {
        return filename.toLowerCase().endsWith(".feature");
    }

    private static boolean isComment(String line) {
        return line.startsWith("//");
    }

    private static String getRelativePath(String path, String rootPath) {
        if (path.startsWith(rootPath)) {
            return path.substring(rootPath.length());
        } else {
            System.out.println("Cannot find relative path: path=" + path + ", rootPath=" + rootPath);
        }
        return null;
    }

}

