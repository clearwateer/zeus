import com.ciitizen.zeus.enums.TestStatus;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.*;

public class MainDriver {



    // the output of this file goes to debug.log based on log4j.xml
    private static final Logger logger = LogManager.getLogger(MainDriver.class);
    private static final String RESULT_FMT = "{'passed': %d, 'failed': %d}";

    private static Map<String, String> params = new HashMap<>();





    public static void main(String[] args) throws InterruptedException , Exception{


        String resultFromCucumber = run(); // collect cucumber result


        try {
            TestResultBreakdown resultDetails = getResultFromJson(resultFromCucumber);
            TestResultBreakdown resultFromLog  = getTestResult();

            resultDetails.combine(resultFromLog);
          ;
            System.out.println(resultDetails);
            logger.info(resultDetails);
        } catch (Exception e) {
            logger.error("Get resultFromCucumber error", e);
        }


        logger.info("Exiting main function");

        System.exit(0);
    }



    public static class TestResultBreakdown {
        int passed = 0;
        int failed = 0;
        int skipped = 0;
        String ExternalReportURL = "";
        ArrayList<String> arguments = new ArrayList<>();
        ArrayList<String> testResultBreakdown = new ArrayList<>();

        public void combine(TestResultBreakdown that) {
            this.passed += that.passed;
            this.failed += that.failed;
            this.skipped += that.skipped;
            testResultBreakdown.addAll(that.testResultBreakdown);
        }

        @Override
        public String toString() {
            StringBuilder strArguments = new StringBuilder();
            strArguments.append("{");
            for (String arg : arguments) {
                strArguments.append(arg).append(",");
            }
            strArguments.append("}");

            StringBuilder strTestBreakdown = new StringBuilder();
            strTestBreakdown.append("[");
            for (String test : testResultBreakdown) {
                strTestBreakdown.append("{").append(test).append("},");
            }
            if (strTestBreakdown.length() > 1) strTestBreakdown.deleteCharAt(strTestBreakdown.length()-1);

            strTestBreakdown.append("]");

            return "{" +
                    "'passed':" + passed + "," +
                    "'failed':" + failed + "," +
                    "'skipped':" + skipped + "," +
                    (ExternalReportURL.equalsIgnoreCase("") ? "" : ("'ExternalReportURL':'" + ExternalReportURL + "',")) +
                    (arguments.size() > 0 ? ("'arguments':" + strArguments.toString() + ",") : "") +
                    "'TestResultBreakdown':" + strTestBreakdown.toString()  +
                    '}';
        }
    }



    /**
     * Function that triggers your test
     *
     */
    public static String run() {
        try {
            ProcessBuilder pb = getProcessBuilder();
            pb.redirectErrorStream(true);

            Process proc;
            BufferedReader stdout;
            int rc;

            proc = pb.start();
            stdout = new BufferedReader(new InputStreamReader(proc.getInputStream()));

            StringBuilder details = new StringBuilder();
            StringBuilder jsonString = new StringBuilder();
            boolean isJson = false;
            String line;
            while ((line = stdout.readLine()) != null) {
                details.append(line).append("\n");
                if (line.equalsIgnoreCase("[")) {
                    isJson = true;
                }
                if (line.equalsIgnoreCase("]")) {
                    jsonString.append(line).append("\n");
                    isJson = false;
                    if (!isCucumberJson(jsonString.toString())) {
                        jsonString = new StringBuilder();
                    }
                }
                if (isJson) {
                    jsonString.append(line).append("\n");
                }
            }

            rc = proc.waitFor();

            try (PrintWriter out = new PrintWriter("logs/cucumber_result.log")) {
                out.print(details);
            }

            try (PrintWriter out = new PrintWriter("logs/cucumber_json.log")) {
                out.print(jsonString);
            }

            return jsonString.toString();
        } catch (Exception e) {
            logger.error("Error getting cucumber result", e);
            return null;
        }
    }

    // check if json object is the cucumber result
    private static boolean isCucumberJson(String json) {
        JsonArray features = new JsonParser().parse(json).getAsJsonArray();
        if (features.size() <= 0) return false;

        JsonObject feature = (JsonObject)features.get(0);

        return (feature.has("elements") && feature.has("line"));
    }

    /**
     * Function that collects test logs and get test result
     *
     */
    public static TestResultBreakdown getTestResult() {
        TestResultBreakdown testR = new TestResultBreakdown();

        try (BufferedReader br = new BufferedReader(new FileReader("logs/result.log"))) {
            String line;
            TestStatus s;

            while((line = br.readLine()) != null) {
                String[] components = line.split("\\|\\|");
                if (components.length < 2) {
                    continue;
                }
                String testName = components[0];
                String testStatus = components[1];
                // String testDetails = components[2];

                try {
                    s = TestStatus.valueOf(testStatus.toUpperCase());
                } catch (IllegalArgumentException e) {
                    logger.error("error parsing test status: " + testStatus + ": " + e.getMessage());
                    continue;
                }
                switch (s) {
                    case PASS:
                        testR.passed++;
                        testR.testResultBreakdown.add("'" + testName + "':'passed'");
                        break;
                    case FAIL:
                        testR.failed++;
                        testR.testResultBreakdown.add("'" + testName + "':'failed'");
                        break;
                    case SKIPPED:
                        testR.skipped++;
                        testR.testResultBreakdown.add("'" + testName + "':'skipped'");
                        break;
                }
            }
        } catch (IOException e) {
            logger.error("Exception while parsing results: " + e.getMessage());
        }

        return testR;
    }

    public static TestResultBreakdown getResultFromJson(String jsonInput) {

        TestResultBreakdown testDetails = new TestResultBreakdown();

        if (jsonInput == null || jsonInput.equalsIgnoreCase("")) {
            return testDetails;
        }

        JsonArray features = new JsonParser().parse(jsonInput).getAsJsonArray();
        for(int i = 0; i < features.size(); i++) {
            JsonObject feature = (JsonObject)features.get(i);
            JsonArray scenarios = feature.getAsJsonArray("elements");
            if (scenarios == null) continue;

            for (int j = 0; j < scenarios.size(); j++) {
                JsonObject scenario = (JsonObject) scenarios.get(j);
                // skip element whose type is not scenario
                if (!scenario.get("type").getAsString().equalsIgnoreCase("scenario")) {
                    continue;
                }
                String scenario_id = scenario.get("id").getAsString();
                boolean scenario_pass = true;
                boolean scenario_fail = false;
                boolean scenario_skip = false;
                JsonArray steps = scenario.getAsJsonArray("steps");

                for (int m = 0; m < steps.size(); m++) {
                    JsonObject step = (JsonObject)steps.get(m);
                    JsonObject stepResult = (JsonObject)step.get("result");
                    String s = stepResult.get("status").getAsString();
                    if (s.equalsIgnoreCase("failed") || s.equalsIgnoreCase("pending")) {
                        scenario_pass = false;
                        scenario_fail = true;
                        break;
                    }
                    if (s.equalsIgnoreCase("skipped")) {
                        scenario_pass = false;
                        scenario_skip = true;
                        break;
                    }
                }

                checkStatus(scenario_id, scenario_pass, scenario_fail, scenario_skip);

                if (scenario_pass) {
                    testDetails.testResultBreakdown.add("'" + scenario_id + "':'passed'");
                    testDetails.passed++;
                }
                if (scenario_fail) {
                    testDetails.testResultBreakdown.add("'" + scenario_id + "':'failed'");
                    testDetails.failed++;
                }
                if (scenario_skip) {
                    testDetails.testResultBreakdown.add("'" + scenario_id + "':'skipped'");
                    testDetails.skipped++;
                }
            }

        }

        return testDetails;
    }





    /**
     * only one of them is true
     * @param pass
     * @param fail
     * @param skip
     */
    private static void checkStatus(String id, boolean pass, boolean fail, boolean skip) {
        int countTrue = 0;

        if (pass) countTrue++;
        if (fail) countTrue++;
        if (skip) countTrue++;

        if (countTrue != 1) {
            logger.error("Status Error for scenario " + id + " : pass=" + pass + ", fail=" + fail + ", skip=" + skip);
        }

    }

    private static ProcessBuilder getProcessBuilder() {
        String CMD = "java";
        String CP = "-cp";
        String CLASS_PATH = "build/output/libs/*";
        String CUCUMBER_MAIN = "cucumber.api.cli.Main";
        String PLUG_IN = "-p";
        String OUTPUT_FORMAT = "json";
        String GLUE = "-g";
        String GLUE_CLASS = "com.ciitizen.zulu";
        String TAGS = "-t";
        String FEATURE_FILE_DIR = "feature_files/";




        List<String> listCommand = new ArrayList<String>();
        // run real device tet cases

        listCommand.add(CMD);
        listCommand.add(CP);
        listCommand.add(CLASS_PATH);
        listCommand.add(CUCUMBER_MAIN);
        listCommand.add(PLUG_IN);
        listCommand.add(OUTPUT_FORMAT);
        listCommand.add(GLUE);
        listCommand.add(GLUE_CLASS);
        listCommand.add(TAGS);
        listCommand.add("~@ignored");


        return new ProcessBuilder(CMD, CP, CLASS_PATH, CUCUMBER_MAIN, PLUG_IN, OUTPUT_FORMAT, GLUE, GLUE_CLASS, TAGS, "~@ignored", TAGS, "~@cloud", FEATURE_FILE_DIR);
    }







}
