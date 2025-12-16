package br.com.just.peticao.inicial.test;

import br.com.just.peticao.inicial.test.factory.Peticao;
import br.com.pje.model.TokenPattern;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import javax.net.ssl.SSLContext;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static br.com.jus.peticao.inicial.impl.DeserializeToObject.apply;
import static br.com.jus.peticao.inicial.impl.PeticaoSupplier.url;
import static br.com.jus.peticao.inicial.impl.SSLContextToRequest.instanceOf;
import static br.com.jus.peticao.inicial.impl.SleepActionImpl.*;

public class PeticaoIntercorrenteTest extends BaseIntegrationTest {

    @BeforeEach
    public void setUp() {
//        FirefoxOptions firefoxOptions = new FirefoxOptions();
//        firefoxOptions.addArguments("--headless=false");

        driver = new ChromeDriver();
//        driver = new FirefoxDriver();
        driver.manage().window().maximize();
        driver.manage().deleteAllCookies();
        js = (JavascriptExecutor) driver;
        vars = new HashMap<>();
        wait = new WebDriverWait(driver, Duration.ofSeconds(2));
    }

    @AfterEach
    public void tearDown() {
        driver.quit();
    }

    private void lerProcessos() {
        File file = pathProcessosSubmetidos.toFile();
        if (file.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                br.lines().forEach(line -> System.out.println(line));
            } catch (IOException e) {
                Logger.getLogger(PeticaoIntercorrenteTest.class.getSimpleName()).severe("Erro na leitura do arquivo de processos");
            }
        }
    }

    private String accessToken() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create("https://sso.stg.cloud.pje.jus.br/auth/realms/pje/protocol/openid-connect/token"))//Recuperando token
                .header("Content-Type", "application/x-www-form-urlencoded").POST(HttpRequest.BodyPublishers.ofString("grant_type=client_credentials&client_id=pje-tjrj-1g-h3&client_secret=17985570-9ca4-4553-b20f-223562dada17")).build();
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println(response.statusCode());
        String token = null;
        if (response.statusCode() == 200) {
            token = response.body().toString();
        }
        return token;
    }

    @Test
    public void testarCollection() {
        Peticao peticao1 = Peticao.instanceOf(1l, "Teste 1");
        Peticao peticao2 = Peticao.instanceOf(2l, "Teste 2");

        Collection<Peticao> peticaoCollection = Arrays.asList(peticao1, peticao2);
        final List<Long> peticoesIds = peticaoCollection.stream().map(Peticao::getId).collect(Collectors.toList());
        peticoesIds.forEach(System.out::println);
    }


    @Test
    public void protocolarPeticaoInicial() throws InterruptedException, AWTException {
        driver.get(url.get());
        sleep.actionComponent(driver, "//input[@id='username']", false, "324.547.258-78");
        sleep.actionComponent(driver, "//input[@id='password']", false, "Megatirador65#");
        clickSleep3s.apply(driver, "//input[@id='kc-login']");

        definirProcessosPeticaoIntercorrentes();
        selecionarArquivosPeticao();
        submeterPeticao();
    }

    private void definirProcessosPeticaoIntercorrentes() throws InterruptedException {
        threadSleep_2000_ms.get();
        pularButtonAction.apply(driver);

        clickSleep3s.apply(driver, "//mat-icon[normalize-space()='menu']");
        clickSleep3s.apply(driver, "(//span[@class='mat-list-item-content'])[6]");
        pularButtonAction.apply(driver);

        sleep.actionComponent(driver, "mat-input-1", true, "0800102-27.2019.8.19.0031");
        clickSleep3s.apply(driver, "//button[@class='mat-focus-indicator ml-3 mat-raised-button mat-button-base mat-primary']");
        threadSleep_ms.apply(3000);

        clickSleep3s.apply(driver, "//button[@id='botao-acao']");
        alternarAbas();
    }

    private void selecionarArquivosPeticao() throws AWTException, InterruptedException {
        threadSleep_2000_ms.get();

        JavascriptExecutor js = (JavascriptExecutor) driver;
        String currentUrl = (String) js.executeScript("return window.location.href;");
        System.out.println(currentUrl);

        WebElement fileInputPdfA = sleep.actionComponent(driver,"//input[@accept='application/pdf']", false);
        fileInputPdfA.sendKeys(myFile.apply("_A.pdf").getAbsolutePath());
        threadSleep_2000_ms.get();
        sleep.actionComponent(driver, "mat-select-value-3", true).click();
        WebElement optionPeticao = new WebDriverWait(driver, Duration.ofSeconds(5)).until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//span[@class='mat-option-text'][normalize-space()='Petição (57)']")));
        optionPeticao.click();
        clickSleep3s.apply(driver, "//mat-icon[@class='mat-icon notranslate material-icons mat-ligature-font mat-icon-no-color']");
        sleep.actionComponent(driver, "mat-input-1", true, "PeticaoInicialAutomatizado%s".formatted(peticaoIndex.getAndSet(peticaoIndex.get() + 1)));

        WebElement fileInputAnexo = sleep.actionComponent(driver,"//input[@type='file' and @multiple]", false);
        fileInputAnexo.sendKeys(myFile.apply("protocolo").getAbsolutePath());
        threadSleep_2000_ms.get();
        sleep.actionComponent(driver,"mat-select-value-5",true).click();
        WebElement optionOutrosDocumentos = new WebDriverWait(driver, Duration.ofSeconds(5)).until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//span[normalize-space()='Outros documentos (80)']")));
        optionOutrosDocumentos.click();
        clickSleep3s.apply(driver, "(//mat-icon[@role='img'][normalize-space()='close'])[2]");
        sleep.actionComponent(driver, "mat-input-2", true, "OutrosDocumentos-PeticaoInicialAutomatizado%s".formatted(peticaoIndex.get()));
    }

    private void submeterPeticao() throws InterruptedException {
        clickSleep3s.apply(driver, "//label[@for='formly_3_radio_semGreRjAssociada_0_1-input']//span[@class='mat-radio-outer-circle']");
        clickSleep3s.apply(driver, "(//span[normalize-space()='Salvar Rascunho'])[1]");
        clickSleep3s.apply(driver, "(//span[normalize-space()='Fechar'])[1]");
        clickSleep3s.apply(driver, "(//span[normalize-space()='Protocolar'])[1]");
        clickSleep3s.apply(driver, "(//span[normalize-space()='Sim'])[1]");
        clickSleep3s.apply(driver, "(//span[normalize-space()='Ir para minhas petições'])[1]");
        threadSleep_ms.apply(5000);
    }

    @Test
    public void validarNroProcessos() throws IOException, InterruptedException, NoSuchAlgorithmException, KeyManagementException {
        TokenPattern tokenPattern = apply.deserializeJson(accessToken());
        SSLContext sslContext = instanceOf.createInstanceProtocol("TLS");


        HttpRequest getProcesso = HttpRequest.newBuilder(URI.create("https://portal-interno-api-tribunais.stg.pdpj.jus.br/api/v1/oficios/processo/08158361120248190203")).header("Authorization", "Bearer " + tokenPattern.getAccess_token()).header("Accept", "application/json").header("Content-Type", "application/json")

                // 🔥 Mandatory PDPJ headers
                .header("id-orgao-julgador", "191447").header("id-orgao-lotado", "191447").header("jtr-tribunal", "819").header("X-PDPJ-CPF-USUARIO-OPERADOR", "32454725878")

                .header("X-PDPJ-TOKEN-USUARIO-OPERADOR", tokenPattern.getAccess_token()).header("X-PDPJ-ORIGEM-SISTEMA", "PORTAL_TRIBUNAIS").header("jtr-sistema", "PDPJ")

                // Optional but matches browser
                .header("Origin", "https://portal-interno-tribunais.stg.pdpj.jus.br").header("Referer", "https://portal-interno-tribunais.stg.pdpj.jus.br/").GET().build();
        HttpClient processoClient = HttpClient.newBuilder().sslContext(sslContext).connectTimeout(Duration.ofSeconds(10)).build();
        HttpResponse processoResponse = processoClient.send(getProcesso, HttpResponse.BodyHandlers.ofString());
        System.out.println(processoResponse.statusCode());
        System.out.println(processoResponse.body().toString());

//        lerProcessos();
    }

}
