package simcli.ui;

import simcli.entities.Item;

import java.util.List;
import java.util.Scanner;

public class MenuPagination {
    public interface MenuAction {
        void onSelect(Item item, int realIndex);
    }

    public static void displayPaginatedMenu(String title, List<? extends Item> catalog, String actionPrompt, Scanner scanner, MenuAction action) {
        int pageSize = 10;
        int currentPage = 0;
        boolean inMenu = true;

        while (inMenu) {
            int totalPages = (int) Math.ceil((double) catalog.size() / pageSize);
            if (totalPages == 0) totalPages = 1;
            if (currentPage >= totalPages) currentPage = totalPages - 1;

            System.out.println("\n=== " + title + " (Page " + (currentPage + 1) + " of " + totalPages + ") ===");
            
            if (catalog.isEmpty()) {
                System.out.println("Nothing to display here.");
            } else {
                int startIdx = currentPage * pageSize;
                int endIdx = Math.min(startIdx + pageSize, catalog.size());

                for (int i = startIdx; i < endIdx; i++) {
                    Item item = catalog.get(i);
                    System.out.println("[" + (i - startIdx + 1) + "] " + item.getObjectName() + 
                                     (item.getPrice() > 0 ? " - $" + item.getPrice() : ""));
                }
            }

            System.out.println("\n[N] Next Page   [P] Previous Page");
            System.out.println("[0] Back / Exit");
            System.out.print(actionPrompt + "> ");

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
                        System.out.println("Invalid selection.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input. Use numbers or N/P.");
                }
            }
        }
    }
}
