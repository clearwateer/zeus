package com.zeus.enums;

public enum ResponseParameters {

TOKEN("tkn"),
    ID("id");


    @Override
    public String toString(){
        return uri;
    }

    private String uri;



    ResponseParameters(String uri){
        this.uri = uri;

    }
    public String getName() {
        return uri;
    }





}
