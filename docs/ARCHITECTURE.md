# Sims CLI Game — Architecture & Core Logic

## 1. Core Systems & High-Level Flow
The game is built around heavily decoupled systems using standard Object-Oriented patterns. 
*   **`Main.java`**: The entry point that delegates control entirely to `MainMenu`.
*   **`GameEngine.java`**: The central orchestrator running the infinite `while (!isGameOver())` loop. It acts as the "glue" connecting the world, the player, and time.
*   **`TimeManager.java`**: Responsible for tracking ticks (1 tick = 1 in-game hour), determining day/night cycles, and advancing the calendar (days).
*   **`WorldManager.java`**: Initializes, populates, and holds the `CityMap`.

## 2. Command Pattern (Input Routing)
To avoid having massive, unreadable `switch` statements inside the main game loop, user input is routed through **`InputHandler.java`** using the **Command Pattern**.

1.  `InputHandler` reads the global hotkey typed by the user (e.g., `'W'` for Work, `'T'` for Travel).
2.  It instantiates a specific command class implementing `ICommand` (e.g., `new WorkCommand(activePlayer, scanner, timeManager)`).
3.  `CommandResult execute()` is called on that class, which securely encapsulates all logic, validations, and UI output for that specific action.
4.  The command returns an enum (`CommandResult`):
    *   `TICK_FORWARD`: Advances time by 1 hour and decays needs.
    *   `NO_TICK`: The action was cancelled, invalid, or simply a status check (e.g., checking inventory).
    *   `SLEEP_EVENT`: Fast-forwards time automatically to the next morning.
    *   `GAME_OVER`: Signals the engine to break the loop.

## 3. Entity Composition (The Sim Class)
Instead of putting a thousand lines of code directly into the **`Sim`** class, `Sim.java` acts as a **Facade**, delegating heavy work to focused component managers:
*   **`NeedsTracker`**: Holds and decays the base attributes (`Hunger`, `Energy`, `Hygiene`, `Fun`, `Social`).
*   **`CareerProfile`**: Tracks `Job` enums, daily shifts, max limits, and truancy counter.
*   **`InventoryManager`**: Handles capacity limits and backpack storage.
*   **`SkillManager`**: Future expansion module for handling specific proficiency levels.

## 4. State Pattern (Lifecycles & Needs)
The game utilizes the **State Pattern** to dynamically change how the `Sim` reacts to the passage of time without relying on massive `if/else` checks:
*   **Life Stages (`ChildStage`, `TeenStage`, `AdultStage`, `ElderStage`)**: 
    *   Each stage implements the `LifeStage` interface.
    *   The interface dictates if the Sim `canWork()` and what their `getEnergyDecayModifier()` is (e.g., Teens lose energy faster, Elders lose it slower). 
    *   The `getNextStage()` method automatically dictates the contiguous transition.
*   **Sim Health States (`SimState`)**: 
    *   Managed automatically inside `NeedsTracker.updateState()`. It evaluates the lowest need value (`EXCELLENT` > `GOOD` > `POOR`) and handles critical thresholds.
    *   Triggering `STARVING` initiates a countdown that eventually pushes the state to `DEAD`.

## 5. The World & Real Estate Hierarchy
The physical map is split using inheritance to ensure Sims cannot do residential actions (like sleeping in beds or upgrading capacity) in public stores:
*   **`Building.java` (Abstract)**: The base class defining entry/exit logs, names, and a default `isResidential() { return false; }`.
*   **`Residential.java`**: Inherits `Building`, overriding `isResidential()` to `true`. This adds a list of `Room` objects, maximum occupancies, and property ownership/purchasing (`isOwned`).
*   **`Commercial.java`**: Stores mapping to specific commercial jobs or shops (e.g., "The Supermarket").

---
**How to use this document for debugging:**
*   If a menu option fails to read input correctly -> Check `InputHandler.java`.
*   If an action happens but time doesn't sync properly -> Check what `CommandResult` the specific `Command` execution is returning.
*   If you find yourself writing `if (sim.getAge() > 65)` somewhere in the code -> **Stop!** That logic belongs inside the returning `LifeStage` class!
