package br.com.jus.peticao.inicial;

import org.openqa.selenium.WebDriver;

import static br.com.jus.peticao.impl.SleepActionByIdImpl.sleepById;
import static br.com.jus.peticao.impl.SleepActionByXPathImpl.*;


public record PeticaoInicialCaracteristicasTest(WebDriver driver) {
    public  void definirCaracteristicasProcesso() throws InterruptedException {
        threadSleep_ms.apply(5000);
        sleepByXPath.actionComponent(driver, "(//span[@class='mat-radio-inner-circle'])[1]").click(); //Justica gratuita
        sleepByXPath.actionComponent(driver, "(//span[@class='mat-radio-inner-circle'])[4]").click();//Liminar ou antecipacao de tutela
        sleepById.actionComponent(driver, "mat-input-10", "10000");//Valor da causa
        sleepByXPath.actionComponent(driver,"(//span[@class='mat-radio-inner-circle'])[6]").click(); //Segredo de justiça

        sleepByXPath.actionComponent(driver,"//span[normalize-space()='Próximo']").click();
        threadSleep_ms.apply(5000);
    }

//    public void definirPrioridadeProcesso() throws InterruptedException {
//        sleep.actionComponent(driver, "mat-select-value-7", true).click(); //Prioridade do processo
//        sleep.actionComponent(driver, "//span[normalize-space()='Juízo 100% digital']").click();
//        sleep.actionComponent(driver,"//span[normalize-space()='Próximo']").click();
//
//        threadSleep_ms.apply(5000);
//    }
}
