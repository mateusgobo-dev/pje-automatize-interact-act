package br.com.carta.precatoria.ordem;

import br.com.carta.precatoria.BaseTestCartaPrecatoria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import java.util.ArrayList;
import java.util.List;

public class CartaPrecatoriaTest extends BaseTestCartaPrecatoria {

    private WebDriver driver = new ChromeDriver();
    private static List<String> processos = new ArrayList<>();
    static {
        processos.add("0800001-69.2024.8.19.0045");
    }

    private String username = System.getProperty("user.name");
    private String password = System.getProperty("user.password");
    private String url = "https://sso.stg.cloud.pje.jus.br/auth/realms/pje/protocol/openid-connect/auth?client_id=portal-servicos-frontend&redirect_uri=https%3A%2F%2Fportal-interno-tribunais.stg.pdpj.jus.br%2F&state=20569176-ea56-462a-84ef-0e454a8d36da&response_mode=fragment&response_type=code&scope=openid&nonce=a9ea49d6-9679-4cab-8426-e6293374027a";

    @BeforeEach
    public void beforeEach() {
        driver.manage().window().maximize();
        driver.manage().deleteAllCookies();
    }

    @Test
    public void criarCartaPrecatoria()  throws InterruptedException {
        driver.get(url);
        this.autenticar(driver);
        this.selecionarComarca(driver);
        this.novaCartaPrecatoriaOrdem(driver);
        this.selecionarProcesso(driver,  processos.get(0));
        this.selecionarRamoJustica(driver);
        this.selecionarTribunal(driver);
        this.selecionarInstancia(driver);
        this.selecionarComarcaZonaSecao(driver);
        Thread.sleep(10000l);
    }
}
