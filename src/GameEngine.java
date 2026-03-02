import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;

/**
 * The Controller: Initializes the environment and manages the core gameplay loop.
 */
public class GameEngine {
    private List<Sim> neighborhood;
    private Residential playerHome;
    
    public GameEngine() {
        this.neighborhood = new ArrayList<>();
        this.playerHome = new Residential("The Shared Dorm");
        
        // Populating the environment with interactive objects
        this.playerHome.addInteractable(new Bed());
        this.playerHome.addInteractable(new Computer());
        this.playerHome.addInteractable(new WeightBench());
    }
    
    public void init() {
        AdultSim player1 = new AdultSim("Dylan", 21, "Software Engineer");
        this.neighborhood.add(player1);
        System.out.println("=== Booting Simulation: The Sims (CLI Edition) ===");
    }
    
    public void run() {
        Scanner scanner = new Scanner(System.in);
        boolean running = true;
        int currentTick = 1;
        
        // Polymorphism: Grabbing a Sim reference
        Sim activePlayer = this.neighborhood.get(0);
        this.playerHome.enter(activePlayer);
        
        while (running) {
            System.out.println("\n--- TICK " + currentTick + " ---");
            activePlayer.tick();
            
            if (activePlayer.getState() == SimState.DEAD) {
                System.out.println("FATAL: " + activePlayer.getName() + " has expired. Simulation Terminated.");
                break;
            }
            
            System.out.println("\nSelect Action for " + activePlayer.getName() + ":");
            System.out.println("[1] Go to Work");
            System.out.println("[2] Sleep (Use Bed)");
            System.out.println("[3] Study (Use Computer)");
            System.out.println("[4] Exercise (Use Weight Bench)");
            System.out.println("[5] Exit Simulation");
            System.out.print("COMMAND> ");
            
            String choice = scanner.nextLine();
            
            try {
                switch (choice) {
                    case "1":
                        activePlayer.performActivity("Work");
                        break;
                    case "2":
                        this.playerHome.getInteractables().get(0).interact(activePlayer);
                        break;
                    case "3":
                        this.playerHome.getInteractables().get(1).interact(activePlayer);
                        break;
                    case "4":
                        this.playerHome.getInteractables().get(2).interact(activePlayer);
                        break;
                    case "5":
                        running = false;
                        System.out.println("Saving and Exiting...");
                        break;
                    default:
                        System.out.println("Invalid CLI command. Time passes idly.");
                }
            } catch (SimulationException e) {
                // Checked Exception handling
                System.err.println("ACTION REJECTED: " + e.getMessage());
            } catch (Exception e) {
                // Unchecked/Runtime Exception fallback
                System.err.println("CRITICAL SYSTEM FAULT: " + e.toString());
            } finally {
                System.out.println("Tick " + currentTick + " resolution complete.");
            }
            
            currentTick++;
        }
        
        scanner.close();
    }
    
    public static void main(String[] args) {
        GameEngine engine = new GameEngine();
        engine.init();
        engine.run();
    }
}