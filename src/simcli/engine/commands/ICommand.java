package simcli.engine.commands;

import simcli.engine.CommandResult;
import simcli.engine.SimulationException;
import simcli.engine.SleepEventException;

/**
 * Contract that every player action must fulfill.
 *
 * <p>Each action (work, travel, interact, etc.) becomes its own class
 * instead of living inside one giant if/else chain in InputHandler.
 * This is the core interface of the <b>Command Pattern</b> used throughout
 * the engine.</p>
 */
public interface ICommand {

    /**
     * Executes this command and returns a result telling the engine
     * whether to advance time or not.
     *
     * @return a {@link CommandResult} indicating the outcome of the command.
     * @throws SimulationException  if the game rules block the action
     *                              (e.g. too young to work, insufficient funds).
     * @throws SleepEventException  to signal a sleep-skip to next morning.
     */
    CommandResult execute() throws SimulationException, SleepEventException;
}
