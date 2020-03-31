package com;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import cucumber.api.testng.AbstractTestNGCucumberTests;
import org.junit.runner.RunWith;



@RunWith(Cucumber.class)
@CucumberOptions(
        monochrome = true,
        plugin = {"pretty", "html:target/cucumber-html-report"},
        tags = "@Sanity"
)

public class ZeusRunner extends AbstractTestNGCucumberTests {
}
