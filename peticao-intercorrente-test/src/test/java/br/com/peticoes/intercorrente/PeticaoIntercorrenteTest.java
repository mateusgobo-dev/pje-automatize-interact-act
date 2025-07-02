package br.com.peticoes.intercorrente;

import br.com.peticoes.BaseTestPeticao;
import br.com.peticoes.model.Processo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.chrome.ChromeDriver;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.Duration;

import static br.com.peticoes.PeticaoFunction.sleepTimerApplication;

public class PeticaoIntercorrenteTest extends BaseTestPeticao {

    @BeforeEach
    public void setUp() {
        this.driver = new ChromeDriver();
        this.driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(2));
        this.driver.manage().window().maximize();
    }

    @Test
    public void criarPeticaoIntercorrenteComGuiaInvalida() {
        driver.get(url);
        this.autenticar(driver);
        this.novaPeticao(driver);

        String urlProcessoService = "https://portalexterno-tribunais.stg.pdpj.jus.br/api/v2/processos/" + processosSupplier.get().get(0);
        driver.get(urlProcessoService);


        this.buscarProcessoParaPeticaoIntercorrente(driver);

        //Waiting 20 seconds for next view
        this.selecionarArquivoPrincipal(driver);
        this.selecionarAnexo(driver);
        sleepTimerApplication.get();
    }

    @Test
    public void buscarProcessoParaPeticaoIntercorrente() throws Exception {
        String urlProcessoService = "https://portalexterno-tribunais.stg.pdpj.jus.br/api/v2/processos/" + processosSupplier.get().get(0);
        this.applyRequestTrustLayer();

        // 4. Build HttpClient with our SSLContext and parameters
        HttpClient client = HttpClient.newBuilder()
                .sslContext(sslContext)
                .sslParameters(sslParams)
                .build();

        // 5. Prepare and send request
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(urlProcessoService))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("Status code: " + response.statusCode());

        // 4. Deserialize JSON to POJO
        ObjectMapper mapper = new ObjectMapper();
        Processo processo = mapper.readValue(response.body(), Processo.class);

        // 5. Print result
        System.out.println("Total processos: " + processo.getTotal());
        processo.getContent().forEach(c ->
                System.out.println("Processo: " + c.getNumeroProcesso())
        );
    }

    @Test
    public void authenticate() throws Exception {
        // Replace these with your real values
        final String TOKEN_URL = "https://sso.stg.cloud.pje.jus.br/auth/realms/pje/login-actions/authenticate?execution=a86b29e8-20bb-46f2-9c54-017559576112&client_id=portalexterno-frontend&tab_id=KyvMkN4DqWU";
        final String CLIENT_ID = "portalexterno-frontend";
        final String USERNAME = "32454725878";
        final String PASSWORD = "megatirador65#";
        this.applyRequestTrustLayer();

        CookieManager cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);

        var client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .cookieHandler(cookieManager)
                .sslContext(sslContext)
                .sslParameters(sslParams)
                .build();

        // 1) Request an OpenID Connect token using password grant
        String form = new StringBuilder()
                .append("grant_type=authorization_code")
                .append("&username=").append(USERNAME)
                .append("&password=").append(PASSWORD)
                .toString();

        URI uri = URI.create(TOKEN_URL);
        CookieStore cookieStore = cookieManager.getCookieStore();
        HttpCookie cookie = new HttpCookie("policy", "accepted");
        cookie.setDomain("sso.stg.cloud.pje.jus.br");
        cookie.setPath("/");
        cookieStore.add(uri, cookie);

        var tokenRequest = HttpRequest.newBuilder()
                .uri(uri)
                .timeout(Duration.ofSeconds(60))
                .header("Content-Type", "text/html;charset=utf-8")
                .POST(HttpRequest.BodyPublishers.ofString(form))
                .build();

        HttpResponse<String> tokenResponse = client.send(tokenRequest,
                HttpResponse.BodyHandlers.ofString());

        if (tokenResponse.statusCode() != 200) {
            throw new RuntimeException("Token request failed: " +
                    tokenResponse.statusCode() + " → " + tokenResponse.body());
        }

        // 2) Parse out the access_token (and ID token if you need it)
        ObjectMapper mapper = new ObjectMapper();
        JsonNode json = mapper.readTree(tokenResponse.body());
        String accessToken = json.get("access_token").asText();
        String idToken = json.has("id_token") ? json.get("id_token").asText() : null;

        System.out.println("Access Token: " + accessToken);
        if (idToken != null) System.out.println("ID Token: " + idToken);

        // 3) Call your protected API with the Bearer token
        var apiRequest = HttpRequest.newBuilder()
                .uri(URI.create("https://portalexterno-tribunais.stg.pdpj.jus.br/api/v2/processos/" + processosSupplier.get().get(0)))
                .timeout(Duration.ofSeconds(10))
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + accessToken)
                .GET()
                .build();

        HttpResponse<String> apiResponse = client.send(apiRequest,
                HttpResponse.BodyHandlers.ofString());

        if (apiResponse.statusCode() == 200) {
            System.out.println("API Response: " + apiResponse.body());
        } else {
            System.err.println("API Error: " +
                    apiResponse.statusCode() + " → " + apiResponse.body());
        }
    }
}
