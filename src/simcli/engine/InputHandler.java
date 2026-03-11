package simcli.engine;

import simcli.entities.AdultSim;
import simcli.entities.Job;
import simcli.entities.Sim;
import simcli.ui.AsciiArt;
import simcli.world.Building;
import simcli.world.Residential;
import simcli.world.Room;
import simcli.world.interactables.Interactable;

import java.util.List;
import java.util.Scanner;

public class InputHandler implements IInputHandler {
    private IWorldManager worldManager;
    private TimeManager timeManager;

    public InputHandler(IWorldManager worldManager, TimeManager timeManager) {
        this.worldManager = worldManager;
        this.timeManager = timeManager;
    }

    @Override
    public CommandResult handle(String input, Sim activePlayer, Scanner scanner) {
        Building currentLocation = worldManager.getCurrentLocation();
        List<Interactable> items;

        if (currentLocation instanceof Residential && activePlayer.getCurrentRoom() != null) {
            items = activePlayer.getCurrentRoom().getInteractables();
        } else {
            items = currentLocation.getInteractables();
        }

        try {
            if (input.equals("W")) {
                activePlayer.performActivity("Work");
                if (activePlayer instanceof AdultSim) {
                    timeManager.advanceTicks(((AdultSim) activePlayer).getCareer().getWorkingHours() - 1);
                }
                return CommandResult.TICK_FORWARD;
            } else if (input.equals("J")) {
                handleJobMarket(activePlayer, scanner);
                return CommandResult.NO_TICK; // Handled internally
            } else if (input.equals("T")) {
                return handleTravel(activePlayer, scanner, currentLocation);
            } else if (input.equals("M")) {
                handleMoveRoom(activePlayer, scanner, currentLocation);
                return CommandResult.NO_TICK;
            } else if (input.equals("H")) {
                handleHouseInfo(currentLocation, scanner);
                return CommandResult.NO_TICK;
            } else if (input.equals("I")) {
                handleCharacterStatus(activePlayer, currentLocation, scanner);
                return CommandResult.NO_TICK;
            } else if (input.equals("V")) {
                handleInventory(activePlayer, currentLocation, scanner);
                return CommandResult.NO_TICK;
            } else if (input.equals("U")) {
                handleUpgradeRoom(activePlayer, currentLocation, scanner);
                return CommandResult.NO_TICK;
            } else if (input.equals("S")) {
                return CommandResult.SAVE_AND_EXIT;
            } else {
                int choice = Integer.parseInt(input) - 1;
                if (choice >= 0 && choice < items.size()) {
                    items.get(choice).interact(activePlayer, scanner, timeManager);
                    return CommandResult.TICK_FORWARD;
                } else {
                    System.out.println("Invalid item choice.");
                    pause(scanner);
                    return CommandResult.NO_TICK;
                }
            }
        } catch (SleepEventException e) {
            return CommandResult.SLEEP_EVENT;
        } catch (SimulationException e) {
            System.err.println("ACTION REJECTED: " + e.getMessage());
            pause(scanner);
            return CommandResult.NO_TICK;
        } catch (NumberFormatException e) {
            System.err.println("Invalid input.");
            pause(scanner);
            return CommandResult.NO_TICK;
        }
    }

    private void pause(Scanner scanner) {
        System.out.print("\nPress ENTER to return...");
        scanner.nextLine();
    }

    private void handleJobMarket(Sim activePlayer, Scanner scanner) {
        if (activePlayer instanceof AdultSim) {
            AdultSim adult = (AdultSim) activePlayer;
            System.out.println("\n=== JOB MARKET ===");
            System.out.println("Current Job: " + adult.getCareer().getTitle());
            System.out.println("[0] Quit Current Job (Become Unemployed)");

            Job[] allJobs = Job.values();
            for (int i = 1; i < allJobs.length; i++) {
                Job j = allJobs[i];
                System.out.println("[" + i + "] " + j.getTitle() + " (Start: $" + j.getBaseSalary()
                        + ", Req Age: " + j.getMinAge() + "-" + j.getMaxAge() + ")");
            }
            System.out.println("[-1] Back");
            System.out.print("Select Job> ");
            try {
                int jChoice = Integer.parseInt(scanner.nextLine().trim());
                if (jChoice == -1) {
                    // Do nothing
                } else if (jChoice == 0) {
                    adult.changeJob(Job.UNEMPLOYED);
                } else if (jChoice > 0 && jChoice < allJobs.length) {
                    Job targetJob = allJobs[jChoice];
                    if (adult.getAge() >= targetJob.getMinAge() && adult.getAge() <= targetJob.getMaxAge()) {
                        adult.changeJob(targetJob);
                    } else {
                        System.out.println("You don't meet the age requirements for this job.");
                        pause(scanner);
                    }
                } else {
                    System.out.println("Invalid choice.");
                    pause(scanner);
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input.");
                pause(scanner);
            }
        } else {
            System.out.println("Only adults can access the job market.");
            pause(scanner);
        }
    }

    private CommandResult handleTravel(Sim activePlayer, Scanner scanner, Building currentLocation) {
        List<Building> cityMap = worldManager.getCityMap();
        System.out.println("\nAvailable Locations:");
        for (int i = 0; i < cityMap.size(); i++) {
            System.out.println("[" + (i + 1) + "] " + cityMap.get(i).getName());
        }
        System.out.println("[0] Cancel");
        System.out.print("Select destination> ");

        try {
            int destStr = Integer.parseInt(scanner.nextLine().trim());
            if (destStr == 0) {
                return CommandResult.NO_TICK;
            } else if (destStr > 0 && destStr <= cityMap.size()) {
                Building target = cityMap.get(destStr - 1);
                if (currentLocation == target) {
                    System.out.println("You are already at " + target.getName() + "!");
                    pause(scanner);
                    return CommandResult.NO_TICK;
                } else {
                    worldManager.setCurrentLocation(target);
                    AsciiArt.printTravelAnimation();
                    target.enter(activePlayer);
                    return CommandResult.TICK_FORWARD;
                }
            } else {
                System.out.println("Invalid destination.");
                pause(scanner);
                return CommandResult.NO_TICK;
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid destination.");
            pause(scanner);
            return CommandResult.NO_TICK;
        }
    }

    private void handleMoveRoom(Sim activePlayer, Scanner scanner, Building currentLocation) {
        if (currentLocation instanceof Residential) {
            Residential res = (Residential) currentLocation;
            System.out.println("\n=== MOVE ROOM ===");
            List<Room> rooms = res.getRooms();
            for (int i = 0; i < rooms.size(); i++) {
                System.out.println("[" + (i + 1) + "] " + rooms.get(i).getName());
            }
            System.out.println("[0] Cancel");
            System.out.print("Select Room> ");
            try {
                int rChoice = Integer.parseInt(scanner.nextLine().trim());
                if (rChoice > 0 && rChoice <= rooms.size()) {
                    activePlayer.setCurrentRoom(rooms.get(rChoice - 1));
                    System.out.println("Moved to " + activePlayer.getCurrentRoom().getName() + ".");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input.");
            }
        } else {
            System.out.println("You can only move between rooms at home!");
        }
        pause(scanner);
    }

    private void handleHouseInfo(Building currentLocation, Scanner scanner) {
        if (currentLocation instanceof Residential) {
            Residential res = (Residential) currentLocation;
            System.out.println("\n=== HOUSE INFO: " + res.getName() + " ===");
            for (Room r : res.getRooms()) {
                System.out.println("- " + r.getName() + " (Capacity: " + r.getUsedCapacity() + "/"
                        + r.getMaxCapacity() + ")");
                for (Interactable it : r.getInteractables()) {
                    System.out.println("    => " + it.getObjectName());
                }
            }
            System.out.println("======================================");
        } else {
            System.out.println("You can only inspect residential buildings.");
        }
        pause(scanner);
    }

    private void handleCharacterStatus(Sim activePlayer, Building currentLocation, Scanner scanner) {
        System.out.println("\n=== CHARACTER STATUS ===");
        System.out.println("Name: " + activePlayer.getName());
        System.out.println("Age: " + activePlayer.getAge());
        System.out.println("Money: $" + activePlayer.getMoney());
        if (activePlayer instanceof AdultSim) {
            System.out.println("Current Job Status: " + ((AdultSim) activePlayer).getCareer().getTitle());
        }
        System.out.println("Hunger: " + activePlayer.getHunger().getValue() + " / " + simcli.needs.Need.MAX_VALUE);
        System.out.println("Energy: " + activePlayer.getEnergy().getValue() + " / " + simcli.needs.Need.MAX_VALUE);
        System.out
                .println("Happiness: " + activePlayer.getHappiness().getValue() + " / " + simcli.needs.Need.MAX_VALUE);
        System.out.println(
                "Inventory Items: " + activePlayer.getInventory().size() + " / " + activePlayer.getInventoryCapacity());
        System.out.println("Location: " + currentLocation.getName());
        System.out.println("==================");
        pause(scanner);
    }

    private void handleInventory(Sim activePlayer, Building currentLocation, Scanner scanner)
            throws SleepEventException {
        boolean managingInventory = true;
        int pageSize = 10;
        int currentPage = 0;

        while (managingInventory) {
            List<simcli.entities.Item> inv = activePlayer.getInventory();
            int totalPages = (int) Math.ceil((double) inv.size() / pageSize);
            if (totalPages == 0)
                totalPages = 1;
            if (currentPage >= totalPages)
                currentPage = totalPages - 1;

            System.out.println("\n=== INVENTORY (Page " + (currentPage + 1) + " of " + totalPages + ") ===");
            System.out.println("Capacity: " + inv.size() + " / " + activePlayer.getInventoryCapacity());

            if (inv.isEmpty()) {
                System.out.println("Your inventory is empty.");
                managingInventory = false;
                break;
            }

            int startIdx = currentPage * pageSize;
            int endIdx = Math.min(startIdx + pageSize, inv.size());

            for (int i = startIdx; i < endIdx; i++) {
                System.out.println("[" + (i - startIdx + 1) + "] " + inv.get(i).getObjectName());
            }

            System.out.println("\n[N] Next Page   [P] Previous Page");
            System.out.println("[0] Back");
            System.out.print("Select item to Use/Place> ");

            String invInput = scanner.nextLine().trim().toUpperCase();

            if (invInput.equals("0")) {
                managingInventory = false;
            } else if (invInput.equals("N")) {
                if (currentPage < totalPages - 1)
                    currentPage++;
            } else if (invInput.equals("P")) {
                if (currentPage > 0)
                    currentPage--;
            } else {
                try {
                    int invChoice = Integer.parseInt(invInput);
                    if (invChoice > 0 && invChoice <= (endIdx - startIdx)) {
                        int realIndex = startIdx + invChoice - 1;
                        simcli.entities.Item selectedItem = inv.get(realIndex);

                        if (selectedItem instanceof simcli.entities.Furniture
                                && currentLocation instanceof Residential) {
                            simcli.entities.Furniture furn = (simcli.entities.Furniture) selectedItem;
                            Residential res = (Residential) currentLocation;
                            System.out.println("Select a room to place " + furn.getObjectName() + " (Requires "
                                    + furn.getSpaceScore() + " space):");
                            List<Room> rooms = res.getRooms();
                            for (int i = 0; i < rooms.size(); i++) {
                                Room r = rooms.get(i);
                                System.out.println("[" + (i + 1) + "] " + r.getName() + " (Capacity: "
                                        + r.getUsedCapacity() + "/" + r.getMaxCapacity() + ")");
                            }
                            System.out.println("[0] Cancel");
                            System.out.print("Room> ");
                            int rChoice = Integer.parseInt(scanner.nextLine().trim());
                            if (rChoice > 0 && rChoice <= rooms.size()) {
                                Room targetRoom = rooms.get(rChoice - 1);
                                if (targetRoom != activePlayer.getCurrentRoom()) {
                                    System.out.println("You can only place furniture in the room you are currently in!");
                                } else if (targetRoom.canFit(furn)) {
                                    Interactable instance = null;
                                    switch (furn.getObjectName()) {
                                        case "Bed":
                                            instance = new simcli.world.interactables.Bed();
                                            break;
                                        case "Computer":
                                            instance = new simcli.world.interactables.Computer();
                                            break;
                                        case "Fridge":
                                            instance = new simcli.world.interactables.Fridge();
                                            break;
                                        case "Weight Bench":
                                            instance = new simcli.world.interactables.WeightBench();
                                            break;
                                        case "Shower":
                                            instance = new simcli.world.interactables.Shower();
                                            break;
                                        case "Storage Chest":
                                            instance = new simcli.world.interactables.StorageChest();
                                            break;
                                    }
                                    if (instance != null) {
                                        targetRoom.placeFurniture(instance, furn.getSpaceScore());
                                        activePlayer.getInventory().remove(selectedItem);
                                        System.out.println("Placed " + furn.getObjectName() + " in "
                                                + targetRoom.getName() + "!");
                                    }
                                } else {
                                    System.out.println("Not enough space in " + targetRoom.getName() + "!");
                                }
                            }
                        } else {
                            try {
                                selectedItem.interact(activePlayer, scanner, timeManager);
                            } catch (SleepEventException e) {
                                throw e; // rethrow to be caught by handle
                            } catch (SimulationException e) {
                                System.err.println("ACTION REJECTED: " + e.getMessage());
                            }
                        }
                    } else {
                        System.out.println("Invalid selection.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input.");
                } catch (SleepEventException e) {
                    throw e; // properly rethrow to outer
                }
            }
        }
        pause(scanner);
    }

    private void handleUpgradeRoom(Sim activePlayer, Building currentLocation, Scanner scanner) {
        if (currentLocation instanceof Residential) {
            Residential res = (Residential) currentLocation;
            System.out.println("\n=== UPGRADE ROOM ===");
            System.out.println("Cost: $500 for +20 Capacity");
            System.out.println("Your Cash: $" + activePlayer.getMoney());
            List<Room> rooms = res.getRooms();
            for (int i = 0; i < rooms.size(); i++) {
                Room r = rooms.get(i);
                System.out.println("[" + (i + 1) + "] " + r.getName() + " (Capacity: " + r.getUsedCapacity()
                        + "/" + r.getMaxCapacity() + ")");
            }
            System.out.println("[0] Cancel");
            System.out.print("Room> ");
            try {
                int rChoice = Integer.parseInt(scanner.nextLine().trim());
                if (rChoice > 0 && rChoice <= rooms.size()) {
                    Room targetRoom = rooms.get(rChoice - 1);
                    targetRoom.upgradeCapacity(activePlayer, 20, 500);
                }
            } catch (Exception e) {
                System.out.println("Invalid selection.");
            }
        } else {
            System.out.println("You can only upgrade rooms at home!");
        }
        pause(scanner);
    }
}
