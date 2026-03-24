package simcli.engine;

import simcli.entities.actors.Sim;

import java.util.Scanner;

/**
 * Contract for the class that processes raw player input during the game loop.
 * GameEngine calls this interface so it never needs to contain command-parsing
 * logic itself, keeping it a clean coordinator.
 */
public interface IInputHandler {

    /**
     * Processes a single player command string.
     *
     * @param input        the raw input string (already uppercased by the caller)
     * @param activePlayer the currently active Sim
     * @param scanner      for reading follow-up user input inside sub-menus
     * @return a {@link CommandResult} telling the engine what to do next
     */
    CommandResult handle(String input, Sim activePlayer, Scanner scanner);
}
