package simcli.engine.commands;

import simcli.engine.CommandResult;
import simcli.engine.SimulationException;
import simcli.engine.SleepEventException;
import simcli.entities.actors.Sim;
import simcli.ui.UIManager;
import simcli.world.interactables.Interactable;

import java.util.List;
import java.util.Scanner;

public class InteractCommand extends BaseCommand {
    private final CommandContext ctx;
    private final List<Interactable> items;
    private final int choiceIndex;

    public InteractCommand(CommandContext ctx, List<Interactable> items, int choiceIndex) {
        this.ctx = ctx;
        this.items = items;
        this.choiceIndex = choiceIndex;
    }

    @Override
    protected CommandResult run() throws SimulationException, SleepEventException {
        Sim activePlayer = ctx.getActivePlayer();
        Scanner scanner = ctx.getScanner();

        if (choiceIndex >= 0 && choiceIndex < items.size()) {
            items.get(choiceIndex).interact(activePlayer, scanner, ctx.getTimeManager());
            return CommandResult.TICK_FORWARD;
        } else {
            UIManager.printMessage("Invalid item choice.");
            pause(scanner);
            return CommandResult.NO_TICK;
        }
    }

}
