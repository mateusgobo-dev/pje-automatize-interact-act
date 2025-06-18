package br.com.peticoes.intercorrente;

import br.com.peticoes.BaseTestPeticao;
import br.com.peticoes.PeticaoFunction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.http.ClientConfig;

import java.util.logging.Logger;

import static br.com.peticoes.PeticaoFunction.*;

public class PeticaoIntercorrenteTest extends BaseTestPeticao {

    @BeforeEach
    public void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.setBinary("/usr/bin/firefox");

        this.driver = new FirefoxDriver(options);
        this.driver.manage().window().maximize();
        this.driver.manage().deleteAllCookies();
    }

    @Test
    public void criarPeticaoIntercorrenteComGuiaInvalida() {
        driver.get(url);
        this.autenticar(driver);
        this.novaPeticao(driver);
        this.buscarProcessoParaPeticaoIntercorrente(driver);
        sleepTimerApplication.get();
    }
}
