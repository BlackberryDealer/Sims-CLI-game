import java.nio.file.*;
import java.io.IOException;

public class Refactor {
    public static void main(String[] args) throws IOException {
        String baseDir = "c:/Users/leowl/OOP/Sims-CLI-game/src/simcli/";
        String[] dirs = {"entities", "world", "engine/commands"};
        for (String dir : dirs) {
            Files.walk(Paths.get(baseDir + dir))
                .filter(Files::isRegularFile)
                .filter(p -> p.toString().endsWith(".java"))
                .forEach(p -> {
                    try {
                        String content = new String(Files.readAllBytes(p));
                        content = content.replace("simcli.ui.UIManager.printMessage(", "simcli.engine.SimulationLogger.log(");
                        content = content.replace("simcli.ui.UIManager.printWarning(", "simcli.engine.SimulationLogger.logWarning(");
                        content = content.replace("simcli.ui.UIManager.displayActionAnimation(", "simcli.engine.SimulationLogger.logAnimation(");
                        content = content.replace("simcli.ui.UIManager.prompt(", "simcli.engine.SimulationLogger.prompt(");
                        
                        content = content.replace("UIManager.printMessage(", "simcli.engine.SimulationLogger.log(");
                        content = content.replace("UIManager.printWarning(", "simcli.engine.SimulationLogger.logWarning(");
                        content = content.replace("UIManager.displayActionAnimation(", "simcli.engine.SimulationLogger.logAnimation(");
                        content = content.replace("UIManager.prompt(", "simcli.engine.SimulationLogger.prompt(");
                        
                        Files.write(p, content.getBytes());
                    } catch(Exception e) { e.printStackTrace(); }
                });
        }
        System.out.println("Refactoring complete.");
    }
}
