package com.ciitizen.zeus.common;

import com.ciitizen.zeus.enums.RuntimeProperty;
import com.ciitizen.zeus.model.LoadConfig;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.PropertiesConfigurationLayout;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.apache.http.util.TextUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class ConfigHelper {




    private static final Logger logger = LogManager.getLogger(ConfigHelper.class);
    public static final String ZEUS_HOME_FOLDER = getZeusFolder();
    public static final String BASE_CONFIG_FOLDER = ZEUS_HOME_FOLDER + "src/main/resources/";
    public static final String CONFIG_FTU_FOLDER = ZEUS_HOME_FOLDER + "Ciitizen_Tests/config/";
    public static final String CONFIG_PROPERTIES_FILE = CONFIG_FTU_FOLDER + "zeusconfig.properties"; // THE properties file. Single source of information for all test configuration properties (hopefully).

    private static LoadConfig loadConfig;
    private static Map<String, String> zeusPropsMap = new ConcurrentHashMap<>();
    private static Map<String, Object> runtimeMap = new ConcurrentHashMap<>();

    private static String getZeusFolder() {
        String ZHF = System.getenv("ZEUS_TEST_HOME");
        ZHF = TextUtils.isEmpty(ZHF) ? Paths.get("").toAbsolutePath().toString() : ZHF;
        ZHF = ZHF.endsWith(File.separator) ? ZHF : ZHF + File.separator;
        return ZHF;
    }





    static {
        initializeConfigPropsMap();
    }

    public static String getZeusHomeFolder() {
        if(TextUtils.isEmpty(ZEUS_HOME_FOLDER)) {
            throw new RuntimeException("ZEUS_TEST_HOME environment variable is not configured.");
        }

        return ZEUS_HOME_FOLDER;
    }

    public static String getBaseConfigLocation() {
        if(TextUtils.isEmpty(BASE_CONFIG_FOLDER)) {
            throw new RuntimeException("ZEUS_TEST_HOME environment variable is not configured.");
        }

        return BASE_CONFIG_FOLDER;
    }

    public static LoadConfig getLoadConfig() {
        if(TextUtils.isEmpty(CONFIG_FTU_FOLDER)) {
            throw new RuntimeException("ZEUS_TEST_HOME environment variable is not configured.");
        }

        if(null == loadConfig) {
            loadConfig = new LoadConfig();
        }
        return loadConfig;
    }

    public static Properties getPropValues(String configFile) {
        Properties properties = null;
        try(InputStream in = new FileInputStream(configFile))  {
            properties = new Properties();
            properties.load(in);
        } catch (IOException ex) {
            throw new RuntimeException(String.format("Error reading properties file : %s", configFile));
        }
        return properties;
    }

    public static Properties getPropValues() {
        return getPropValues(CONFIG_FTU_FOLDER + "zeusconfig.properties");
    }

    public static void initializeConfigPropsMap(){
        Properties xmsConfigProps = getPropValues(CONFIG_FTU_FOLDER + "zeusconfig.properties");
        for(Object key : xmsConfigProps.keySet()) {
            String value = processValue((String)key, xmsConfigProps.getProperty((String)key));

            zeusPropsMap.put((String)key, value);
        }

        //Scale config properties
        Properties scaleConfigProps = getPropValues(CONFIG_FTU_FOLDER + "scaleconfig.properties");
        for(Object key : scaleConfigProps.keySet()) {
            String value = processValue((String)key, scaleConfigProps.getProperty((String)key));

            zeusPropsMap.put((String)key, value);
        }



    }

    public static String processValue(String key, String value) {
        if("${autogenerate_mac_address}".equalsIgnoreCase(value)) {
            value = randomMACAddress();
            logger.info("Auto-generate: Key {} Value {}", key, value);
        }

        return value;
    }

    private static String randomMACAddress(){
        Random rand = new Random();
        byte[] macAddr = new byte[6];
        rand.nextBytes(macAddr);

        StringBuilder sb = new StringBuilder(18);
        for(byte b : macAddr){

            if(sb.length() > 0)
                sb.append(":");

            sb.append(String.format("%02x", b));
        }


        return sb.toString();
    }

    public static Map<String, String> getConfigPropsMap() {
        return zeusPropsMap;
    }

    public static Map<String, Object> getRuntimePropsMap() {
        return runtimeMap;
    }

    public static String getConfigPropValue(String propName) {
        return zeusPropsMap.get(propName);
    }

    public static String setConfigPropValue(String propName, String propValue){
        try {
            return setConfigPropValue(propName, propValue, false);
        }catch(IOException e){
            logger.info("This exception is never expected to be hit");
            e.printStackTrace();
        }
        return null;
    }

    public static String setConfigPropValue(String propName, String propValue,boolean persist) throws IOException {
        if(persist){
            saveToConfig(propName,propValue);
        }
        return  zeusPropsMap.put(propName, propValue);
    }

    public static synchronized void saveToConfig(String propName,String propValue) throws IOException{
        try {
            PropertiesConfiguration config = new PropertiesConfiguration();
            PropertiesConfigurationLayout layout = new PropertiesConfigurationLayout(config);
            layout.load(new InputStreamReader(new FileInputStream(CONFIG_PROPERTIES_FILE)));
            config.setProperty(propName, propValue);
            layout.save(new FileWriter(CONFIG_PROPERTIES_FILE, false));
        }catch (ConfigurationException e){
            logger.error("Failed to write the Data to the Properties File",e);
            throw new IOException("Failed to write the Date to the properties File");
        }
    }

    public static void setRuntimeValue(String propName, Object value) {
        runtimeMap.put(propName, value);
    }
    public static void clearRuntimeValue(String propName) {
        runtimeMap.remove(propName);
    }
    public static Object getRuntimeValue(String propName) {
        return runtimeMap.get(propName);
    }



    public static String getAPIAuthToken(){
        return (String)ConfigHelper.getRuntimeValue(RuntimeProperty.AUTH_TOKEN.getName());
    }

    public static String replaceMacrosWithGlobalConfig(String templateFilePath) throws Exception {
        return replaceMacros(templateFilePath, zeusPropsMap);
    }

    public static String replaceMacrosWithGlobalAndRuntimeConfig(String templateFilePath) throws Exception {
        String template = replaceMacros(templateFilePath, zeusPropsMap);
        StrSubstitutor strSubstitutor = new StrSubstitutor(runtimeMap);
        String templateAfterReplacingMacros = strSubstitutor.replace(template);
        return templateAfterReplacingMacros;
    }

    public static String replaceMacros(String templateFilePath, Map<String,String> values) throws Exception {
        String template = new String(Files.readAllBytes(Paths.get(templateFilePath)));
        StrSubstitutor strSubstitutor = new StrSubstitutor(values);
        String templateAfterReplacingMacros = strSubstitutor.replace(template);
        return templateAfterReplacingMacros;
    }

    public static String replaceMacrosForContent(String content, Map<String,String> values) {
        org.apache.commons.lang.text.StrSubstitutor strSubstitutor = new org.apache.commons.lang.text.StrSubstitutor(values);
        return strSubstitutor.replace(content);
    }

    public static String replaceMacrosAndGlobalAndRuntimeConfig(String templateFilePath, Map<String,String> values) throws Exception {
        values.putAll(zeusPropsMap);
        String template = new String(Files.readAllBytes(Paths.get(templateFilePath)));
        StrSubstitutor strSubstitutor = new StrSubstitutor(values);
        String templateAfterReplacingMacros = strSubstitutor.replace(template);
        return templateAfterReplacingMacros;
    }

}


