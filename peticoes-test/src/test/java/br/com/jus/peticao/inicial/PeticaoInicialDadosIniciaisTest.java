package br.com.jus.peticao.inicial;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import static br.com.jus.peticao.inicial.impl.SleepActionImpl.*;


public record PeticaoInicialDadosIniciaisTest(WebDriver driver) {
    public  void definirDadosIniciaisDestino() throws InterruptedException {
        threadSleep_2000_ms.get();
        sleep.actionComponent(driver, "mat-input-5", true).click(); //Ramo
        optionValue.apply(driver, "//span[normalize-space()='Justiça Estadual']");

        sleep.actionComponent(driver, "mat-input-6", true).click(); //Tribunal
        optionValue.apply(driver, "//span[contains(text(),'TJRJ - Tribunal de Justiça do Estado do Rio de Jan')]");

        sleep.actionComponent(driver, "mat-select-value-3", true).click();  //Instancia
        optionValue.apply(driver, "//span[normalize-space()='1º Grau']");

        threadSleep_ms.apply(50000);
        sleep.actionComponent(driver, "mat-input-7", true).click(); //Jurisdicao
        optionValue.apply(driver, "//span[contains(text(),'Comarca da Capital - 1ª Vara da Infância, Juventud')]");
    }

    public void definirClasseJudicial() throws InterruptedException {
        sleep.actionComponent(driver, "mat-input-2", true).click(); //Jurisdicao
        optionValue.apply(driver, "(//span[@class='mat-option-text'])[7]");
    }

    public void definirAssuntos() throws InterruptedException {
        sleep.actionComponent(driver, "(//span[@class='mat-button-wrapper'])[12]", false).click(); threadSleep_ms.apply(2000);
        sleep.actionComponent(driver, "//label[@for='mat-checkbox-6-input']//span[@class='mat-checkbox-inner-container mat-checkbox-inner-container-no-side-margin']", false).click();
        sleep.actionComponent(driver, "//span[normalize-space()='Incluir selecionados']", false).click();
        threadSleep_2000_ms.get();

        sleep.actionComponent(driver, "//span[@class='mat-slide-toggle-bar mat-slide-toggle-bar-no-side-margin']", false).click();
        sleep.actionComponent(driver, "//span[normalize-space()='Próximo']", false).click();
    }
}
