package br.com.pje.automatize;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PjeAutomatizeTest {

    protected Supplier<Void> sleepTimerTwoSeconds = () -> {
        try {
            Thread.sleep(Duration.ofSeconds(2l));
        }catch (InterruptedException e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, e.getMessage(), e);
        }
        return null;
    };

    protected Function<Long,Void> sleepTimerInSeconds = (timeout) -> {
        try {
            Thread.sleep(Duration.ofSeconds(timeout));
        }catch (InterruptedException e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, e.getMessage(), e);
        }
        return null;
    };
    protected BiFunction<ChromeDriver, String, Void> clickWait = (driver, locator) -> {
        driver.findElement(By.xpath(locator)).click();
        sleepTimerInSeconds.apply(5l);
        return null;
    };
    protected BiFunction<ChromeDriver, String, Void> clickTabWait = (driver, locator) -> {
        driver.findElement(By.xpath(locator)).click();
        sleepTimerInSeconds.apply(4l);
        return null;
    };

    protected ChromeDriver setupDriver() {
        ChromeDriver driver = new ChromeDriver();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        driver.manage().window().maximize();
        driver.manage().deleteAllCookies();
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(60l));
        return driver;
    }


    protected void autenticarPje(ChromeDriver driver) throws Exception {
        driver.findElement(By.xpath("//input[@id='username']")).sendKeys("32454725878");
        driver.findElement(By.xpath("//input[@id='password']")).sendKeys("admin123");
        clickWait.apply(driver,"//input[@id='btnEntrar']");
        clickWait.apply(driver,"//a[normalize-space()='Prosseguir sem o Token']");
        sleepTimerInSeconds.apply(3l);
    }

    protected void pesquisarProcesso(ChromeDriver driver) throws Exception {
        clickWait.apply(driver,"//span[normalize-space()='Abrir menu']");
        clickWait.apply(driver,"(//a[@href='#'][normalize-space()='Processo'])[1]");
        clickWait.apply(driver,"//a[contains(text(),'Pesquisar')]");
        clickWait.apply(driver,"(//a[contains(text(),'Processo')])[2]");
        driver.findElement(By.xpath("//input[@id='fPP:numeroProcesso:numeroSequencial']")).sendKeys("0800001");
        driver.findElement(By.xpath("//input[@id='fPP:numeroProcesso:numeroDigitoVerificador']")).sendKeys("12");
        driver.findElement(By.xpath("//input[@id='fPP:numeroProcesso:Ano']")).sendKeys("2026");
        driver.findElement(By.xpath("//input[@id='fPP:numeroProcesso:NumeroOrgaoJustica']")).sendKeys("0203");
        clickWait.apply(driver,"//input[@id='fPP:searchProcessos']");
        driver.findElement(By.xpath("(//a[normalize-space()='0800001-12.2026.8.19.0203'])[1]")).click();
    }

    protected ChromeDriver  autosDigitaisRetificarAutuacao(ChromeDriver driver) throws Exception {
        //Switch tab
        sleepTimerInSeconds.apply(8l);

        WebDriver changeTab = driver.switchTo().window(driver.getWindowHandles().toArray()[1].toString());
        clickTabWait.apply((ChromeDriver) changeTab,"//i[@class='fa fa-pencil-square-o']");
        return driver;
    }
}
