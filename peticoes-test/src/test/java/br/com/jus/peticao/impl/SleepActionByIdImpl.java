package br.com.jus.peticao.impl;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

@FunctionalInterface
public interface SleepActionByIdImpl extends SleepActionTemplate{

    SleepActionByIdImpl sleepById = (driver, idReference, keys) -> {
        WebElement webElement =  driver.findElement(By.id(idReference)) ;
        if(keys.length > 0){
            webElement.sendKeys(keys);
        }
        threadSleep_2000_ms.get();
        return webElement;
    };
}
