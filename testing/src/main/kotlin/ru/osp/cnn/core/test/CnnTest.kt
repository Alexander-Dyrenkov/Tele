package ru.osp.cnn.core.test

import cucumber.api.CucumberOptions
import cucumber.api.junit.Cucumber
import org.junit.runner.RunWith

@RunWith(Cucumber::class)
@CucumberOptions(
    tags = ["@id-32"],
    plugin = ["pretty"],
    features = ["classpath:features/ussd"],
    glue = ["common", "cnnTest"]
)
public class CnnTest