package br.com.extract.data.api;


import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExtractPathRestApiTest {
    private static final Path path = Paths.get(System.getProperty("user.dir"),"src", "test", "resources", "rest_services");
    private static final Path pathAllFiles = Paths.get(System.getProperty("user.dir"),"src", "test", "resources", "all_files_pje");
    private static final Path pathFilesResult = Paths.get(System.getProperty("user.dir"),"src", "test", "resources");

    @Test
    public void getPathFromServiceFiles()  throws IOException {
        final Map<String, List<String>> valuesOf = new HashMap<>();
        final List<String> paths = new ArrayList<>();
        Files.list(path).forEach(file -> {
            try {
                String fileName      = file.getFileName().toString();
                List<String> content = Files.readAllLines(file, StandardCharsets.ISO_8859_1);
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
        StringBuilder contracts = new StringBuilder();
        valuesOf.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(values -> {
            lines.append(values.getKey());
            lines.append(System.lineSeparator());
            values.getValue().stream().filter(line -> !line.isEmpty()).forEach(line -> lines.append(line));
            lines.append(System.lineSeparator());

            contracts.append(values.getKey());
            contracts.append(System.lineSeparator());
        });
        File result = new File(pathFilesResult.toFile()+File.separator+"pje_services.txt");
        File resultContracts = new File(pathFilesResult.toFile()+File.separator+"pje_services_contracts.txt");
        if(result.exists()){Files.delete(Paths.get(result.toURI()));}
        try(FileWriter fileWriterResult = new FileWriter(result);
              FileWriter fileWriterContracts = new FileWriter(resultContracts)) {
            fileWriterResult.write(lines.toString());
            fileWriterContracts.write(contracts.toString());
        }catch(IOException ex){
            Logger.getLogger(ExtractPathRestApiTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Test
    public void getPathsFromAllFiles()  throws IOException {
        final Map<String, List<String>> valuesOf = new HashMap<>();
        final List<String> paths = new ArrayList<>();
        Files.list(pathAllFiles).forEach(file -> {
            try {
                String fileName      = file.getFileName().toString();
                List<String> content = Files.readAllLines(file, StandardCharsets.ISO_8859_1);
                content.stream().filter(line -> !line.isEmpty()).forEach(line -> {
                    Pattern pattern = Pattern.compile("@Path\\s*\\(\\s*\"([^\"]+)\"\\s*\\)");
                    Matcher matcher = pattern.matcher(line);
                    if (matcher.find()) {
                        String name = matcher.group(1);
                        paths.add(name);
                        paths.add(System.lineSeparator());
                    }
                });
                if(!paths.isEmpty()) {
                    paths.add("-----------------------------------------------------------");
                    valuesOf.put(fileName, new ArrayList<>(paths));
                    paths.clear();
                }
            }catch (IOException ex){
                Logger.getLogger(ExtractPathRestApiTest.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        StringBuilder lines = new StringBuilder();
        StringBuilder contracts = new StringBuilder();
        valuesOf.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(values -> {
            lines.append(values.getKey());
            lines.append(System.lineSeparator());
            values.getValue().stream().filter(line -> !line.isEmpty()).forEach(line -> lines.append(line));
            lines.append(System.lineSeparator());

            contracts.append(values.getKey());
            contracts.append(System.lineSeparator());
        });
        File result = new File(pathFilesResult.toFile()+File.separator+"pje_services_all_files.txt");
        File resultContracts = new File(pathFilesResult.toFile()+File.separator+"pje_services_all_contracts.txt");
        if(result.exists()){Files.delete(Paths.get(result.toURI()));}
        try(FileWriter fileWriterResult = new FileWriter(result);
            FileWriter fileWriterContracts = new FileWriter(resultContracts)) {
            fileWriterResult.write(lines.toString());
            fileWriterContracts.write(contracts.toString());
        }catch(IOException ex){
            Logger.getLogger(ExtractPathRestApiTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        valuesOf.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(values -> {
            lines.append(values.getKey());
            lines.append(System.lineSeparator());
            values.getValue().stream().filter(line -> !line.isEmpty()).forEach(line -> lines.append(line));
            lines.append(System.lineSeparator());
        });
    }
}
