package br.com.peticoes;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Logger;

import static br.com.peticoes.PeticaoFunction.*;

public class BaseTestPeticao {

    protected WebDriver driver;
    private static List<String> processos = new ArrayList<>();

    static {
        processos.add("0800001-69.2024.8.19.0045");
    }
    protected Supplier<List<String>> processosSupplier = () -> processos;

    protected String username = System.getProperty("user.name");
    protected String password = System.getProperty("user.password");
    protected String url = "https://sso.stg.cloud.pje.jus.br/auth/realms/pje/protocol/openid-connect/auth?client_id=portalexterno-frontend&redirect_uri=https%3A%2F%2Fportalexterno-tribunais.stg.pdpj.jus.br%2Fhome&state=52e38c99-790b-422c-8624-c5a340d87795&response_mode=fragment&response_type=code&scope=openid&nonce=d208e48c-b598-4047-81d8-ffd03b0f9d99";
    protected  SSLContext sslContext = null;
    protected SSLParameters sslParams;
    Function<TrustManager[], Void> sslContextInit = trustManager -> {
        try {
            this.sslContext = SSLContext.getInstance("TLS");
            this.sslContext.init(null, trustManager, new SecureRandom());
            this.sslParams = sslContext.getDefaultSSLParameters();
            this.sslParams.setEndpointIdentificationAlgorithm(null);
        }catch (NoSuchAlgorithmException ex){
        }catch (KeyManagementException ex){}
        return null;
    };
    protected void applyRequestTrustLayer(){
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                }
        };
        sslContextInit.apply(trustAllCerts);
    }

    private Supplier<Boolean> ignorarNavegacaoGuiada = () -> {
        String xPathDialog = "//body/div[@class='cdk-overlay-container']/div[@class='cdk-global-overlay-wrapper']";
        String xPathButton = "//button[@aria-label='Fechar']";

        WebElement element = findByXpath.apply(xPathDialog, driver)
                .findElement(By.xpath("//*[contains(@id, 'cdk-overlay')]"))
                .findElement(By.xpath("//div[1]"));
        boolean isDisplayed = nextElement.test(element);
        if (nextElement.test(element)) {
            Logger.getLogger("Ignorando navegacao guiada....");
            element = findByXpathClickTimeout.apply(xPathButton, driver);
        }
        return isDisplayed;
    };

    protected final WebElement autenticar(WebDriver driver) {
        driver.findElement(By.id("username")).sendKeys("32454725878");
        driver.findElement(By.id("password")).sendKeys("Megatirador65#");
        WebElement element = findByXpathClickTimeout.apply("(//input[@id='kc-login'])[1]", driver);
        return element;
    }

    protected final void novaPeticao(WebDriver driver) {
        sleepTimer.get();
        this.ignorarNavegacaoGuiada.get();
        findByXpathClickTimeout.apply("(//mat-icon[normalize-space()='menu'])[1]", driver);
        findByXpathClickTimeout.apply("(//span[@class='mat-list-item-content'])[4]", driver);
    }

    protected final void buscarProcessoParaPeticaoIntercorrente(WebDriver driver) {
        sleepTimer.get();//Aguardando 5 segundos
        this.ignorarNavegacaoGuiada.get();

        WebElement element = findByXpath.apply("//div[@class='mat-form-field-infix ng-tns-c39-10']", driver);
        if (nextElement.test(element)) {
            element = element.findElement(By.name("numeroProcesso"));
            if (nextElement.test(element)) {
                element.sendKeys(processos.get(0));
                if (nextElement.test(element)) {
                    element = findByXpathClickTimeout.apply("(//button[@class='mat-focus-indicator ml-3 mat-raised-button mat-button-base mat-primary'])[1]", driver);
                    sleepTimer.get();
                    if (nextElement.test(element)) {
//                        findByIdClick.apply("botao-acao", driver);
////                        boolean result =  wait.until(ExpectedConditions.urlContains("/peticao/processo"));
//                        boolean result = new WebDriverWait(driver, Duration.ofSeconds(10)).until(d -> {
//                            String url = d.getCurrentUrl();
//                            System.out.println(url);
//                            return url.matches(".*peticao/processo\\?.*processo=\\d{7}-\\d{2}\\.\\d{4}\\.\\d\\.\\d{2}\\.\\d{4}.*");
//                        });
//                        if(result) {
//                            WebElement processo = new WebDriverWait(driver, Duration.ofSeconds(10)).until(ExpectedConditions.presenceOfElementLocated(
//                                    By.xpath("//*[contains(text(), 'Arquivo Principal')]")
//                            ));
//                            System.out.printf(processo.getText());

                        // click button that triggers route change
                        element = driver.findElement(By.id("botao-acao"));
                        element.click();
                        JavascriptExecutor js = (JavascriptExecutor) driver;
                        WebElement shadowHost = driver.findElement(By.tagName("app-root"));
                        WebElement shadowRoot = (WebElement) js.executeScript("return arguments[0].shadowRoot", shadowHost);
                        WebElement elementInsideShadowDom = shadowRoot.findElement(By.xpath("(//button[@type='button'])[2]"));
                        System.out.println(elementInsideShadowDom.getText());
                    }
                }
            }
        }
    }

    protected final void selecionarArquivoPrincipal(WebDriver driver) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[contains(text(), 'Incluir Arquivo Principal')]")));
        System.out.println(element.getText());

//        WebElement element = findByXpath.apply("(//button[@type='button'])[2]", driver);
//        WebElement fileInput = element.findElement(By.cssSelector("/input[type='file']"));
//        ((JavascriptExecutor) driver).executeScript("arguments[0].style.display = 'block';", fileInput);// Optionally unhide it if needed

        /**WebElement element = findByXpath.apply("(//button[@type='button'])[2]", driver);
         if(nextElement.test(element)){
         fileInput.sendKeys("C:\\Users\\mateu.gobo\\JUS-BR\\PETICOES\\PETICAO_INICIAL_A.pdf");// Send file path
         element.click();
         }**/
    }

    protected final void selecionarAnexo(WebDriver driver) {
        WebElement element = findByXpathClickTimeout.apply("(//button[@type='button'])[3]", driver);
        element.click();
        element.sendKeys("C:\\Users\\mateu.gobo\\JUS-BR\\PETICOES\\protocoloPeticao_20254000000003513.pdf");
    }
}
