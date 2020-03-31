package com.zeus.enums;

public enum APIQueryParameters {

    receipient("recipient"),
    type("type");



    @Override
    public String toString(){
        return uri;
    }

    private String uri;



    APIQueryParameters(String uri){
        this.uri = uri;

    }
    public String getName() {
        return uri;
    }



}
