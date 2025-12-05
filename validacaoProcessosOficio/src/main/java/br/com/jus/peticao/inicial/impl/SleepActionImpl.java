package br.com.jus.peticao.inicial.impl;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.function.BiFunction;
import java.util.function.Function;

@FunctionalInterface
public interface SleepActionImpl {

    WebElement actionComponent(WebDriver driver, String reference, CharSequence...keys) throws InterruptedException;

    SleepActionImpl sleep = (driver, reference, keys) -> {
        WebElement webElement = driver.findElement(By.xpath(reference));
        if(keys.length > 0){
            webElement.sendKeys(keys);
        }
        Thread.sleep(1000l);
        return webElement;
    };

    BiFunction<WebDriver, String, Void> click = (driver, reference) -> {
        WebElement webElement = driver.findElement(By.xpath(reference));
        webElement.click();
        return null;
    };
}
