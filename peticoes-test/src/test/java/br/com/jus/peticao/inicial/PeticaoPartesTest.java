package br.com.jus.peticao.inicial;

import org.openqa.selenium.WebDriver;

import static br.com.jus.peticao.inicial.impl.SleepActionImpl.sleep;

public record PeticaoPartesTest(WebDriver driver) {
    public void definirPoloAtivo() throws InterruptedException {
        sleep.actionComponent(driver, "//mat-panel-title[@class='mat-expansion-panel-header-title ng-tns-c139-56']", false).click();

    }
}
