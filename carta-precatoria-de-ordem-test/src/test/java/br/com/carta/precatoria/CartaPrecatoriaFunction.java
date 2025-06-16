package br.com.carta.precatoria;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.logging.Logger;

public interface CartaPrecatoriaFunction {

    Supplier<Void> sleepTimer = () -> {
        try {
            Thread.sleep(5000);
        }catch (InterruptedException e) {
            Logger.getLogger(BaseTestCartaPrecatoria.class.getSimpleName()).severe(e.getMessage());
        }
        return null;
    };

    BiFunction<String, WebDriver, WebElement> findByIdClick = (value, driver) -> {
        WebElement element = driver.findElement(By.id(value));
        element.click();
        try {
            Thread.sleep(1500);
        }catch (InterruptedException e) {
            Logger.getLogger(BaseTestCartaPrecatoria.class.getSimpleName()).severe(e.getMessage());
        }
        return element;
    };

    BiFunction<String, WebDriver, WebElement> findByXpath = (value, driver) -> {
        WebElement element = driver.findElement(By.xpath(value));
        element.click();
        try {
            Thread.sleep(1500);
        }catch (InterruptedException e) {
            Logger.getLogger(BaseTestCartaPrecatoria.class.getSimpleName()).severe(e.getMessage());
        }
        return element;
    };

    BiFunction<WebDriver, WebElement, Select> selectValues = (driver,webElement) -> {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofMillis(1500));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("(//input[@id='mat-input-3'])[1]")));

        Select selectOptionComunicacao = new Select(webElement);
        selectOptionComunicacao.getAllSelectedOptions().forEach(optionValue -> System.out.println(optionValue.getText()));
        return selectOptionComunicacao;
    };
}
