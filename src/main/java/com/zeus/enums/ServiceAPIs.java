package com.zeus.enums;

public enum ServiceAPIs {

    CONSOLE_LOGIN("/login"),
    CREATE_USER("/notifications");


    @Override
    public String toString(){
        return uri;
    }

    private String uri;



    ServiceAPIs(String uri){
        this.uri = uri;

    }
    public String getName() {
        return uri;
    }




}