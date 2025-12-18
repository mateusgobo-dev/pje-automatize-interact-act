package br.com.jus.peticao.inicial;

import org.openqa.selenium.WebDriver;

import static br.com.jus.peticao.inicial.impl.SleepActionImpl.*;

public record PeticaoInicialPartesTest(WebDriver driver) {
    public void definirPoloAtivo() throws InterruptedException {
        threadSleep_2000_ms.get();
        sleep.actionComponent(driver, "mat-expansion-panel-header-0", true).click();
        sleep.actionComponent(driver, "//label[@for='mat-radio-11-input']//span[@class='mat-radio-label-content']", false).click();
        sleep.actionComponent(driver, "//label[@for='mat-radio-26-input']//span[@class='mat-radio-label-content']", false).click();
        sleep.actionComponent(driver, "", true, "324.547.258-78");//?
        clickSleep3s.apply(driver, "(//span[@class='mat-button-wrapper'])[9]");
        clickSleep3s.apply(driver, "//span[normalize-space()='Confirmar']");

        threadSleep_2000_ms.get();
        sleep.actionComponent(driver, "mat-tab-label-0-2", true).click();
        sleep.actionComponent(driver, "mat-input-27", true,  "14051-220");
        clickSleep3s.apply(driver, "(//span[@class='mat-button-wrapper'])[11]");
    }

    public void definirPoloPassivo() throws InterruptedException {
        sleep.actionComponent(driver, "mat-expansion-panel-header-1", true).click();
    }
}
