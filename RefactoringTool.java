import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

public class RefactoringTool {
    
    private static final Map<String, String> CLASS_MOVES = new HashMap<>();
    
    static {
        // Utils -> Persistence
        CLASS_MOVES.put("simcli.utils.SaveManager", "simcli.persistence.SaveManager");
        
        // Entities -> Needs
        CLASS_MOVES.put("simcli.entities.NeedsTracker", "simcli.needs.NeedsTracker");
        
        // Entities -> Items
        CLASS_MOVES.put("simcli.entities.Item", "simcli.entities.items.Item");
        CLASS_MOVES.put("simcli.entities.Consumable", "simcli.entities.items.Consumable");
        CLASS_MOVES.put("simcli.entities.Food", "simcli.entities.items.Food");
        CLASS_MOVES.put("simcli.entities.Furniture", "simcli.entities.items.Furniture");
        
        // Entities -> Components
        CLASS_MOVES.put("simcli.entities.SkillManager", "simcli.entities.components.SkillManager");
        CLASS_MOVES.put("simcli.entities.SkillType", "simcli.entities.components.SkillType");
        CLASS_MOVES.put("simcli.entities.InventoryManager", "simcli.entities.components.InventoryManager");
        CLASS_MOVES.put("simcli.entities.CareerProfile", "simcli.entities.components.CareerProfile");
        
        // Entities -> Actors
        CLASS_MOVES.put("simcli.entities.Sim", "simcli.entities.actors.Sim");
        CLASS_MOVES.put("simcli.entities.NPCSim", "simcli.entities.actors.NPCSim");
        CLASS_MOVES.put("simcli.entities.Job", "simcli.entities.actors.Job");
        CLASS_MOVES.put("simcli.entities.Gender", "simcli.entities.actors.Gender");
        CLASS_MOVES.put("simcli.entities.Trait", "simcli.entities.actors.Trait");
        CLASS_MOVES.put("simcli.entities.SimState", "simcli.entities.actors.SimState");
        CLASS_MOVES.put("simcli.entities.ActionState", "simcli.entities.actors.ActionState");
        CLASS_MOVES.put("simcli.entities.ISimBehaviour", "simcli.entities.actors.ISimBehaviour");
    }

    public static void main(String[] args) throws IOException {
        String projectRoot = "c:/Users/tdmca/OneDrive/Desktop/OOP/Sims-CLI-game/";
        
        // Step 1: Move specific classes
        for (Map.Entry<String, String> entry : CLASS_MOVES.entrySet()) {
            String oldClass = entry.getKey();
            String newClass = entry.getValue();
            
            String oldPathStr = projectRoot + "src/" + oldClass.replace(".", "/") + ".java";
            String newPathStr = projectRoot + "src/" + newClass.replace(".", "/") + ".java";
            
            File oldFile = new File(oldPathStr);
            if (oldFile.exists()) {
                File newFile = new File(newPathStr);
                newFile.getParentFile().mkdirs();
                
                System.out.println("Moving " + oldFile.getName() + " to " + newFile.getParent());
                Files.move(oldFile.toPath(), newFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                
                // Update package declaration in the moved file
                String content = new String(Files.readAllBytes(newFile.toPath()));
                String oldPackage = oldClass.substring(0, oldClass.lastIndexOf("."));
                String newPackage = newClass.substring(0, newClass.lastIndexOf("."));
                content = content.replace("package " + oldPackage + ";", "package " + newPackage + ";");
                Files.write(newFile.toPath(), content.getBytes());
            }
        }
        
        // Step 2: Handle LifecycleDemo.java move from src to test
        File demoOld = new File(projectRoot + "src/simcli/entities/lifecycle/LifecycleDemo.java");
        File demoNew = new File(projectRoot + "test/simcli/entities/lifecycle/LifecycleDemo.java");
        if (demoOld.exists()) {
            demoNew.getParentFile().mkdirs();
            System.out.println("Moving LifecycleDemo to test folder...");
            Files.move(demoOld.toPath(), demoNew.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }

        // Step 3: Update imports and fully qualified names globally
        Files.walk(Paths.get(projectRoot + "src"))
            .filter(Files::isRegularFile)
            .filter(p -> p.toString().endsWith(".java"))
            .forEach(RefactoringTool::processFile);
            
        Files.walk(Paths.get(projectRoot + "test"))
            .filter(Files::isRegularFile)
            .filter(p -> p.toString().endsWith(".java"))
            .forEach(RefactoringTool::processFile);
            
        System.out.println("Refactoring complete.");
    }

    private static void processFile(Path filePath) {
        try {
            String content = new String(Files.readAllBytes(filePath));
            boolean changed = false;
            
            for (Map.Entry<String, String> entry : CLASS_MOVES.entrySet()) {
                String oldClass = entry.getKey();
                String newClass = entry.getValue();
                
                // Replace imports
                if (content.contains("import " + oldClass + ";")) {
                    content = content.replace("import " + oldClass + ";", "import " + newClass + ";");
                    changed = true;
                }
                
                // Replace FQNs in the code e.g. @see simcli.entities.Sim or simcli.entities.SimState.DEAD
                if (content.contains(oldClass)) {
                    // Avoid matching substrings by adding a check or just replacing FQNs.
                    // For safety, just replace the fully qualified name since there's no substring conflict here.
                    content = content.replace(oldClass, newClass);
                    changed = true;
                }
            }
            
            if (changed) {
                Files.write(filePath, content.getBytes());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
