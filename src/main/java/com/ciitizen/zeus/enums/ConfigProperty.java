package com.ciitizen.zeus.enums;

public enum ConfigProperty {
    HOSTURL("host.url"),
    SERVICE_NAME("service.name"),
    USER_NAME("username"),
    PASSWORD("password");



    private String name;

    ConfigProperty(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
