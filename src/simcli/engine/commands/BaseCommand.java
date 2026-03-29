package simcli.engine.commands;

import simcli.engine.CommandResult;
import simcli.engine.SimulationException;
import simcli.engine.SleepEventException;
import simcli.ui.UIManager;

/**
 * Template Method Pattern base class for all player commands.
 *
 * <p>{@link #execute()} is the locked-down skeleton ({@code final}) —
 * subclasses only override {@link #run()}. This keeps the contract with
 * InputHandler/GameEngine untouchable while still letting each command
 * define its own behaviour. Classic Open/Closed Principle.</p>
 *
 * <p>Every command gets a {@link CommandContext} injected through the
 * constructor so none of them ever need a direct reference to
 * {@code GameEngine}.</p>
 */
public abstract class BaseCommand implements ICommand {

    /** Single source of truth for all game state a command may need. */
    protected final CommandContext ctx;

    /**
     * Constructs a new command with the given context.
     *
     * @param ctx the shared command context for this turn.
     */
    protected BaseCommand(CommandContext ctx) {
        this.ctx = ctx;
    }

    /**
     * Executes this command by delegating to the {@link #run()} hook method.
     *
     * <p>This method is {@code final} so nobody can override the top-level
     * flow. All custom logic goes in {@link #run()}.</p>
     *
     * @return a {@link CommandResult} indicating the outcome.
     * @throws SimulationException if game rules block the action.
     * @throws SleepEventException to signal a sleep-skip.
     */
    @Override
    public final CommandResult execute() throws SimulationException, SleepEventException {
        return run();
    }

    /**
     * Hook method — each concrete command puts its actual logic here.
     *
     * @return a {@link CommandResult} indicating the outcome.
     * @throws SimulationException if game rules block the action.
     * @throws SleepEventException to signal a sleep-skip.
     */
    protected abstract CommandResult run() throws SimulationException, SleepEventException;

    /**
     * Shared helper so every command can pause without duplicating code.
     * Flushes buffered output and waits for the player to press ENTER.
     */
    protected void pause() {
        UIManager.prompt("\nPress ENTER to return...");
        ctx.getScanner().nextLine();
    }
}
