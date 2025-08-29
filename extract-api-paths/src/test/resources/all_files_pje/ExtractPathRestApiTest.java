package br;

import org.junit.Test;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.nio.file.StandardOpenOption.*;

public class ExtractPathRestApiTest {
    private static final Path path = Paths.get(System.getProperty("user.dir"),"src", "test", "resources", "rest_services");

    @Test
    public void listFilesTest()  throws IOException {
        final Map<String, List<String>> valuesOf = new HashMap<>();
        final List<String> paths = new ArrayList<>();
        Files.list(path).forEach(file -> {
            try {
                String fileName      = file.getFileName().toString();
                List<String> content = Files.readAllLines(file, Charset.defaultCharset());
                content.stream().filter(line -> !line.isEmpty()).forEach(line -> {
                    Pattern pattern = Pattern.compile("@Path\\s*\\(\\s*\"([^\"]+)\"\\s*\\)");
                    Matcher matcher = pattern.matcher(line);
                    if (matcher.find()) {
                        String name = matcher.group(1);
                        paths.add(name);
                        paths.add(System.lineSeparator());
                    }
                });
                paths.add("-----------------------------------------------------------");
                valuesOf.put(fileName, new ArrayList<>(paths));
                paths.clear();
            }catch (IOException ex){
                Logger.getLogger(ExtractPathRestApiTest.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        StringBuilder lines = new StringBuilder();
        valuesOf.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(values -> {
            lines.append(values.getKey());
            lines.append(System.lineSeparator());
            values.getValue().stream().filter(line -> !line.isEmpty()).forEach(line -> lines.append(line));
            lines.append(System.lineSeparator());
        });
        File result = new File(path.toFile()+File.separator+"pje_services.txt");
        if(result.exists()){Files.delete(Paths.get(result.toURI()));}
        try(FileWriter fileWriter = new FileWriter(result)) {
            fileWriter.write(lines.toString());
        }catch(IOException ex){
            Logger.getLogger(ExtractPathRestApiTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
