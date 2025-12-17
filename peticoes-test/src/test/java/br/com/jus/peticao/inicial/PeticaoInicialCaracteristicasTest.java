package br.com.jus.peticao.inicial;

import org.openqa.selenium.WebDriver;

import static br.com.jus.peticao.inicial.impl.SleepActionImpl.*;


public record PeticaoInicialCaracteristicasTest(WebDriver driver) {
    public  void definirCaracteristicasProcesso() throws InterruptedException {
        threadSleep_ms.apply(5000);
        sleep.actionComponent(driver, "(//span[@class='mat-radio-inner-circle'])[1]", false).click(); //Justica gratuita
        sleep.actionComponent(driver, "(//span[@class='mat-radio-inner-circle'])[4]", false).click();//Liminar ou antecipacao de tutela
        sleep.actionComponent(driver, "mat-input-9", true, "10000");//Valor da causa
        sleep.actionComponent(driver,"(//span[@class='mat-radio-inner-circle'])[6]", false).click(); //Segredo de justiça
    }

    public void definirPrioridadeProcesso() throws InterruptedException {
        sleep.actionComponent(driver, "mat-select-value-7", true).click(); //Segredo justica
        sleep.actionComponent(driver, "//span[normalize-space()='Juízo 100% digital']", false).click();
        threadSleep_ms.apply(4000);
        sleep.actionComponent(driver,"mat-input-22", true).click();
        threadSleep_2000_ms.get();
        sleep.actionComponent(driver, "//span[normalize-space()='Próximo']", false).click();

        threadSleep_ms.apply(5000);
    }
}
