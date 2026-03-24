package simcli.ui;

import simcli.entities.actors.Sim;
import simcli.world.Building;
import simcli.world.interactables.Interactable;
import simcli.ui.ascii.AsciiEngine;

import java.util.List;

/**
 * Concrete implementation of {@link IRenderer} for terminal-based display.
 * All output is routed through {@link UIManager} for consistency.
 */
public class TerminalRenderer implements IRenderer {
    private final AsciiEngine asciiEngine;

    public TerminalRenderer() {
        this.asciiEngine = new AsciiEngine();
    }

    @Override
    public void clear() {
        UIManager.clearScreen();
    }

    @Override
    public void printHint() {
        UIManager.printHint();
    }

    @Override
    public void renderHUD(Sim player, Building location, int day, String formattedTime,
            String timeOfDay, boolean inRoom, String roomName) {
        
        UIManager.printMessage(asciiEngine.render(player, location));

        String locationName = location != null ? location.getName() : "Unknown";

        if (inRoom) {
            UIManager.printMessage("\n--- DAY " + day + " | " + timeOfDay
                    + " (" + formattedTime + ") | Location: " + locationName
                    + " - Room: " + roomName + " ---");
        } else {
            UIManager.printMessage("\n--- DAY " + day + " | " + timeOfDay
                    + " (" + formattedTime + ") | Location: " + locationName
                    + " ---");
        }
        
        // Reset action to IDLE after rendering so art doesn't persist next frame
        player.setCurrentAction(simcli.entities.models.ActionState.IDLE);
    }

    @Override
    public void renderHouseholdDashboard(List<Sim> neighborhood, Sim activePlayer) {
        StringBuilder inactiveStats = new StringBuilder("HOUSEHOLD: ");
        boolean hasInactive = false;
        for (Sim sim : neighborhood) {
            if (sim != activePlayer && sim.getState() != simcli.entities.models.SimState.DEAD) {
                inactiveStats.append("[").append(sim.getName()).append(": ")
                             .append(sim.getHealth()).append("% | ")
                             .append(sim.getState()).append("]  ");
                hasInactive = true;
            }
        }
        if (hasInactive) {
            UIManager.printMessage(inactiveStats.toString());
        }
    }

    @Override
    public void renderActiveSimStats(Sim activePlayer, List<Sim> neighborhood) {
        UIManager.printMessage("\n==========================================================================");
        String stats = "[" + activePlayer.getName().toUpperCase() + "] Health: " + activePlayer.getHealth() + "%" +
                " | Hunger: " + activePlayer.getHunger().getValue() +
                " | Energy: " + activePlayer.getEnergy().getValue() +
                " | Hygiene: " + activePlayer.getHygiene().getValue() +
                " | Happiness: " + activePlayer.getHappiness().getValue() +
                " | Social: " + activePlayer.getSocial().getValue() +
                " | Cash: $" + activePlayer.getMoney() + " | Job: " + activePlayer.getCareer().getTitle() + 
                " | State: " + activePlayer.getState();
        UIManager.printMessage(stats);

        // Add Baby Hunger if applicable (any non-playable child Sim in household)
        for (Sim sim : neighborhood) {
            if (sim.isChildSim() && !sim.isPlayable()) {
                UIManager.printMessage("[BABY] " + sim.getName() + " Hunger: " + sim.getHunger().getValue() + "/100");
            }
        }
        
        UIManager.printMessage("==========================================================================");
    }

    @Override
    public void renderDeathStats(Sim deadSim) {
        UIManager.printMessage("\n==========================================");
        UIManager.printMessage("  Oh no! " + deadSim.getName() + " has tragically died!");
        UIManager.printMessage("==========================================");
        UIManager.printMessage("  --- Final Stats ---");
        UIManager.printMessage("  Name:      " + deadSim.getName());
        UIManager.printMessage("  Age:       " + deadSim.getAge());
        UIManager.printMessage("  Hunger:    " + deadSim.getHunger().getValue() + " / " + simcli.needs.Need.MAX_VALUE);
        UIManager.printMessage("  Energy:    " + deadSim.getEnergy().getValue() + " / " + simcli.needs.Need.MAX_VALUE);
        UIManager.printMessage("  Hygiene:   " + deadSim.getHygiene().getValue() + " / " + simcli.needs.Need.MAX_VALUE);
        UIManager.printMessage("  Happiness: " + deadSim.getHappiness().getValue() + " / " + simcli.needs.Need.MAX_VALUE);
        UIManager.printMessage("  Social:    " + deadSim.getSocial().getValue() + " / " + simcli.needs.Need.MAX_VALUE);
        UIManager.printMessage("  Health:    " + deadSim.getHealth() + "%");
        UIManager.printMessage("  Cash:      $" + deadSim.getMoney());
        UIManager.printMessage("  Total Earned: $" + deadSim.getTotalMoneyEarned());
        UIManager.printMessage("==========================================");
    }

    @Override
    public void renderActions(Sim activePlayer, List<Interactable> items, boolean isResidential) {
        UIManager.printActionGrid(activePlayer, items, isResidential);
    }
}
