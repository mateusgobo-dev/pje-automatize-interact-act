package br.com.pje.automatize.partes;

import br.com.pje.automatize.PjeAutomatizeTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.Select;

import java.io.Serializable;
import java.time.Duration;

public class PjeAddAutomatizeParts extends PjeAutomatizeTest implements Serializable {
    private static final long serialVersionUID = 1L;
    private  static ChromeDriver driver;

    private String[] documentos = new String[]{};

    @BeforeEach
    public void setUp() throws Exception {
        driver = this.setupDriver();
        driver.get("https://stg-03.tjrj.pje.jus.br/1g/login.seam");
    }

    @AfterAll
    public static void tearDown() throws Exception {
        try {
            driver.quit();
        }catch (Exception e) {
            throw e;
        }
    }

    @Test
    public void adicionarPartesProcesso() throws Exception {
        this.autenticarPje(driver);
        this.pesquisarProcesso(driver);
        ChromeDriver retificarAutuacao =  this.autosDigitaisRetificarAutuacao(driver);
        changeToPartes(retificarAutuacao);
    }

    public void changeToPartes(ChromeDriver chromeDriver) throws Exception {
        //Switch tab
        ChromeDriver tabPartes = (ChromeDriver) chromeDriver.switchTo().window(chromeDriver.getWindowHandles().toArray()[2].toString());
        tabPartes.findElement(By.xpath("(//td[@id='tabPartes_lbl'])[1]")).click();
        sleepTimerTwoSeconds.get();

        tabPartes.findElement(By.xpath("(//i)[11]")).click();
        Select select = new Select(tabPartes.findElement(By.tagName("select")));
        select.selectByIndex(1);
        Thread.sleep(Duration.ofSeconds(2l));

        tabPartes.findElement(By.xpath("(//input[@id='preCadastroPessoaFisicaForm:preCadastroPessoaFisica_nrCPFDecoration:preCadastroPessoaFisica_nrCPF'])[1]")).sendKeys("32454725878");
        tabPartes.findElement(By.xpath("(//input[@id='preCadastroPessoaFisicaForm:pesquisarDocumentoPrincipal'])[1]")).click();
        Thread.sleep(Duration.ofSeconds(2l));

        tabPartes.findElement(By.xpath("(//input[@id='preCadastroPessoaFisicaForm:btnConfirmarCadastro'])[1]")).click();
        tabPartes.findElement(By.xpath("(//input[@id='formInserirParteProcesso:btnComplementarDadosParte'])[1]")).click();
        Thread.sleep(Duration.ofSeconds(2l));
        tabPartes.findElement(By.xpath("(//input[@id='formInserirParteProcesso:btnInserirParteProcesso'])[1]")).click();

        WebElement element = tabPartes.findElement(By.xpath("(//span[@class='rich-messages-label'][contains(text(),\"Selecione ao menos um endereço para utilizar no pr\")])[2]"));
        if(element.isDisplayed()){
            tabPartes.findElement(By.xpath("(//input[@id='formInserirParteProcesso:btnInserirParteProcesso'])[1]")).click();
            tabPartes.findElement(By.xpath("(//input[@id='formInserirParteProcesso:cadastroPartePessoaEnderecochbkxIsEnderecoDesconhecido'])[1]")).click();
            tabPartes.findElement(By.xpath("(//i[@class='icon-fechar'])[25]")).click();
        }
        tabPartes.findElement(By.xpath("(//input[@id='formInserirParteProcesso:btnInserirParteProcesso'])[1]")).click();

        tabPartes.findElement(By.xpath("(//i[@class='icon-fechar'])[19]")).click();
    }
}
