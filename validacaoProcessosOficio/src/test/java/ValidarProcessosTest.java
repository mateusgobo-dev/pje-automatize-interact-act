import br.com.pje.model.TokenPattern;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class ValidarProcessosTest {

    private WebDriver driver;
    private WebDriverWait wait;
    private Map<String, Object> vars;
    JavascriptExecutor js;

    private Path path = Path.of(System.getProperty("user.dir")).resolve(Paths.get("src", "test", "resources", "processos_submetidos"));

    @BeforeEach
    public void setUp() {
        driver = new ChromeDriver();
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
        if(file.exists()) {
            try(BufferedReader br = new BufferedReader(new FileReader(file))) {
                br.lines().forEach(line -> System.out.println(line));
            }catch (IOException e) {
                Logger.getLogger(ValidarProcessosTest.class.getSimpleName()).severe("Erro na leitura do arquivo de processos");
            }
        }
    }

    @Test
    public void validarNroProcessos() throws IOException, InterruptedException, NoSuchAlgorithmException, KeyManagementException {
        HttpRequest request = HttpRequest.newBuilder(URI.create("https://sso.stg.cloud.pje.jus.br/auth/realms/pje/protocol/openid-connect/token"))//Recuperando token
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString("grant_type=client_credentials&client_id=pje-tjrj-1g-h3&client_secret=17985570-9ca4-4553-b20f-223562dada17"))
                .build();
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println(response.statusCode());
        String token = null;
        TokenPattern tokenPattern = null;
        if (response.statusCode() == 200) {
             token = response.body().toString();
             ObjectMapper mapper = new ObjectMapper();
             mapper.configure(JsonParser.Feature.IGNORE_UNDEFINED, true);
             mapper.disable(SerializationFeature.FAIL_ON_UNWRAPPED_TYPE_IDENTIFIERS);
             tokenPattern = mapper.readValue(token, TokenPattern.class);
            System.out.println(tokenPattern);
        }

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, new TrustManager[]{
                new X509TrustManager() {
                    public void checkClientTrusted(java.security.cert.X509Certificate[] xcs, String s) {}
                    public void checkServerTrusted(java.security.cert.X509Certificate[] xcs, String s) {}
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() { return new java.security.cert.X509Certificate[0]; }
                }
        }, new SecureRandom());

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
        System.out.println(response.body().toString());

//        lerProcessos();
    }

}
