package br.com.grerj;

import br.com.grerj.automatizado.GrerjAutomatizadoTest;
import org.openqa.selenium.WebDriver;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.function.Supplier;
import java.util.logging.Logger;

public class BaseGrerjAutomatizado {
    protected WebDriver webDriver;
    protected Path mainPath = Paths.get(System.getProperty("user.dir"));
    public static Supplier<Void> threadSleep = () -> {
        try {
            Thread.sleep(Duration.ofMillis(1200));
        } catch (InterruptedException ex) {
            Logger.getLogger(GrerjAutomatizadoTest.class.getSimpleName()).severe("Erro no timeout %s".formatted(ex.getMessage()));
        }
        return null;
    };
}
