package br.com.jus.peticao.inicial;

import br.com.jus.peticao.impl.SleepActionByNameImpl;
import org.openqa.selenium.WebDriver;

import static br.com.jus.peticao.impl.SleepActionByIdImpl.sleepById;
import static br.com.jus.peticao.impl.SleepActionByNameImpl.sleepByName;
import static br.com.jus.peticao.impl.SleepActionByXPathImpl.*;

public record PeticaoInicialPartesTest(WebDriver driver) {
    public void definirPoloAtivo() throws InterruptedException {
        threadSleep_2000_ms.get();
        sleepById.actionComponent(driver, "mat-expansion-panel-header-0").click();
        sleepByXPath.actionComponent(driver, "//mat-radio-button[.//input[@value='FISICA']]").click();
        sleepByXPath.actionComponent(driver, "//mat-radio-button[.//input[@value='true']]").click();
        sleepByXPath.actionComponent(driver, "(//input[@formcontrolname='cpf'])[1]" ,"324.547.258-78");
        clickSleep3s.apply(driver, "(//span[@class='mat-button-wrapper'])[8]");
        clickSleep3s.apply(driver, "//span[normalize-space()='Confirmar']");

        threadSleep_2000_ms.get();
        sleepByXPath.actionComponent(driver, "//div[contains(text(),'Contatos')]").click();
        sleepByXPath.actionComponent(driver, "(//input[@formcontrolname='cep'])[1]",  "14051-220");
        clickSleep3s.apply(driver, ".//button[@class='mat-focus-indicator mat-raised-button mat-button-base mat-primary']");
        sleepByXPath.actionComponent(driver, "//input[@formcontrolname='numero'][1]").sendKeys("2479");
        sleepByXPath.actionComponent(driver, "//span[@class='mat-checkbox-inner-container']").click();
        clickSleep3s.apply(driver, "//span[normalize-space()='Incluir Endereço']");
        clickSleep3s.apply(driver, "//span[normalize-space()='Confirmar'][1]");

        sleepByXPath.actionComponent(driver,  "//mat-select[//@formcontrolname='tipo']").click();
    }

    public void definirPoloPassivo() throws InterruptedException {
        sleepById.actionComponent(driver, "mat-expansion-panel-header-1").click();
    }
}
