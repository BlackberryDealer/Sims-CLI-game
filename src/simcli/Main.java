package simcli;

import simcli.ui.MainMenu;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        MainMenu menu = new MainMenu(scanner);
        menu.display();
        
        scanner.close();
    }
}
