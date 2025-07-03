package br.com.grerj.automatizado;

import br.com.grerj.BaseGrerjAutomatizado;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.Select;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

import static br.com.grerj.automatizado.WebDriverFindElements.webDriverElements;
import static br.com.grerj.enumerators.ElementBy.*;

public class GrerjAutomatizadoTest extends BaseGrerjAutomatizado {

    private static final List<String> nroGrerjCollection = new ArrayList<>();

    @BeforeEach
    public void beforeEach() {
        this.webDriver = new ChromeDriver();
        this.webDriver.manage().window().maximize();
    }

    @AfterAll
    public static void afterAll() {
        nroGrerjCollection.stream().forEach(System.out::println);
        try {
            Path path = new File("%s\\src\\test\\resources\\test-resources-%s.txt".formatted(mainPath.toString(), System.currentTimeMillis())).toPath();
            Path file = Path.of(path.toUri());
            if (Files.notExists(file)) Files.createFile(file);
            StringBuilder values = new StringBuilder();
            nroGrerjCollection.stream().forEach(value -> {
                values.append(value).append(System.lineSeparator());
            });
            Files.writeString(file, values.toString(), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            Logger.getLogger(GrerjAutomatizadoTest.class.getSimpleName(), ex.getMessage());
        }
    }

    private void processoJudicialCabecalho() {
        webDriverElements.executeFindElements(webDriver, URL, "https://wwwh3.tjrj.jus.br/hgrerjweb/#/judicial/processo-judicial", false);
        webDriverElements.executeFindElements(webDriver, ID, "grerjInicial", true);
        webDriverElements.executeFindElements(webDriver, XPATH, "//div[@id='modal-alerta']//button[@type='button'][normalize-space()='Ok']", true);
        webDriverElements.executeFindElements(webDriver, XPATH, "(//select[@id='comarca'])[1]", true);
        webDriverElements.executeFindElements(webDriver, XPATH, "(//option[@value='1: Object'])[1]", true);
        webDriverElements.executeFindElements(webDriver, TEXTFIELD, "cpfCnpjRecolhedor", "32454725878");
        webDriverElements.executeFindElements(webDriver, TEXTFIELD, "nomeResponsavelRecolhimento", "MATEUS EDUARDO GOBO");
    }

    private void processoJudicialCustas() {
        webDriverElements.executeFindElements(webDriver, XPATH, "(//button[@data-original-title='Pesquisar Órgão'])[1]", true);
        final WebElement element = webDriverElements.executeFindElements(webDriver, XPATH, "(//div[@class='form-group col-md-12'])[1]", false);
        final WebElement select = element.findElement(By.id("comarca"));
        select.click();

        Select selectOption = new Select(select);
        List<WebElement> options = selectOption.getOptions();
        options.stream().filter(option -> option.getText().equalsIgnoreCase("atos processuais")).findFirst().get().click();
        threadSleep.get();

        webDriverElements.executeFindElements(webDriver, XPATH, "(//div[@class='col-md-12'][normalize-space()='ARREMATAÇÃO'])[1]", true);
        webDriverElements.executeFindElements(webDriver, XPATH, "(//button[@type='submit'])[1]", true);
        webDriverElements.executeFindElements(webDriver, XPATH, "(//button[@type='button'][normalize-space()='Ok'])[2]", true);
        threadSleep.get();

        webDriverElements.executeFindElements(webDriver, XPATH, "(//button[@type='button'][normalize-space()='Ok'])[2]", true);
    }

    private void processoJudicialCalculoAliquota() {
        final WebElement tableValueGRERJ = webDriverElements.executeFindElements(webDriver, XPATH, "(//table[@class='table table-striped table-bordered table-hover'])[1]", false);
        WebElement textValue = tableValueGRERJ.findElement(By.xpath("(//tbody)[1]")).findElement(By.xpath("(//tr)[2]")).findElement(By.xpath("(//td)[3]")).findElement(By.xpath("(//div)[49]")).findElement(By.xpath("(//input[@autocomplete='off'])[1]"));
        textValue.click();
        textValue.sendKeys("1,00");
        threadSleep.get();

        final WebElement tableCalculoAliquota = webDriverElements.executeFindElements(webDriver, XPATH, "(//table[@class='table table-striped table-bordered table-hover'])[2]", false);
        WebElement textValueTotal = tableCalculoAliquota.findElement(By.xpath("(//tbody)[2]")).findElement(By.xpath("(//tr[@class='ng-star-inserted'])[1]")).findElement(By.xpath("(//td)[7]")).findElement(By.xpath("(//div)[50]")).findElement(By.xpath("(//input[@autocomplete='off'])[2]"));
        textValueTotal.click();
        textValueTotal.clear();
        textValueTotal.sendKeys("1,00");
        tableCalculoAliquota.findElement(By.xpath("(//i[@class='fa fa-calculator fa-fw'])[1]"));
        tableCalculoAliquota.click();
        threadSleep.get();

        tableCalculoAliquota.findElement(By.xpath("(//tbody)[3]")).findElement(By.xpath("(//tr)[12]")).findElement(By.xpath("(//td)[40]")).findElement(By.xpath("(//i[@class='fa fa-calculator fa-fw'])[1]")).click();
        threadSleep.get();
        webDriverElements.executeFindElements(this.webDriver, XPATH, "(//button[@type='submit'])[1]", true);

        final WebElement footer = webDriverElements.executeFindElements(this.webDriver, XPATH, "(//div[@class='modal-footer'])[3]", false);
        footer.findElement(By.xpath("(//button[@type='button'][normalize-space()='Ok'])[2]")).click();
        threadSleep.get();

        String nroGrerj = webDriver.findElement(By.id("numero-grerj")).getDomProperty("value");
        nroGrerjCollection.add(nroGrerj);
        threadSleepTransition.get();
    }

    @Test
    public void automatizarGeracaoGuiaInicial() {
        int max = Objects.nonNull(System.getenv("totalGuias")) ? Integer.valueOf(System.getenv("totalGuias")) : 2;
        for (int i = 0; i < max; i++) {
            this.processoJudicialCabecalho();
            this.processoJudicialCustas();
            this.processoJudicialCalculoAliquota();
        }
        this.webDriver.close();
        this.webDriver.quit();
    }
}