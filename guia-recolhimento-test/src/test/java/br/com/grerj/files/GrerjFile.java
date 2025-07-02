package br.com.grerj.files;

import br.com.grerj.automatizado.GrerjAutomatizadoTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

public class GrerjFile {

    private final Path mainPath = Paths.get(System.getProperty("user.dir"));

    @BeforeEach
    public void beforeEach() {
        System.out.println("br.com.grerj.files.GrerjFile.beforeEach");
    }

    @Test
    public void creatingFile() {
        System.out.println("br.com.grerj.files.GrerjFile.creatingFile");
        try {
            Path path = new File("%s\\src\\test\\resources\\test-resources-%s.txt".formatted(mainPath.toString(), System.currentTimeMillis())).toPath();
            Path file = Path.of(path.toUri());
            if(Files.notExists(file))Files.createFile(file);
            StringBuilder values = new StringBuilder("123");
            Files.write(file, values.toString().getBytes(StandardCharsets.UTF_8));
        } catch (IOException ex) {
            Logger.getLogger(GrerjAutomatizadoTest.class.getSimpleName(), ex.getMessage());
        }
    }
}
