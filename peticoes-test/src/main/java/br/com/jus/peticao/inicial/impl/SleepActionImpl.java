package br.com.jus.peticao.inicial.impl;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

@FunctionalInterface
public interface SleepActionImpl {

    Supplier<Void> threadSleep_2000_ms = () -> {
        try {
            Thread.sleep(2000l);
            return null;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    };

    Function<Integer,Void> threadSleep_ms = (timeout) -> {
        try {
            Thread.sleep(timeout);
            return null;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    };
    Function<WebDriver, Void> pularButtonAction = (driver) -> {
        WebElement webElement = driver.findElement(By.xpath("//button[@class='mat-focus-indicator btn-false mat-button mat-button-base ng-star-inserted']"));
        if(webElement.isDisplayed()){
            webElement.click();
        }
        return null;
    };

    WebElement actionComponent(WebDriver driver, String reference, boolean byId, CharSequence...keys) throws InterruptedException;

    SleepActionImpl sleep = (driver, reference, byId,  keys) -> {
        WebElement webElement =  byId ? driver.findElement(By.id(reference)) : driver.findElement(By.xpath(reference));
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

    BiFunction<WebDriver, String, Void> clickSleep3s = (driver, reference) -> {
        WebElement webElement = driver.findElement(By.xpath(reference));
        webElement.click();
        threadSleep_ms.apply(3000);
        return null;
    };

    BiFunction<WebDriver, String, Select> selectSleep2s = (driver, reference) -> {
        Select select = new Select(driver.findElement(By.xpath(reference)));
        threadSleep_2000_ms.get();
        return select;
    };
}
