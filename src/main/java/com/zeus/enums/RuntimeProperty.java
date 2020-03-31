package com.zeus.enums;

public enum RuntimeProperty {

    AUTH_TOKEN("authtoken"),
    HOST_COOKIES("cookies");



    private String name;

    RuntimeProperty(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
