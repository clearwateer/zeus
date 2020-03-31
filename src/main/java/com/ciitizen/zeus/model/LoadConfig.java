package com.ciitizen.zeus.model;

import com.ciitizen.zeus.common.ConfigHelper;
import com.ciitizen.zeus.enums.ConfigProperty;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LoadConfig {
    private static final Logger logger = LogManager.getLogger(LoadConfig.class.getName());

    public LoadConfig() {
    }

    public String getHostUrl() {
        return "https://" + ConfigHelper.getConfigPropValue(ConfigProperty.HOSTURL.getName());
    }



}

