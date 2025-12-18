package br.com.jus.peticao.inicial;

import br.com.jus.peticao.test.BaseIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static br.com.jus.peticao.inicial.impl.SleepActionImpl.*;

public class PeticaoInicialTest extends BaseIntegrationTest {

    @BeforeEach
    public void openBrowser() {
        this.configurarChromeDriver();
        this.peticaoInicialDadosIniciaisTest = new PeticaoInicialDadosIniciaisTest(driver);
        this.peticaoInicialCaracteristicasTest = new PeticaoInicialCaracteristicasTest(driver);
        this.peticaoInicialPartesTest = new PeticaoInicialPartesTest(driver);
    }

    @Test
    public void peticaoInicialTest() throws InterruptedException {
        autenticarUsuario();
        definirDadosPeticaoInicial();
        this.peticaoInicialDadosIniciaisTest.definirDadosIniciaisDestino();
        this.peticaoInicialDadosIniciaisTest.definirClasseJudicial();
        this.peticaoInicialDadosIniciaisTest.definirAssuntos();
        this.peticaoInicialCaracteristicasTest.definirCaracteristicasProcesso();
        this.peticaoInicialPartesTest.definirPoloAtivo();
    }

    private void definirDadosPeticaoInicial() throws InterruptedException {
        threadSleep_2000_ms.get();
        pularButtonAction.apply(driver);
        sleep.actionComponent(driver, "menu-principal", true).click();
        threadSleep_2000_ms.get();
        clickSleep3s.apply(driver,"(//span[@class='mat-list-item-content'])[5]");
    }
}
