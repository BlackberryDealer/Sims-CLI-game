import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

public class ReplaceFun {
    public static void main(String[] args) throws Exception {
        String baseDir = "c:/Users/dylan/Desktop/Object Oriented Programming/Sims-CLI-game";

        List<Path> allJavaFiles = new ArrayList<>();
        try (Stream<Path> stream = Files.walk(Paths.get(baseDir, "src"))) {
            allJavaFiles.addAll(stream.filter(Files::isRegularFile).filter(p -> p.toString().endsWith(".java")).collect(Collectors.toList()));
        }
        try (Stream<Path> stream = Files.walk(Paths.get(baseDir, "test"))) {
            allJavaFiles.addAll(stream.filter(Files::isRegularFile).filter(p -> p.toString().endsWith(".java")).collect(Collectors.toList()));
        }

        for (Path p : allJavaFiles) {
            String content = new String(Files.readAllBytes(p));
            String oldContent = content;

            content = content.replace("getFun()", "getHappiness()");
            content = content.replace("getFunValue()", "getHappinessValue()");
            content = content.replace("funValue", "happinessValue");
            content = content.replace("new Fun()", "new Happiness()");
            content = content.replace("Need fun;", "Need happiness;");
            content = content.replace("this.fun =", "this.happiness =");
            content = content.replace("this.fun.", "this.happiness.");
            content = content.replace("funBonus", "happinessBonus");
            content = content.replace("FUN_GAIN", "HAPPINESS_GAIN");
            content = content.replace("\"Fun\"", "\"Happiness\"");
            content = content.replace("| Fun: ", "| Happiness: ");
            content = content.replace("Fun.", "Happiness.");
            content = content.replace("Fun ", "Happiness ");
            content = content.replace("fun.", "happiness.");
            content = content.replace("sim.getFun()", "sim.getHappiness()");
            content = content.replace("sim.getFun()", "sim.getHappiness()");

            if (!oldContent.equals(content)) {
                Files.write(p, content.getBytes());
            }
        }
        
        // Delete Fun.java
        Files.deleteIfExists(Paths.get(baseDir, "src/simcli/needs/Fun.java"));

        System.out.println("REPLACED FUN WITH HAPPINESS GLOBALLY");
    }
}
