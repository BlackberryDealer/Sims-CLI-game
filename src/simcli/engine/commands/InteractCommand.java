package simcli.engine.commands;

import simcli.engine.CommandResult;
import simcli.engine.SimulationException;
import simcli.engine.SleepEventException;
import simcli.engine.TimeManager;
import simcli.entities.actors.Sim;
import simcli.ui.UIManager;
import simcli.world.interactables.Interactable;

import java.util.List;
import java.util.Scanner;

public class InteractCommand extends BaseCommand {
    private final Sim activePlayer;
    private final Scanner scanner;
    private final TimeManager timeManager;
    private final List<Interactable> items;
    private final int choiceIndex;

    public InteractCommand(Sim activePlayer, Scanner scanner, TimeManager timeManager, List<Interactable> items, int choiceIndex) {
        this.activePlayer = activePlayer;
        this.scanner = scanner;
        this.timeManager = timeManager;
        this.items = items;
        this.choiceIndex = choiceIndex;
    }

    @Override
    protected CommandResult run() throws SimulationException, SleepEventException {
        if (choiceIndex >= 0 && choiceIndex < items.size()) {
            items.get(choiceIndex).interact(activePlayer, scanner, timeManager);
            return CommandResult.TICK_FORWARD;
        } else {
            UIManager.printMessage("Invalid item choice.");
            pause(scanner);
            return CommandResult.NO_TICK;
        }
    }

}
