import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

public class Refactor {
    public static void main(String[] args) throws Exception {
        String baseDir = "c:/Users/dylan/Desktop/Object Oriented Programming/Sims-CLI-game";
        
        Files.createDirectories(Paths.get(baseDir, "src/simcli/entities/models"));
        Files.createDirectories(Paths.get(baseDir, "src/simcli/entities/managers"));
        
        moveFile(baseDir, "src/simcli/entities/actors/ActionState.java", "src/simcli/entities/models/ActionState.java");
        moveFile(baseDir, "src/simcli/entities/actors/Gender.java", "src/simcli/entities/models/Gender.java");
        moveFile(baseDir, "src/simcli/entities/actors/Job.java", "src/simcli/entities/models/Job.java");
        moveFile(baseDir, "src/simcli/entities/actors/Relationship.java", "src/simcli/entities/models/Relationship.java");
        moveFile(baseDir, "src/simcli/entities/actors/RelationshipStatus.java", "src/simcli/entities/models/RelationshipStatus.java");
        moveFile(baseDir, "src/simcli/entities/actors/SimState.java", "src/simcli/entities/models/SimState.java");
        moveFile(baseDir, "src/simcli/entities/actors/Trait.java", "src/simcli/entities/models/Trait.java");

        moveFile(baseDir, "src/simcli/entities/components/SkillType.java", "src/simcli/entities/models/SkillType.java");

        moveFile(baseDir, "src/simcli/entities/components/InventoryManager.java", "src/simcli/entities/managers/InventoryManager.java");
        moveFile(baseDir, "src/simcli/entities/components/RelationshipManager.java", "src/simcli/entities/managers/RelationshipManager.java");
        moveFile(baseDir, "src/simcli/entities/components/SkillManager.java", "src/simcli/entities/managers/SkillManager.java");
        moveFile(baseDir, "src/simcli/entities/components/CareerProfile.java", "src/simcli/entities/managers/CareerManager.java");

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
            
            // Wildcard imports
            content = content.replace("import simcli.entities.actors.*;", "import simcli.entities.actors.*;\nimport simcli.entities.models.*;");
            content = content.replace("import simcli.entities.components.*;", "import simcli.entities.managers.*;\nimport simcli.entities.models.*;");

            // Fully Qualified Replacements
            content = content.replace("simcli.entities.actors.ActionState", "simcli.entities.models.ActionState");
            content = content.replace("simcli.entities.actors.Gender", "simcli.entities.models.Gender");
            content = content.replace("simcli.entities.actors.Job", "simcli.entities.models.Job");
            content = content.replace("simcli.entities.actors.Relationship", "simcli.entities.models.Relationship");
            content = content.replace("simcli.entities.actors.RelationshipStatus", "simcli.entities.models.RelationshipStatus");
            content = content.replace("simcli.entities.actors.SimState", "simcli.entities.models.SimState");
            content = content.replace("simcli.entities.actors.Trait", "simcli.entities.models.Trait");
            
            content = content.replace("simcli.entities.components.SkillType", "simcli.entities.models.SkillType");
            content = content.replace("simcli.entities.components.InventoryManager", "simcli.entities.managers.InventoryManager");
            content = content.replace("simcli.entities.components.RelationshipManager", "simcli.entities.managers.RelationshipManager");
            content = content.replace("simcli.entities.components.SkillManager", "simcli.entities.managers.SkillManager");
            content = content.replace("simcli.entities.components.CareerProfile", "simcli.entities.managers.CareerManager");

            // Variable and Class Renames
            content = content.replace("CareerProfile", "CareerManager");
            content = content.replace("careerProfile", "careerManager");

            if (!oldContent.equals(content)) {
                Files.write(p, content.getBytes());
            }
        }

        setPackage(baseDir, "src/simcli/entities/models/ActionState.java", "package simcli.entities.models;");
        setPackage(baseDir, "src/simcli/entities/models/Gender.java", "package simcli.entities.models;");
        setPackage(baseDir, "src/simcli/entities/models/Job.java", "package simcli.entities.models;");
        setPackage(baseDir, "src/simcli/entities/models/Relationship.java", "package simcli.entities.models;");
        setPackage(baseDir, "src/simcli/entities/models/RelationshipStatus.java", "package simcli.entities.models;");
        setPackage(baseDir, "src/simcli/entities/models/SimState.java", "package simcli.entities.models;");
        setPackage(baseDir, "src/simcli/entities/models/Trait.java", "package simcli.entities.models;");
        setPackage(baseDir, "src/simcli/entities/models/SkillType.java", "package simcli.entities.models;");
        
        setPackage(baseDir, "src/simcli/entities/managers/InventoryManager.java", "package simcli.entities.managers;");
        setPackage(baseDir, "src/simcli/entities/managers/RelationshipManager.java", "package simcli.entities.managers;");
        setPackage(baseDir, "src/simcli/entities/managers/SkillManager.java", "package simcli.entities.managers;");
        setPackage(baseDir, "src/simcli/entities/managers/CareerManager.java", "package simcli.entities.managers;");

        System.out.println("REFACTOR COMPLETE");
    }

    static void setPackage(String baseDir, String relPath, String pkgStmt) throws IOException {
        Path p = Paths.get(baseDir, relPath);
        if(!Files.exists(p)) return;
        List<String> lines = Files.readAllLines(p);
        for(int i=0; i<lines.size(); i++){
            if(lines.get(i).startsWith("package ")) {
                lines.set(i, pkgStmt);
                break;
            }
        }
        Files.write(p, lines);
    }

    static void moveFile(String baseDir, String fromRel, String toRel) {
        try {
            Path src = Paths.get(baseDir, fromRel);
            Path dest = Paths.get(baseDir, toRel);
            if (Files.exists(src)) {
                Files.createDirectories(dest.getParent());
                Files.move(src, dest, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Moved " + src.getFileName() + " to " + dest.getParent().getFileName());
            }
        } catch (Exception e) {}
    }
}
