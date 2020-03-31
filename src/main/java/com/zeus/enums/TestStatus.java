package com.zeus.enums;

public enum TestStatus {
    PASS("Pass"),
    FAIL("Fail"),
    SKIPPED("Skipped");

    private final String value;

    TestStatus(String status) {
        this.value = status;
    }

}
