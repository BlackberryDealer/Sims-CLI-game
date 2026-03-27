package simcli.engine.commands;

import java.util.Scanner;
import simcli.engine.CommandResult;
import simcli.engine.SimulationException;
import simcli.engine.SleepEventException;
import simcli.ui.UIManager;

/**
 * Abstract base class for all commands in the Command Pattern.
 * 
 * <p>Uses the <b>Template Method Pattern</b>: {@link #execute()} defines the
 * fixed execution contract with callers (e.g. InputHandler), while subclasses
 * implement their specific logic in the {@link #run()} hook method.</p>
 * 
 * <p>Making {@code execute()} final enforces the Open/Closed Principle —
 * the template is closed to modification but open to extension via {@code run()}.</p>
 */
public abstract class BaseCommand implements ICommand {

    /**
     * Template method: defines the overall execution flow.
     * Delegates to the {@link #run()} hook method implemented by each subclass.
     * 
     * <p>This method is {@code final} to prevent subclasses from breaking the
     * execution contract. All custom logic belongs in {@link #run()}.</p>
     * 
     * @return the result indicating how the GameEngine should proceed
     * @throws SimulationException  if game rules prevent execution
     * @throws SleepEventException  if the command triggers a sleep transition
     */
    @Override
    public final CommandResult execute() throws SimulationException, SleepEventException {
        return run();
    }

    /**
     * Hook method for concrete subclasses to provide their specific command logic.
     * This is the only method child classes need to override.
     * 
     * @return the result indicating how the GameEngine should proceed
     * @throws SimulationException  if game rules prevent execution
     * @throws SleepEventException  if the command triggers a sleep transition
     */
    protected abstract CommandResult run() throws SimulationException, SleepEventException;

    /**
     * Utility method: pauses execution and waits for the user to press ENTER.
     * Available to all subclasses to avoid duplicating this common UI pattern.
     * 
     * @param scanner the input scanner to read the ENTER keypress from
     */
    protected void pause(Scanner scanner) {
        UIManager.prompt("\nPress ENTER to return...");
        scanner.nextLine();
    }
}
