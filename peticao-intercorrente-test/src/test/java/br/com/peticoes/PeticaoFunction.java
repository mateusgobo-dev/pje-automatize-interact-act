package br.com.peticoes;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.logging.Logger;

public interface PeticaoFunction {

    Supplier<Void> sleepTimer = () -> {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Logger.getLogger(BaseTestPeticao.class.getSimpleName()).severe(e.getMessage());
        }
        return null;
    };

    Supplier<Void> sleepTimerApplication = () -> {
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            Logger.getLogger(BaseTestPeticao.class.getSimpleName()).severe(e.getMessage());
        }
        return null;
    };

    BiFunction<String, WebDriver, WebElement> findByIdClick = (value, driver) -> {
        WebElement element = driver.findElement(By.id(value));
        element.click();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Logger.getLogger(BaseTestPeticao.class.getSimpleName()).severe(e.getMessage());
        }
        return element;
    };

    BiFunction<String, WebDriver, WebElement> findById = (value, driver) -> {
        try {
            WebElement element = driver.findElement(By.id(value));
            Thread.sleep(1000);
            return element;
        } catch (Exception e) {
            Logger.getLogger(BaseTestPeticao.class.getSimpleName()).severe(e.getMessage());
        }
        return null;
    };

    BiFunction<String, WebDriver, WebElement> findByXpath = (value, driver) -> {
        try {
            WebElement element = driver.findElement(By.xpath(value));
            Thread.sleep(1000);
            return element;
        } catch (Exception e) {
            Logger.getLogger(BaseTestPeticao.class.getSimpleName()).severe(e.getMessage());
        }
        return null;
    };

    BiFunction<String, WebDriver, WebElement> findBySelectorClass = (value, driver) -> {
        try {
            WebElement element = driver.findElement(By.cssSelector(value));
            Thread.sleep(1000);
            return element;
        } catch (Exception e) {
            Logger.getLogger(BaseTestPeticao.class.getSimpleName()).severe(e.getMessage());
        }
        return null;
    };

    BiFunction<String, WebDriver, WebElement> findByXpathClickTimeout = (value, driver) -> {
        WebElement element = driver.findElement(By.xpath(value));
        element.click();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Logger.getLogger(BaseTestPeticao.class.getSimpleName()).severe(e.getMessage());
        }
        return element;
    };
    Predicate<WebElement> nextElement = element -> !Objects.isNull(element) && element.isDisplayed();

    BiFunction<WebDriver, WebElement, Select> selectValues = (driver, webElement) -> {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofMillis(1500));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("(//input[@id='mat-input-3'])[1]")));

        Select selectOptionComunicacao = new Select(webElement);
        selectOptionComunicacao.getAllSelectedOptions().forEach(optionValue -> System.out.println(optionValue.getText()));
        return selectOptionComunicacao;
    };
}
