package com.zeus.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class ServicesCache {
    private static Logger logger= LoggerFactory.getLogger(ServicesCache.class);
    private static Map<String, Map<String, Object>> commonCache ;

    public ServicesCache(){

        commonCache=new HashMap<String,Map<String,Object>>();


    }



    public static Object getCachedModuleObject(String modulePath, String objectKey) {
        Map<String, Object> moduleMap = getModuleMap(modulePath);


        Object moduleObject = moduleMap.get(objectKey);
        return moduleObject;
    }


    public static void updateCacheForModuleObject(String modulePath, String objectKey, Object object) {
        Map<String, Object> moduleMap = getModuleMap(modulePath);

        moduleMap.put(objectKey, object);
    }



    public static Map<String, Object> getModuleMap(String modulePath) {
        Map<String, Object> moduleMap = commonCache.get(modulePath);

        if(moduleMap == null) {
            moduleMap = new HashMap<String,Object>();
            commonCache.put(modulePath, moduleMap);
        }

        return moduleMap;
    }














}
