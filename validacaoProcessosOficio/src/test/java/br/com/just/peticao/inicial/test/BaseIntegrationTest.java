package br.com.just.peticao.inicial.test;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
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
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

public class BaseIntegrationTest {
    protected WebDriver driver;
    protected WebDriverWait wait;
    protected Map<String, Object> vars;
    protected JavascriptExecutor js;
    protected Path pathProcessosSubmetidos = Path.of(System.getProperty("user.dir")).resolve(Paths.get("src", "test", "resources", "processos_submetidos"));
    protected Path pathDocumentos = Path.of(System.getProperty("user.dir")).resolve(Paths.get("src", "test", "resources", "documentos" ));

    protected final AtomicReference<Integer> peticaoIndex = new AtomicReference<>(0);

    protected void autenticarUsuario() {
        driver.findElement(By.xpath("//input[@id='username']")).sendKeys("324.547.258-78");
        driver.findElement(By.xpath("//input[@id='password']")).sendKeys("Megatirador65#");
        driver.findElement(By.xpath("//input[@id='kc-login']")).click();
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

    protected void subirArquivoPeticao(File file) throws AWTException {
        // Copy path to clipboard
        StringSelection selection = new StringSelection(file.getAbsolutePath());
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);

        // Robot actions
        Robot robot = new Robot();
        robot.setAutoDelay(100);

        // CTRL + V
        robot.keyPress(KeyEvent.VK_CONTROL);
        robot.keyPress(KeyEvent.VK_V);
        robot.keyRelease(KeyEvent.VK_V);
        robot.keyRelease(KeyEvent.VK_CONTROL);

        // Small pause before ENTER
        robot.delay(500);

        // ENTER
        robot.keyPress(KeyEvent.VK_ENTER);
        robot.keyRelease(KeyEvent.VK_ENTER);
    }
}
