import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

public class GuiaRecolhimentoTest {

    private WebDriver driver = new ChromeDriver();

    @BeforeEach
    public void setUp() {
        driver.manage().window().maximize();
        driver.manage().deleteAllCookies();
    }

    @Test
    public void testGuiaRecolhimento()  throws InterruptedException {
        driver.get("https://wwwh3.tjrj.jus.br/hgrerjweb/");
        Thread.sleep(5000);
    }

}
