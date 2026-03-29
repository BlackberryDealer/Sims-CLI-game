package simcli.engine.commands;

import simcli.engine.CommandResult;
import simcli.engine.SimulationException;
import simcli.engine.SleepEventException;

// Contract that every player action must fulfill.
// Each action (work, travel, interact, etc.) becomes its own class
// instead of living inside one giant if/else chain in InputHandler.
public interface ICommand {

    // Returns a result telling the engine whether to advance time or not.
    // Throws SimulationException if the game rules block the action,
    // or SleepEventException to signal a sleep-skip to next morning.
    CommandResult execute() throws SimulationException, SleepEventException;
}
