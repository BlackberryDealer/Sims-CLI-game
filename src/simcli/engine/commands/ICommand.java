package simcli.engine.commands;

import simcli.engine.CommandResult;
import simcli.engine.SimulationException;
import simcli.engine.SleepEventException;

/**
 * The Command interface for the Command Pattern.
 * Encapsulates a request as an object, letting us parameterize clients
 * with different requests and avoid massive if/else blocks in InputHandler.
 */
public interface ICommand {

    /**
     * Executes the command.
     * 
     * @return The CommandResult indicating how the GameEngine should proceed next.
     * @throws SimulationException If the rule logic prevents execution.
     * @throws SleepEventException If the command triggers a sleep/rest transition.
     */
    CommandResult execute() throws SimulationException, SleepEventException;
}
