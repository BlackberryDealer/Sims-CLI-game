import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FixTests {
    public static void main(String[] args) throws IOException {
        Files.walk(Paths.get("c:/Users/tdmca/OneDrive/Desktop/OOP/Sims-CLI-game/test"))
            .filter(Files::isRegularFile)
            .filter(p -> p.toString().endsWith(".java"))
            .forEach(p -> {
                try {
                    String content = new String(Files.readAllBytes(p));
                    if (content.contains("import simcli.entities.*;\n")) {
                        content = content.replace("import simcli.entities.*;\n", 
                                "import simcli.entities.actors.*;\n" +
                                "import simcli.entities.components.*;\n" +
                                "import simcli.entities.items.*;\n" +
                                "import simcli.needs.*;\n");
                        Files.write(p, content.getBytes());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
    }
}
