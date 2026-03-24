package simcli.ui;

import simcli.entities.items.Item;

import java.util.List;
import java.util.Scanner;

/**
 * Reusable paginated menu utility for displaying lists of Items.
 * Used by shops, inventory, and storage containers.
 */
public class MenuPagination {

    /** Callback interface for handling item selection. */
    public interface MenuAction {
        void onSelect(Item item, int realIndex);
    }

    /**
     * Displays a paginated menu of items and processes user selection.
     *
     * @param title        title shown at the top of the menu
     * @param catalog      the list of items to display
     * @param actionPrompt prompt shown to the user
     * @param scanner      for reading user input
     * @param action       callback executed when an item is selected
     */
    public static void displayPaginatedMenu(String title, List<? extends Item> catalog,
            String actionPrompt, Scanner scanner, MenuAction action) {
        int pageSize = 10;
        int currentPage = 0;
        boolean inMenu = true;

        while (inMenu) {
            int totalPages = (int) Math.ceil((double) catalog.size() / pageSize);
            if (totalPages == 0) totalPages = 1;
            if (currentPage >= totalPages) currentPage = totalPages - 1;

            UIManager.printMessage("\n=== " + title + " (Page " + (currentPage + 1) + " of " + totalPages + ") ===");
            
            if (catalog.isEmpty()) {
                UIManager.printMessage("Nothing to display here.");
            } else {
                int startIdx = currentPage * pageSize;
                int endIdx = Math.min(startIdx + pageSize, catalog.size());

                for (int i = startIdx; i < endIdx; i++) {
                    Item item = catalog.get(i);
                    UIManager.printMessage("[" + (i - startIdx + 1) + "] " + item.getObjectName() + 
                                     (item.getPrice() > 0 ? " - $" + item.getPrice() : ""));
                }
            }

            UIManager.printMessage("\n[N] Next Page   [P] Previous Page");
            UIManager.printMessage("[0] Back / Exit");
            UIManager.prompt(actionPrompt + "> ");

            String input = scanner.nextLine().trim().toUpperCase();

            if (input.equals("0")) {
                inMenu = false;
            } else if (input.equals("N")) {
                if (currentPage < totalPages - 1) currentPage++;
            } else if (input.equals("P")) {
                if (currentPage > 0) currentPage--;
            } else {
                try {
                    int choice = Integer.parseInt(input);
                    int startIdx = currentPage * pageSize;
                    int maxChoice = Math.min(pageSize, catalog.size() - startIdx);
                    
                    if (choice > 0 && choice <= maxChoice) {
                        int realIndex = startIdx + choice - 1;
                        Item target = catalog.get(realIndex);
                        action.onSelect(target, realIndex);
                    } else {
                        UIManager.printMessage("Invalid selection.");
                    }
                } catch (NumberFormatException e) {
                    UIManager.printMessage("Invalid input. Use numbers or N/P.");
                }
            }
        }
    }
}
