package simcli.engine.commands;

import simcli.engine.CommandResult;
import simcli.engine.SimulationException;
import simcli.engine.SleepEventException;
import simcli.ui.UIManager;
import simcli.world.interactables.Interactable;

import java.util.List;

public class InteractCommand extends BaseCommand {
    private final int choiceIndex;

    /**
     * InteractCommand is the only command that needs an extra parameter beyond
     * the context: the index of the item the player chose to interact with.
     *
     * @param ctx         shared command context.
     * @param choiceIndex zero-based index into the available-items list.
     */
    public InteractCommand(CommandContext ctx, int choiceIndex) {
        super(ctx);
        this.choiceIndex = choiceIndex;
    }

    @Override
    protected CommandResult run() throws SimulationException, SleepEventException {
        List<Interactable> items = ctx.getAvailableItems();

        if (choiceIndex >= 0 && choiceIndex < items.size()) {
            items.get(choiceIndex).interact(ctx.getActivePlayer(), ctx.getScanner(), ctx.getTimeManager());
            return CommandResult.TICK_FORWARD;
        } else {
            UIManager.printMessage("Invalid item choice.");
            pause();
            return CommandResult.NO_TICK;
        }
    }
}
