package simcli.ui;

import simcli.entities.items.Item;

import java.util.List;
import java.util.Scanner;
import java.util.function.Supplier;

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
        displayPaginatedMenu(title, catalog, actionPrompt, scanner, action, null);
    }

    /**
     * Displays a paginated menu of items with a status line (e.g. the
     * player's cash) shown under the title and refreshed after every choice.
     *
     * @param status supplies the status line, or {@code null} for none
     */
    public static void displayPaginatedMenu(String title, List<? extends Item> catalog,
            String actionPrompt, Scanner scanner, MenuAction action, Supplier<String> status) {
        int pageSize = 10;
        int currentPage = 0;
        boolean inMenu = true;

        while (inMenu) {
            int totalPages = (int) Math.ceil((double) catalog.size() / pageSize);
            if (totalPages == 0) totalPages = 1;
            if (currentPage >= totalPages) currentPage = totalPages - 1;

            String pages = totalPages > 1 ? "  {k}page " + (currentPage + 1) + "/" + totalPages + "{/}" : "";
            UIManager.title(title + pages);
            if (status != null) {
                UIManager.printMessage("  " + status.get() + "\n");
            }

            if (catalog.isEmpty()) {
                UIManager.info("Nothing to display here.");
            } else {
                int startIdx = currentPage * pageSize;
                int endIdx = Math.min(startIdx + pageSize, catalog.size());

                for (int i = startIdx; i < endIdx; i++) {
                    Item item = catalog.get(i);
                    String price = item.getPrice() > 0 ? "{G}$" + item.getPrice() + "{/}" : "";
                    UIManager.option(String.valueOf(i - startIdx + 1),
                            Style.fit(item.getObjectName(), 18) + Style.fit(price, 7) + ItemInfo.describe(item));
                }
            }

            System.out.println();
            if (totalPages > 1) {
                UIManager.option("N", "Next page");
                UIManager.option("P", "Previous page");
            }
            UIManager.option("0", "Done");
            UIManager.prompt("\n" + actionPrompt + "> ");

            String input = scanner.nextLine().trim().toUpperCase();

            if (input.equals("0") || input.isEmpty()) {
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
                        UIManager.error("Invalid selection.");
                    }
                } catch (NumberFormatException e) {
                    UIManager.error("Invalid input. Use numbers or N/P.");
                }
            }
        }
    }
}
