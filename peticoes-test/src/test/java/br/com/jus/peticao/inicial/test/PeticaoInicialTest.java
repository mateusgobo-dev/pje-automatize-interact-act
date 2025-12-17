package br.com.jus.peticao.inicial.test;

import br.com.jus.peticao.inicial.impl.SleepActionImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.chrome.ChromeDriver;

import static br.com.jus.peticao.inicial.impl.SleepActionImpl.*;

public class PeticaoInicialTest extends BaseIntegrationTest{

    @BeforeEach
    public void openBrowser() {
        this.configurarChromeDriver();
    }

    @Test
    public void peticaoInicialTest() throws InterruptedException {
        autenticarUsuario();
        definirDadosPeticaoInicial();
    }

    public void definirDadosPeticaoInicial() throws InterruptedException {
        threadSleep_2000_ms.get();
        pularButtonAction.apply(driver);
        sleep.actionComponent(driver, "menu-principal", true).click();

        clickSleep3s.apply(driver,"//mat-list-item[@class='mat-list-item mat-focus-indicator active-list-item ng-star-inserted']");
    }
}
