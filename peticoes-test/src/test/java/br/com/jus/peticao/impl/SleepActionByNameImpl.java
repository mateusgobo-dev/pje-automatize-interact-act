package br.com.jus.peticao.impl;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

@FunctionalInterface
public interface SleepActionByNameImpl extends SleepActionTemplate {

    SleepActionByNameImpl sleepByName = (driver, nameReference, keys) -> {
        WebElement webElement =  driver.findElement(By.name(nameReference));
        if(keys.length > 0){
            webElement.sendKeys(keys);
        }
        threadSleep_2000_ms.get();
        return webElement;
    };
}
