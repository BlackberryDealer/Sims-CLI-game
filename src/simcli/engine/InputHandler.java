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
                if (activePlayer instanceof AdultSim) {
                    AdultSim adult = (AdultSim) activePlayer;
                    if (adult.getShiftsWorkedToday() >= 1 && !adult.hasWarnedAboutOverwork()) {
                        simcli.ui.UIManager.printWarning("Working multiple shifts in a single day drains stats significantly faster!");
                        simcli.ui.UIManager.prompt("Are you sure you want to overwork? (Y/N)> ");
                        String conf = scanner.nextLine().trim();
                        adult.setWarnedAboutOverwork(true);
                        if (!conf.equalsIgnoreCase("Y")) {
                            simcli.ui.UIManager.printMessage("Work action cancelled.");
                            return CommandResult.NO_TICK;
                        }
                    }
                    adult.performActivity("Work");
                    timeManager.advanceTicks(adult.getCareer().getWorkingHours() - 1);
                } else {
                    activePlayer.performActivity("Work");
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
                    simcli.ui.UIManager.printMessage("Invalid item choice.");
                    pause(scanner);
                    return CommandResult.NO_TICK;
                }
            }
        } catch (SleepEventException e) {
            return CommandResult.SLEEP_EVENT;
        } catch (SimulationException e) {
            simcli.ui.UIManager.printWarning("ACTION REJECTED: " + e.getMessage());
            pause(scanner);
            return CommandResult.NO_TICK;
        } catch (NumberFormatException e) {
            simcli.ui.UIManager.printWarning("Invalid input.");
            pause(scanner);
            return CommandResult.NO_TICK;
        }
    }

    private void pause(Scanner scanner) {
        simcli.ui.UIManager.prompt("\nPress ENTER to return...");
        scanner.nextLine();
    }

    private void handleJobMarket(Sim activePlayer, Scanner scanner) {
        if (activePlayer instanceof AdultSim) {
            AdultSim adult = (AdultSim) activePlayer;
            simcli.ui.UIManager.printMessage("\n=== JOB MARKET ===");
            simcli.ui.UIManager.printMessage("Current Job: " + adult.getCareer().getTitle());
            simcli.ui.UIManager.printMessage("[0] Quit Current Job (Become Unemployed)");

            Job[] allJobs = Job.values();
            for (int i = 1; i < allJobs.length; i++) {
                Job j = allJobs[i];
                simcli.ui.UIManager.printMessage("[" + i + "] " + j.getTitle() + " (Start: $" + j.getBaseSalary()
                        + ", Req Age: " + j.getMinAge() + "-" + j.getMaxAge() + ")");
            }
            simcli.ui.UIManager.printMessage("[-1] Back");
            simcli.ui.UIManager.prompt("Select Job> ");
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
                        simcli.ui.UIManager.printMessage("You don't meet the age requirements for this job.");
                        pause(scanner);
                    }
                } else {
                    simcli.ui.UIManager.printMessage("Invalid choice.");
                    pause(scanner);
                }
            } catch (NumberFormatException e) {
                simcli.ui.UIManager.printMessage("Invalid input.");
                pause(scanner);
            }
        } else {
            simcli.ui.UIManager.printMessage("Only adults can access the job market.");
            pause(scanner);
        }
    }

    private CommandResult handleTravel(Sim activePlayer, Scanner scanner, Building currentLocation) {
        List<Building> cityMap = worldManager.getCityMap();
        simcli.ui.UIManager.printMessage("\nAvailable Locations:");
        for (int i = 0; i < cityMap.size(); i++) {
            simcli.ui.UIManager.printMessage("[" + (i + 1) + "] " + cityMap.get(i).getName());
        }
        simcli.ui.UIManager.printMessage("[0] Cancel");
        simcli.ui.UIManager.prompt("Select destination> ");

        try {
            int destStr = Integer.parseInt(scanner.nextLine().trim());
            if (destStr == 0) {
                return CommandResult.NO_TICK;
            } else if (destStr > 0 && destStr <= cityMap.size()) {
                Building target = cityMap.get(destStr - 1);
                if (currentLocation == target) {
                    simcli.ui.UIManager.printMessage("You are already at " + target.getName() + "!");
                    pause(scanner);
                    return CommandResult.NO_TICK;
                } else {
                    worldManager.setCurrentLocation(target);
                    AsciiArt.printTravelAnimation();
                    target.enter(activePlayer);
                    return CommandResult.TICK_FORWARD;
                }
            } else {
                simcli.ui.UIManager.printMessage("Invalid destination.");
                pause(scanner);
                return CommandResult.NO_TICK;
            }
        } catch (NumberFormatException e) {
            simcli.ui.UIManager.printMessage("Invalid destination.");
            pause(scanner);
            return CommandResult.NO_TICK;
        }
    }

    private void handleMoveRoom(Sim activePlayer, Scanner scanner, Building currentLocation) {
        if (currentLocation instanceof Residential) {
            Residential res = (Residential) currentLocation;
            simcli.ui.UIManager.printMessage("\n=== MOVE ROOM ===");
            List<Room> rooms = res.getRooms();
            for (int i = 0; i < rooms.size(); i++) {
                simcli.ui.UIManager.printMessage("[" + (i + 1) + "] " + rooms.get(i).getName());
            }
            simcli.ui.UIManager.printMessage("[0] Cancel");
            simcli.ui.UIManager.prompt("Select Room> ");
            try {
                int rChoice = Integer.parseInt(scanner.nextLine().trim());
                if (rChoice > 0 && rChoice <= rooms.size()) {
                    activePlayer.setCurrentRoom(rooms.get(rChoice - 1));
                    simcli.ui.UIManager.printMessage("Moved to " + activePlayer.getCurrentRoom().getName() + ".");
                }
            } catch (NumberFormatException e) {
                simcli.ui.UIManager.printMessage("Invalid input.");
            }
        } else {
            simcli.ui.UIManager.printMessage("You can only move between rooms at home!");
        }
        pause(scanner);
    }

    private void handleHouseInfo(Building currentLocation, Scanner scanner) {
        if (currentLocation instanceof Residential) {
            Residential res = (Residential) currentLocation;
            simcli.ui.UIManager.printMessage("\n=== HOUSE INFO: " + res.getName() + " ===");
            for (Room r : res.getRooms()) {
                simcli.ui.UIManager.printMessage("- " + r.getName() + " (Capacity: " + r.getUsedCapacity() + "/"
                        + r.getMaxCapacity() + ")");
                for (Interactable it : r.getInteractables()) {
                    simcli.ui.UIManager.printMessage("    => " + it.getObjectName());
                }
            }
            simcli.ui.UIManager.printMessage("======================================");
        } else {
            simcli.ui.UIManager.printMessage("You can only inspect residential buildings.");
        }
        pause(scanner);
    }

    private void handleCharacterStatus(Sim activePlayer, Building currentLocation, Scanner scanner) {
        simcli.ui.UIManager.printMessage("\n=== CHARACTER STATUS ===");
        simcli.ui.UIManager.printMessage("Name: " + activePlayer.getName());
        simcli.ui.UIManager.printMessage("Age: " + activePlayer.getAge());
        simcli.ui.UIManager.printMessage("Money: $" + activePlayer.getMoney());
        if (activePlayer instanceof AdultSim) {
            simcli.ui.UIManager.printMessage("Current Job Status: " + ((AdultSim) activePlayer).getCareer().getTitle());
        }
        simcli.ui.UIManager.printMessage("Hunger: " + activePlayer.getHunger().getValue() + " / " + simcli.needs.Need.MAX_VALUE);
        simcli.ui.UIManager.printMessage("Energy: " + activePlayer.getEnergy().getValue() + " / " + simcli.needs.Need.MAX_VALUE);
        System.out
                .println("Happiness: " + activePlayer.getHappiness().getValue() + " / " + simcli.needs.Need.MAX_VALUE);
        simcli.ui.UIManager.printMessage(
                "Inventory Items: " + activePlayer.getInventory().size() + " / " + activePlayer.getInventoryCapacity());
        simcli.ui.UIManager.printMessage("Location: " + currentLocation.getName());
        simcli.ui.UIManager.printMessage("==================");
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

            simcli.ui.UIManager.printMessage("\n=== INVENTORY (Page " + (currentPage + 1) + " of " + totalPages + ") ===");
            simcli.ui.UIManager.printMessage("Capacity: " + inv.size() + " / " + activePlayer.getInventoryCapacity());

            if (inv.isEmpty()) {
                simcli.ui.UIManager.printMessage("Your inventory is empty.");
                managingInventory = false;
                break;
            }

            int startIdx = currentPage * pageSize;
            int endIdx = Math.min(startIdx + pageSize, inv.size());

            for (int i = startIdx; i < endIdx; i++) {
                simcli.ui.UIManager.printMessage("[" + (i - startIdx + 1) + "] " + inv.get(i).getObjectName());
            }

            simcli.ui.UIManager.printMessage("\n[N] Next Page   [P] Previous Page");
            simcli.ui.UIManager.printMessage("[0] Back");
            simcli.ui.UIManager.prompt("Select item to Use/Place> ");

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
                            simcli.ui.UIManager.printMessage("Select a room to place " + furn.getObjectName() + " (Requires "
                                    + furn.getSpaceScore() + " space):");
                            List<Room> rooms = res.getRooms();
                            for (int i = 0; i < rooms.size(); i++) {
                                Room r = rooms.get(i);
                                simcli.ui.UIManager.printMessage("[" + (i + 1) + "] " + r.getName() + " (Capacity: "
                                        + r.getUsedCapacity() + "/" + r.getMaxCapacity() + ")");
                            }
                            simcli.ui.UIManager.printMessage("[0] Cancel");
                            simcli.ui.UIManager.prompt("Room> ");
                            int rChoice = Integer.parseInt(scanner.nextLine().trim());
                            if (rChoice > 0 && rChoice <= rooms.size()) {
                                Room targetRoom = rooms.get(rChoice - 1);
                                if (targetRoom != activePlayer.getCurrentRoom()) {
                                    simcli.ui.UIManager.printMessage("You can only place furniture in the room you are currently in!");
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
                                        simcli.ui.UIManager.printMessage("Placed " + furn.getObjectName() + " in "
                                                + targetRoom.getName() + "!");
                                    }
                                } else {
                                    simcli.ui.UIManager.printMessage("Not enough space in " + targetRoom.getName() + "!");
                                }
                            }
                        } else {
                            try {
                                selectedItem.interact(activePlayer, scanner, timeManager);
                            } catch (SleepEventException e) {
                                throw e; // rethrow to be caught by handle
                            } catch (SimulationException e) {
                                simcli.ui.UIManager.printWarning("ACTION REJECTED: " + e.getMessage());
                            }
                        }
                    } else {
                        simcli.ui.UIManager.printMessage("Invalid selection.");
                    }
                } catch (NumberFormatException e) {
                    simcli.ui.UIManager.printMessage("Invalid input.");
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
            simcli.ui.UIManager.printMessage("\n=== UPGRADE ROOM ===");
            simcli.ui.UIManager.printMessage("Cost: $500 for +20 Capacity");
            simcli.ui.UIManager.printMessage("Your Cash: $" + activePlayer.getMoney());
            List<Room> rooms = res.getRooms();
            for (int i = 0; i < rooms.size(); i++) {
                Room r = rooms.get(i);
                simcli.ui.UIManager.printMessage("[" + (i + 1) + "] " + r.getName() + " (Capacity: " + r.getUsedCapacity()
                        + "/" + r.getMaxCapacity() + ")");
            }
            simcli.ui.UIManager.printMessage("[0] Cancel");
            simcli.ui.UIManager.prompt("Room> ");
            try {
                int rChoice = Integer.parseInt(scanner.nextLine().trim());
                if (rChoice > 0 && rChoice <= rooms.size()) {
                    Room targetRoom = rooms.get(rChoice - 1);
                    targetRoom.upgradeCapacity(activePlayer, 20, 500);
                }
            } catch (Exception e) {
                simcli.ui.UIManager.printMessage("Invalid selection.");
            }
        } else {
            simcli.ui.UIManager.printMessage("You can only upgrade rooms at home!");
        }
        pause(scanner);
    }
}
