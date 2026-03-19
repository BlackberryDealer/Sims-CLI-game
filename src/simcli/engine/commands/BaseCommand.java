package simcli.engine.commands;

import java.util.Scanner;
import simcli.ui.UIManager;

public abstract class BaseCommand implements ICommand {
    protected void pause(Scanner scanner) {
        UIManager.prompt("\nPress ENTER to return...");
        scanner.nextLine();
    }
}
