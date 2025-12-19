package br.com.jus.peticao.impl;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

@FunctionalInterface
public interface SleepActionByXPathImpl extends SleepActionTemplate {

    SleepActionByXPathImpl sleepByXPath = (driver, xPathReference,  keys) -> {
        WebElement webElement =  driver.findElement(By.xpath(xPathReference));
        if(keys.length > 0){
            webElement.sendKeys(keys);
        }
        threadSleep_2000_ms.get();
        return webElement;
    };

}
