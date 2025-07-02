package br.com.grerj.automatizado;

import br.com.grerj.BaseGrerjAutomatizado;
import br.com.grerj.enumerators.ElementBy;
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
import java.util.logging.Logger;

import static br.com.grerj.automatizado.WebDriverFindElements.*;
import static br.com.grerj.enumerators.ElementBy.*;

public class GrerjAutomatizadoTest extends BaseGrerjAutomatizado {

    @BeforeEach
    public void beforeEach() {
        this.webDriver = new ChromeDriver();
        this.webDriver.manage().window().maximize();
    }

    private void processoJudicialCabecalho(){
        webDriverElements.executeFindElements(webDriver, URL, "https://wwwh3.tjrj.jus.br/hgrerjweb/#/judicial/processo-judicial",false);
        webDriverElements.executeFindElements(webDriver, ID, "grerjInicial",true);
        webDriverElements.executeFindElements(webDriver, XPATH, "//div[@id='modal-alerta']//button[@type='button'][normalize-space()='Ok']",false);
        webDriverElements.executeFindElements(webDriver, XPATH, "(//select[@id='comarca'])[1]",false);
        webDriverElements.executeFindElements(webDriver, XPATH, "(//option[@value='1: Object'])[1]",false);
        webDriverElements.executeFindElements(webDriver, TEXTFIELD, "cpfCnpjRecolhedor","32454725878");
        webDriverElements.executeFindElements(webDriver, TEXTFIELD, "nomeResponsavelRecolhimento","MATEUS EDUARDO GOBO");
    }

    private void processoJudicialCustas(){

    }

    @Test
    public void automatizarGeracaoGuia() {
        this.processoJudicialCabecalho();
        final List<String> nroGrerjCollection = new ArrayList<>();
        webDriver.findElement(By.xpath("(//button[@data-original-title='Pesquisar Órgão'])[1]")).click();
        threadSleep.get();
        final WebElement element = webDriver.findElement(By.xpath("(//div[@class='form-group col-md-12'])[1]"));
        final WebElement select = element.findElement(By.id("comarca"));
        select.click();
        Select selectOption = new Select(select);
        List<WebElement> options = selectOption.getOptions();
        options.stream().filter(option -> option.getText().equalsIgnoreCase("atos processuais")).findFirst().get().click();
        threadSleep.get();
        element.findElement(By.xpath("(//div[@class='col-md-12'][normalize-space()='ARREMATAÇÃO'])[1]")).click();

        threadSleep.get();
        webDriver.findElement(By.xpath("(//button[@type='submit'])[1]")).click();
        threadSleep.get();
        webDriver.findElement(By.xpath("(//button[@type='button'][normalize-space()='Ok'])[2]")).click();
        threadSleep.get();
        webDriver.findElement(By.xpath("(//button[@type='button'][normalize-space()='Ok'])[2]")).click();
        threadSleep.get();

        final WebElement table1 = webDriver.findElement(By.xpath("(//table[@class='table table-striped table-bordered table-hover'])[1]"));
        WebElement textValue = table1.findElement(By.xpath("(//tbody)[1]")).findElement(By.xpath("(//tr)[2]"))
                .findElement(By.xpath("(//td)[3]"))
                .findElement(By.xpath("(//div)[49]"))
                .findElement(By.xpath("(//input[@autocomplete='off'])[1]"));
        textValue.click();
        textValue.sendKeys("1,00");
        threadSleep.get();

        final WebElement table2 = webDriver.findElement(By.xpath("(//table[@class='table table-striped table-bordered table-hover'])[2]"));
        WebElement textValueTotal = table2.findElement(By.xpath("(//tbody)[2]")).findElement(By.xpath("(//tr[@class='ng-star-inserted'])[1]"))
                .findElement(By.xpath("(//td)[7]"))
                .findElement(By.xpath("(//div)[50]")).findElement(By.xpath("(//input[@autocomplete='off'])[2]"));
        textValueTotal.click();
        textValueTotal.clear();
        textValueTotal.sendKeys("1,00");
        table2.findElement(By.xpath("(//i[@class='fa fa-calculator fa-fw'])[1]"));
        table2.click();
        threadSleep.get();

        table2.findElement(By.xpath("(//tbody)[3]")).findElement(By.xpath("(//tr)[12]"))
                .findElement(By.xpath("(//td)[40]"))
                .findElement(By.xpath("(//i[@class='fa fa-calculator fa-fw'])[1]"))
                .click();
        threadSleep.get();

        webDriver.findElement(By.xpath("(//button[@type='submit'])[1]")).click();
        threadSleep.get();

        final WebElement footer = webDriver.findElement(By.xpath("(//div[@class='modal-footer'])[3]"));
        threadSleep.get();
        footer.findElement(By.xpath("(//button[@type='button'][normalize-space()='Ok'])[2]")).click();

        String nroGrerj = webDriver.findElement(By.id("numero-grerj")).getDomProperty("value");
        nroGrerjCollection.add(nroGrerj);

        System.out.println(nroGrerj);

        try {
            Path path = new File("%s\\src\\test\\resources\\test-resources-%s.txt".formatted(mainPath.toString(), System.currentTimeMillis())).toPath();
            Path file = Path.of(path.toUri());
            if (Files.notExists(file)) Files.createFile(file);
            StringBuilder values = new StringBuilder();
            nroGrerjCollection.stream().forEach(value -> values.append(value));
            Files.write(file, values.toString().getBytes(StandardCharsets.UTF_8));
        } catch (IOException ex) {
            Logger.getLogger(GrerjAutomatizadoTest.class.getSimpleName(), ex.getMessage());
        }
    }
}