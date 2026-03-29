package simcli.engine.commands;

import simcli.engine.CommandResult;
import simcli.engine.SimulationException;
import simcli.engine.SleepEventException;
import simcli.ui.UIManager;
import simcli.world.interactables.Interactable;

import java.util.List;

/**
 * Command that interacts with a specific item at the current location or room.
 *
 * <p>Delegates to the chosen {@link Interactable}'s own
 * {@link Interactable#interact} method, which may throw
 * {@link SleepEventException} (e.g. using a bed) or
 * {@link SimulationException} (e.g. action not allowed).</p>
 */
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

    /**
     * Executes the interaction by calling the selected item's interact method.
     *
     * @return {@link CommandResult#TICK_FORWARD} on success,
     *         {@link CommandResult#NO_TICK} if the index is out of range.
     * @throws SimulationException  propagated from the interactable.
     * @throws SleepEventException  propagated if the interaction causes sleep.
     */
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
