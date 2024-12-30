package org.myapplication.testing;

import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class TestConfigLoader {
    @SuppressWarnings("unchecked")
    public static List<Map<String, Object>> loadTests(String filePath) {
        // Initialize Yaml without specifying a Constructor
        Yaml yaml = new Yaml();

        // Read the file from the classpath
        InputStream inputStream = TestConfigLoader.class.getClassLoader().getResourceAsStream(filePath);
        if (inputStream == null) {
            throw new IllegalArgumentException("File not found: " + filePath);
        }

        // Parse the YAML file
        Map<String, Object> yamlData = yaml.load(inputStream);

        // Extract the "tests" key and cast it as a list of maps
        List<Map<String, Object>> testCases = (List<Map<String, Object>>) yamlData.get("tests");
        if (testCases == null) {
            throw new IllegalArgumentException("The 'tests' section is missing in the YAML file.");
        }

        return testCases;
    }
}