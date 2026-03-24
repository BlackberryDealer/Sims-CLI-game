package simcli.entities.lifecycle;

import simcli.engine.LifecycleManager;
import simcli.entities.actors.Sim;
import simcli.entities.models.Gender;

/**
 * LifecycleDemo — a standalone CLI demonstration of the State Design Pattern
 * applied to a Sim's aging and life cycle system.
 *
 * <h2>What this demo proves</h2>
 * l>
 * <strong>One object, forever:</strong> {@code bob} is a {@link Sim}
 *     created once and never replaced. The Java identity hash never change
 * .</li>
 * <strong>Polymorphic behaviour change:</strong> Before age 18 the Sim cannot
 * work; after turning 18 it can — with no {@code instanceof}, no type cast,
 *     and no subclass swap required.</li>
 * <strong>Clean separation:</strong> {@link LifecycleManager} tracks time;
 * {@link simcli.entities.actors.Sim#ageUp()} owns the transition; the concrete
 *       stages own the rules. Each class has exactly one responsibility.</li>
 * </ol>
 *
 * 
 * <h2>Compile and run (no GUI, no database — pure CLI)</h2>
 * <pre>
 *   javac -d bin -sourcepath src src\simcli\entities\lifecycle\LifecycleDemo.java
 *   java  -cp bin simcli.entities.lifecycle.LifecycleDemo
 * </pre>
 */
public class LifecycleDemo {

    public static void main(String[] args) {

        // ====================================================================
        // Create the Sim ONCE.
        // Using the unified Sim class which sets ChildStage inside based on age.
        //          This single object will live for Bob's entire simulated life.
        // ====================================================================
        System.out.println("============================================================");
        System.out.println("  Sims CLI — State Pattern Lifecycle Demo");
        System.out.println("============================================================\n");

        // Sim constructor now sets ChildStage or AdultStage based on age.
        Sim bob = new Sim("Bob", 0, Gender.MALE);

        int bobIdentityHash = System.identityHashCode(bob);
        System.out.println(">>> Sim created:");
        System.out.println("    Object identity hash (memory proxy): " + bobIdentityHash);
        System.out.println("    " + bob.getName()
                + " | Age: " + bob.getAge()
                + " | Stage: " + bob.getCurrentStageName()
                + " | canWork: " + bob.canWork());
        System.out.println();

        // ====================================================================
        // LifecycleManager with ticksPerYear = 1
        // 1 tick = 1 year for demo brevity. In a real game this would
        //          be 24 * 365 or similar.
        // ====================================================================
        LifecycleManager manager = new LifecycleManager(1);

        // ====================================================================
        // Age Bob from 0 → 17.
        // Each processTick() call crosses a year boundary (ticksPerYear=1),
        //          so ageUp() fires every tick. Bob stays in ChildStage.
        // ====================================================================
        System.out.println("------------------------------------------------------------");
        System.out.println("  Aging Bob from birth to age 17 (ChildStage active)...");
        System.out.println("------------------------------------------------------------");
        for (int year = 1; year <= 17; year++) {
            manager.processTick(bob);
        }

        System.out.println();
        System.out.println(">>> Bob at age " + bob.getAge() + " (before 18th birthday):");
        printBehaviourSummary(bob);
        System.out.println("    Java identity hash: " + System.identityHashCode(bob)
                + "  <-- same object, never replaced");
        System.out.println();

        // ====================================================================
        // The 18th birthday tick.
        // ChildStage.getNextStage(18) returns a NEW AdultStage object.
        // Sim.ageUp() detects the new instance (identity check !=)
        // and executes the "brain swap": currentStage = new AdultStage().
        //          BOB IS THE EXACT SAME JAVA OBJECT BEFORE AND AFTER THIS LINE.
        // ====================================================================
        System.out.println("------------------------------------------------------------");
        System.out.println("  Processing Bob's 18th birthday tick — the 'brain swap'...");
        System.out.println("------------------------------------------------------------");
        manager.processTick(bob); // <-- Stage swap happens inside this call

        System.out.println();
        System.out.println(">>> Bob at age " + bob.getAge() + " (after 18th birthday):");
        printBehaviourSummary(bob);
        System.out.println("    Java identity hash: " + System.identityHashCode(bob)
                + "  <-- STILL the very same object! Only the stage reference changed.");
        System.out.println();

        // ====================================================================
        // STEP 5 — Confirm identity hash is unchanged, proving no new object.
        // ====================================================================
        System.out.println("============================================================");
        System.out.println("  DEMO SUMMARY");
        System.out.println("============================================================");
        System.out.println("  Identity hash at creation : " + bobIdentityHash);
        System.out.println("  Identity hash now          : " + System.identityHashCode(bob));
        System.out.println("  Hashes equal? " + (bobIdentityHash == System.identityHashCode(bob))
                + "  <- Bob was NEVER replaced in memory.");
        System.out.println();
        System.out.println("  Bob's name        : " + bob.getName());
        System.out.println("  Bob's final age   : " + bob.getAge());
        System.out.println("  Bob's final stage : " + bob.getCurrentStageName());
        System.out.println("  Bob can work?     : " + bob.canWork());
        System.out.println("  Energy decay rate : " + bob.getLifeStage().getEnergyDecayModifier() + "x");
        System.out.println("  Total ticks run   : " + manager.getCurrentTick());
        System.out.println();
        System.out.println("  The Sim was created once. Only the internal 'currentStage'");
        System.out.println("  reference was swapped from ChildStage → AdultStage at age 18.");
        System.out.println("  This is the State Design Pattern eliminating the trapped-object problem.");
        System.out.println("============================================================");
    }

    /**
     * Prints a behaviour summary for a Sim using only the base {@code Sim} API.
     * No {@code instanceof}, no casting — pure polymorphism.
     */
    private static void printBehaviourSummary(Sim sim) {
        System.out.println("  Stage       : " + sim.getCurrentStageName());
        if (sim.canWork()) {
            System.out.println("  Work status : ✔ CAN WORK — eligible for jobs and income.");
        } else {
            System.out.println("  Work status : ✘ CANNOT WORK — children should study and play.");
        }
        System.out.printf("  Energy decay: %.1fx the normal rate.%n",
                sim.getLifeStage().getEnergyDecayModifier());
    }
}
