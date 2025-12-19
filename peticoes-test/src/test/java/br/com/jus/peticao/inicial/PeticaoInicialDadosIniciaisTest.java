package br.com.jus.peticao.inicial;

import org.openqa.selenium.WebDriver;

import static br.com.jus.peticao.impl.SleepActionByIdImpl.sleepById;
import static br.com.jus.peticao.impl.SleepActionByXPathImpl.*;


public record PeticaoInicialDadosIniciaisTest(WebDriver driver) {
    public  void definirDadosIniciaisDestino() throws InterruptedException {
        threadSleep_2000_ms.get();
        sleepByXPath.actionComponent(driver, "//input[@placeholder='Selecione o Ramo da Justiça']").click(); //Ramo
        optionValue.apply(driver, "//span[normalize-space()='Justiça Estadual']");

        sleepByXPath.actionComponent(driver, "//input[@placeholder='Selecione o Tribunal']").click(); //Tribunal
        optionValue.apply(driver, "//span[contains(text(),'TJRJ - Tribunal de Justiça do Estado do Rio de Jan')]");

        sleepByXPath.actionComponent(driver, "//input[@formcontrolname='grau']").click();  //Instancia
        optionValue.apply(driver, "//span[normalize-space()='1º Grau']");

        threadSleep_ms.apply(50000);
        sleepByXPath.actionComponent(driver, "//input[@placeholder='Selecione a Jurisdição']").click(); //Jurisdicao
        optionValue.apply(driver, "//span[contains(text(),'Comarca da Capital - 1ª Vara da Infância, Juventud')]");
    }

    public void definirClasseJudicial() throws InterruptedException {
        sleepByXPath.actionComponent(driver, "//input[@placeholder='Selecione a Classe judicial']").click(); //Jurisdicao
        optionValue.apply(driver, "(//span[@class='mat-option-text'])[7]");
    }

    public void definirAssuntos() throws InterruptedException {
        sleepByXPath.actionComponent(driver, "(//span[@class='mat-button-wrapper'])[12]").click(); threadSleep_ms.apply(2000);
        sleepByXPath.actionComponent(driver, "//label[@for='mat-checkbox-6-input']//span[@class='mat-checkbox-inner-container mat-checkbox-inner-container-no-side-margin']").click();
        sleepByXPath.actionComponent(driver, "//span[normalize-space()='Incluir selecionados']").click();
        threadSleep_2000_ms.get();

        sleepByXPath.actionComponent(driver, "//span[@class='mat-slide-toggle-bar mat-slide-toggle-bar-no-side-margin']").click();
        sleepByXPath.actionComponent(driver, "//span[normalize-space()='Próximo']").click();
    }
}
