package br.com.just.peticao.inicial.test;

import br.com.pje.model.TokenPattern;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import javax.net.ssl.SSLContext;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
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
import java.util.HashMap;
import java.util.logging.Logger;

import static br.com.jus.peticao.inicial.impl.DeserializeToObject.apply;
import static br.com.jus.peticao.inicial.impl.PeticaoSupplier.url;
import static br.com.jus.peticao.inicial.impl.SSLContextToRequest.instanceOf;
import static br.com.jus.peticao.inicial.impl.SleepActionImpl.*;

public class ValidarProcessosTest extends BaseIntegrationTest {

    @BeforeEach
    public void setUp() {
        driver = new FirefoxDriver();
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
        File file = path.toFile();
        if (file.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                br.lines().forEach(line -> System.out.println(line));
            } catch (IOException e) {
                Logger.getLogger(ValidarProcessosTest.class.getSimpleName()).severe("Erro na leitura do arquivo de processos");
            }
        }
    }

    private String accessToken() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create("https://sso.stg.cloud.pje.jus.br/auth/realms/pje/protocol/openid-connect/token"))//Recuperando token
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString("grant_type=client_credentials&client_id=pje-tjrj-1g-h3&client_secret=17985570-9ca4-4553-b20f-223562dada17"))
                .build();
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
    public void protocolarPeticaoInicial() throws InterruptedException, AWTException {
        driver.get(url.get());
        sleep.actionComponent(driver, "//input[@id='username']", false, "324.547.258-78");
        sleep.actionComponent(driver, "//input[@id='password']", false, "Megatirador65#");
        click.apply(driver, "//input[@id='kc-login']");

        definirProcessosPeticaoIntercorrentes();
        selecionarArquivosPeticao();
    }

    private void definirProcessosPeticaoIntercorrentes() throws InterruptedException {
        threadSleep_2000_ms.get();
        pularButtonAction.apply(driver);

        threadSleep_2000_ms.get();
        click.apply(driver, "//mat-icon[normalize-space()='menu']");

        threadSleep_2000_ms.get();
        click.apply(driver, "(//span[@class='mat-list-item-content'])[6]");

        threadSleep_2000_ms.get();
        pularButtonAction.apply(driver);

        sleep.actionComponent(driver, "mat-input-1", true, "0800102-27.2019.8.19.0031");
        click.apply(driver, "//button[@class='mat-focus-indicator ml-3 mat-raised-button mat-button-base mat-primary']");
        threadSleep_ms.apply(10000);

        click.apply(driver, "//button[@id='botao-acao']");
        threadSleep_ms.apply(10000);
    }

    private void selecionarArquivosPeticao() throws AWTException {
        click.apply(driver, "(//button[@type='button'])[2]");

        // File path to upload
        String filePath = "C:\\Users\\mateu.gobo\\JUS-BR\\PETICOES\\PETICAO_INICIAL_A.pdf";

        // Copy to clipboard
        StringSelection s = new StringSelection(filePath);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(s, null);

        // Paste and press Enter
        Robot robot = new Robot();
        robot.delay(1000);
        robot.keyPress(KeyEvent.VK_CONTROL);
        robot.keyPress(KeyEvent.VK_V);
        robot.keyRelease(KeyEvent.VK_V);
        robot.keyRelease(KeyEvent.VK_CONTROL);
        robot.keyPress(KeyEvent.VK_ENTER);
        robot.keyRelease(KeyEvent.VK_ENTER);
    }

    @Test
    public void validarNroProcessos() throws IOException, InterruptedException, NoSuchAlgorithmException, KeyManagementException {
        TokenPattern tokenPattern = apply.deserializeJson(accessToken());
        SSLContext sslContext = instanceOf.createInstanceProtocol("TLS");


        HttpRequest getProcesso = HttpRequest.newBuilder(URI.create("https://portal-interno-api-tribunais.stg.pdpj.jus.br/api/v1/oficios/processo/08158361120248190203"))
                .header("Authorization", "Bearer " + tokenPattern.getAccess_token())
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")

                // 🔥 Mandatory PDPJ headers
                .header("id-orgao-julgador", "191447")
                .header("id-orgao-lotado", "191447")
                .header("jtr-tribunal", "819")
                .header("X-PDPJ-CPF-USUARIO-OPERADOR", "32454725878")

                .header("X-PDPJ-TOKEN-USUARIO-OPERADOR", tokenPattern.getAccess_token())
                .header("X-PDPJ-ORIGEM-SISTEMA", "PORTAL_TRIBUNAIS")
                .header("jtr-sistema", "PDPJ")

                // Optional but matches browser
                .header("Origin", "https://portal-interno-tribunais.stg.pdpj.jus.br")
                .header("Referer", "https://portal-interno-tribunais.stg.pdpj.jus.br/")
                .GET()
                .build();
        HttpClient processoClient = HttpClient.newBuilder()
                .sslContext(sslContext)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        HttpResponse processoResponse = processoClient.send(getProcesso, HttpResponse.BodyHandlers.ofString());
        System.out.println(processoResponse.statusCode());
        System.out.println(processoResponse.body().toString());

//        lerProcessos();
    }

}
