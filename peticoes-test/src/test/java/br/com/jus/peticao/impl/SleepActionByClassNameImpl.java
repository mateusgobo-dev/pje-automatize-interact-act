package br.com.jus.peticao.impl;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

@FunctionalInterface
public interface SleepActionByClassNameImpl extends SleepActionTemplate {

    SleepActionByClassNameImpl sleepByXPath = (driver, classNameReference, keys) -> {
        WebElement webElement =  driver.findElement(By.className(classNameReference));
        if(keys.length > 0){
            webElement.sendKeys(keys);
        }
        threadSleep_2000_ms.get();
        return webElement;
    };

}
