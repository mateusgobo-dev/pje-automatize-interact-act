package br.com.peticoes;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.logging.Logger;

import static br.com.peticoes.PeticaoFunction.findByXpath;
import static br.com.peticoes.PeticaoFunction.sleepTimer;

public class BaseTestPeticao {

    protected WebDriver driver;
    private static List<String> processos = new ArrayList<>();
    static {
        processos.add("0800001-69.2024.8.19.0045");
    }
    protected String username = System.getProperty("user.name");
    protected String password = System.getProperty("user.password");
    protected String url = "https://sso.stg.cloud.pje.jus.br/auth/realms/pje/protocol/openid-connect/auth?client_id=portalexterno-frontend&redirect_uri=https%3A%2F%2Fportalexterno-tribunais.stg.pdpj.jus.br%2Fhome&state=52e38c99-790b-422c-8624-c5a340d87795&response_mode=fragment&response_type=code&scope=openid&nonce=d208e48c-b598-4047-81d8-ffd03b0f9d99";

    private Supplier<Boolean> ignorarNavegacaoGuiada = () -> {
        boolean isDisplayed = driver.findElement(By.xpath("(//div[@id='mat-dialog-title-0'])[1]")).isDisplayed();
        if (isDisplayed) {
            Logger.getLogger("Ignorando navegacao guiada....");
            driver.findElement(By.xpath("(//span[normalize-space()='Pular'])[1]")).click();
        }
        return isDisplayed;
    };

    protected final WebElement autenticar(WebDriver driver) {
        driver.findElement(By.id("username")).sendKeys("32454725878");
        driver.findElement(By.id("password")).sendKeys("Megatirador65#");
        WebElement element = findByXpath.apply("(//input[@id='kc-login'])[1]", driver);
        return element;
    }

    protected final void novaPeticao(WebDriver driver) {
        sleepTimer.get();
        this.ignorarNavegacaoGuiada.get();
        findByXpath.apply("(//mat-icon[normalize-space()='menu'])[1]", driver);
        findByXpath.apply("(//span[@class='mat-list-item-content'])[4]", driver);
    }

    protected final void buscarProcessoParaPeticaoIntercorrente(WebDriver driver) {
        sleepTimer.get();//Aguardando 5 segundos
        this.ignorarNavegacaoGuiada.get();
        driver.findElement(By.xpath("(//input[@id='mat-input-0'])[1]")).sendKeys(processos.get(0));
    }

    protected final void novaCartaPrecatoriaOrdem(WebDriver driver) {
        findByXpath.apply("(//span[normalize-space()='Nova Carta'])[1]", driver);
    }

    protected final void selecionarProcesso(WebDriver driver, String processo) {
        driver.findElement(By.xpath("(//input[@id='mat-input-0'])[1]")).sendKeys(processo);
        findByXpath.apply("(//span[normalize-space()='Buscar'])[1]", driver);
        findByXpath.apply("(//span[normalize-space()='Confirmar'])[1]", driver);
    }

    protected final void selecionarRamoJustica(WebDriver driver) {
        findByXpath.apply("//input[@id='mat-input-1']", driver);
        findByXpath.apply("(//span[normalize-space()='Justiça Estadual'])[1]", driver);
    }

    protected final void selecionarTribunal(WebDriver driver) {
        findByXpath.apply("(//input[@id='mat-input-2'])[1]", driver);
        findByXpath.apply("(//span[contains(text(),'TJRJ - Tribunal de Justiça do Estado do Rio de Jan')])[1]", driver);

    }

    protected final void selecionarInstancia(WebDriver driver) {
        WebElement element = findByXpath.apply("(//input[@id='mat-input-11'])[1]", driver);
        findByXpath.apply("(//span[normalize-space()='1º Grau - TJRJ'])[1]", driver);
    }

    protected final void selecionarComarcaZonaSecao(WebDriver driver) {
        WebElement element = findByXpath.apply("(//input[@id='mat-input-12'])[1]", driver);
//        selectValues.apply(element);
        findByXpath.apply("(//span[normalize-space()='ANGRA DOS REIS'])[1]", driver);
    }
}
