package simcli.engine.commands;

import simcli.engine.CommandResult;
import simcli.engine.SimulationException;
import simcli.engine.SleepEventException;
import simcli.ui.UIManager;

// Template Method Pattern:
// execute() is the locked-down skeleton (final) — subclasses only override run().
// This keeps the contract with InputHandler/GameEngine untouchable while still
// letting each command define its own behaviour. Classic Open/Closed Principle.
//
// Every command gets a CommandContext injected through the constructor
// so none of them ever need a direct reference to GameEngine.
public abstract class BaseCommand implements ICommand {

    // single source of truth for all game state a command may need
    protected final CommandContext ctx;


    protected BaseCommand(CommandContext ctx) {
        this.ctx = ctx;
    }

    // final = nobody can override the top-level flow. All custom logic goes in run().
    @Override
    public final CommandResult execute() throws SimulationException, SleepEventException {
        return run();
    }

    // hook method — each concrete command puts its actual logic here
    protected abstract CommandResult run() throws SimulationException, SleepEventException;

    // shared helper so every command can pause without duplicating code
    protected void pause() {
        UIManager.prompt("\nPress ENTER to return...");
        ctx.getScanner().nextLine();
    }
}
