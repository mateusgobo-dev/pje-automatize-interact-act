package br.com.grerj.automatizado;

import br.com.grerj.enumerators.ElementBy;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.Objects;

import static br.com.grerj.BaseGrerjAutomatizado.threadSleep;
import static br.com.grerj.enumerators.ElementBy.CLASS_NAME;
import static br.com.grerj.enumerators.ElementBy.URL;

@FunctionalInterface
public interface WebDriverFindElements {
    WebElement executeFindElements(WebDriver driver, ElementBy by, String element, Object value);

    WebDriverFindElements webDriverElements = (webDriver, elementBy, element,  value) -> {
        WebElement webElement = null;
        if (elementBy.equals(URL)) {
            webDriver.get(element);
        } else {
            webElement = switch (elementBy) {
                case CLASS_NAME -> webDriver.findElement(By.className(element));
                case ID -> webDriver.findElement(By.id(element));
                case XPATH -> webDriver.findElement(By.xpath(element));
                case CSS_SELETOR -> webDriver.findElement(By.cssSelector(element));
                case  TEXTFIELD -> webDriver.findElement(By.id(element));
                default -> throw new IllegalStateException("Unexpected value: " + elementBy);
            };
            if (( value ) instanceof Boolean) {
                webElement.click();
            }else{
                webElement.sendKeys((String) value);
            }
        }
        threadSleep.get();
        return webElement;
    };
}
