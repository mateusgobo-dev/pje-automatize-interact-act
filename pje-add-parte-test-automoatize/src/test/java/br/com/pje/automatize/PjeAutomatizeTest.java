package br.com.pje.automatize;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
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

    protected Function<ChromeDriver,WebDriverWait> wait = (driver) -> new WebDriverWait(driver, Duration.ofSeconds(10));
    protected Function<Long,Void> sleepTimerInSeconds = (timeout) -> {
        try {
            Thread.sleep(Duration.ofSeconds(timeout));
        }catch (InterruptedException e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, e.getMessage(), e);
        }
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
        wait.apply(driver).until(ExpectedConditions.elementToBeClickable(By.xpath("//input[@id='btnEntrar']"))).click();
        driver.findElement(By.xpath("//a[normalize-space()='Prosseguir sem o Token']")).click();
        sleepTimerInSeconds.apply(3l);
    }

    protected void pesquisarProcesso(ChromeDriver driver) throws Exception {
        driver.findElement(By.xpath("//span[normalize-space()='Abrir menu']")).click();
        driver.findElement(By.xpath("(//a[@href='#'][normalize-space()='Processo'])[1]")).click();
        driver.findElement(By.xpath("//a[contains(text(),'Pesquisar')]")).click();
        driver.findElement(By.xpath("(//a[contains(text(),'Processo')])[2]")).click();
        driver.findElement(By.xpath("//input[@id='fPP:numeroProcesso:numeroSequencial']")).sendKeys("0800001");
        driver.findElement(By.xpath("//input[@id='fPP:numeroProcesso:numeroDigitoVerificador']")).sendKeys("12");
        driver.findElement(By.xpath("//input[@id='fPP:numeroProcesso:Ano']")).sendKeys("2026");
        driver.findElement(By.xpath("//input[@id='fPP:numeroProcesso:NumeroOrgaoJustica']")).sendKeys("0203");
        wait.apply(driver).until(ExpectedConditions.elementToBeClickable(By.xpath("//input[@id='fPP:searchProcessos']"))).click();

        sleepTimerTwoSeconds.get();
        driver.findElement(By.xpath("(//a[normalize-space()='0800001-12.2026.8.19.0203'])[1]")).click();
        sleepTimerTwoSeconds.get();
    }

    protected ChromeDriver  autosDigitaisRetificarAutuacao(ChromeDriver driver) throws Exception {
        //Switch tab
        WebDriver changeTab = driver.switchTo().window(driver.getWindowHandles().toArray()[1].toString());
        WebElement element = changeTab.findElement(By.xpath("//i[@class='fa fa-pencil-square-o']"));
        element.click();

        sleepTimerInSeconds.apply(3l);
        return driver;
    }
}
