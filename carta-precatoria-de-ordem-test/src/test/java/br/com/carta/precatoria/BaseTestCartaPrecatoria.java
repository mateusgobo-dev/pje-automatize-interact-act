package br.com.carta.precatoria;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import static br.com.carta.precatoria.CartaPrecatoriaFunction.*;

public class BaseTestCartaPrecatoria {

    protected final WebElement autenticar(WebDriver driver) {
        driver.findElement(By.id("username")).sendKeys("32454725878");
        driver.findElement(By.id("password")).sendKeys("Megatirador65#");
        WebElement element = findByXpath.apply("//input[@id='kc-login']", driver);
        return element;
    }

    protected final void selecionarComarca(WebDriver driver) {
        sleepTimer.get();//Aguardando 5 segundos
        findByXpath.apply("(//mat-icon[normalize-space()='chevron_right'])[1]", driver);
        findByXpath.apply("(//mat-icon[@role='img'][normalize-space()='chevron_right'])[1]", driver);
        findByXpath.apply("(//div[@class='mat-tree-node disabled'])[18]", driver);
        findByXpath.apply("(//span[normalize-space()='CAPITAL 1 VARA CIVEL'])[1]", driver);
        findByXpath.apply("(//span[normalize-space()='Confirmar'])[1]", driver);
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
