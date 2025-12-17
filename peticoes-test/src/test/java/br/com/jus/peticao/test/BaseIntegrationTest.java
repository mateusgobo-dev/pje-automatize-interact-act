package br.com.jus.peticao.test;

import br.com.jus.peticao.inicial.PeticaoInicialCaracteristicasTest;
import br.com.jus.peticao.inicial.PeticaoInicialDadosIniciaisTest;
import org.junit.jupiter.api.AfterEach;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static br.com.jus.peticao.inicial.impl.PeticaoSupplier.urlPortalExterno;
import static br.com.jus.peticao.inicial.impl.SleepActionImpl.clickSleep3s;
import static br.com.jus.peticao.inicial.impl.SleepActionImpl.sleep;

public class BaseIntegrationTest {
    protected WebDriver driver;
    protected WebDriverWait wait;
    protected Map<String, Object> vars;
    protected JavascriptExecutor js;
    protected Path pathProcessosSubmetidos = Path.of(System.getProperty("user.dir")).resolve(Paths.get("src", "test", "resources", "processos_submetidos"));
    protected Path pathDocumentos = Path.of(System.getProperty("user.dir")).resolve(Paths.get("src", "test", "resources", "documentos" ));
    protected final AtomicReference<Integer> peticaoIndex = new AtomicReference<>(0);
    protected PeticaoInicialDadosIniciaisTest peticaoInicialDadosIniciaisTest;
    protected PeticaoInicialCaracteristicasTest peticaoInicialCaracteristicasTest;

    protected void configurarChromeDriver(){
        driver = new ChromeDriver();
        configPropertiesDriver();
    }

    protected  void configurarFirefoxDriver(){
        driver = new FirefoxDriver();
        configPropertiesDriver();
    }

    private void configPropertiesDriver(){
        driver.manage().window().maximize();
        driver.manage().deleteAllCookies();
        js = (JavascriptExecutor) driver;
        vars = new HashMap<>();
        wait = new WebDriverWait(driver, Duration.ofSeconds(2));
    }

    protected void autenticarUsuario() throws InterruptedException {
        driver.get(urlPortalExterno.get());
        sleep.actionComponent(driver, "//input[@id='username']", false, "324.547.258-78");
        sleep.actionComponent(driver, "//input[@id='password']", false, "Megatirador65#");
        clickSleep3s.apply(driver, "//input[@id='kc-login']");
    }

    protected  String executarComandoJavaScript(String comando) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        String resultado = (String) js.executeScript(comando);
        return resultado;
    }

    protected Function<String, File> myFile = (prefix) -> {
        try {
             Optional<Path> filePdf = Files.list(pathDocumentos).filter(file -> file.getFileName().toString().contains(prefix)).findFirst();
             if(!filePdf.isEmpty()) return filePdf.get().toFile();
             return null;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    };

    protected  void alternarAbas(){
        String currentTab = driver.getWindowHandle();
        new WebDriverWait(driver, Duration.ofSeconds(8))// wait for new tab
                .until(d -> d.getWindowHandles().size() > 1);

        for (String tab : driver.getWindowHandles()) {// switch to the new tab
            if (!currentTab.equals(tab)) {
                driver.switchTo().window(tab);
                break;
            }
        }

        // NOW this is the new URL
        JavascriptExecutor js = (JavascriptExecutor) driver;
        String newUrl = (String) js.executeScript("return window.location.href;");
        System.out.println("New tab URL = " + newUrl + ", original = " + currentTab);
    }

    @AfterEach
    public void tearDown() {
        driver.quit();
    }
}
