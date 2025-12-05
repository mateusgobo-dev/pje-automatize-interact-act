package br.com.just.peticao.inicial.test;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class BaseIntegrationTest {
    protected WebDriver driver;
    protected WebDriverWait wait;
    protected Map<String, Object> vars;
    protected JavascriptExecutor js;
    protected Path path = Path.of(System.getProperty("user.dir")).resolve(Paths.get("src", "test", "resources", "processos_submetidos"));

    protected void autenticarUsuario(){
        driver.findElement(By.xpath("//input[@id='username']")).sendKeys("324.547.258-78");
        driver.findElement(By.xpath("//input[@id='password']")).sendKeys("Megatirador65#");
        driver.findElement(By.xpath("//input[@id='kc-login']")).click();
    }
}
