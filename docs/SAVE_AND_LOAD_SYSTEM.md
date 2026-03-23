# Deep Dive: Save & Load Persistence System

## 1. Overview
The Sims CLI game implements a custom text-based serialization system handled entirely by **`SaveManager.java`**. Instead of relying on binary Java serialization or external database dependencies, the game writes standard `.txt` files to a local `saves/` directory. 

This approach was chosen specifically because it makes the save files **human-readable** and **easily modifiable** (which is incredibly useful for manual testing of edge cases, like forcefully changing a Sim's age to 64 to test the Elder transition).

## 2. Serialization (Saving State)
When the user triggers a save, the `GameEngine` passes a reference of itself to `SaveManager.saveGame()`. The manager uses a `PrintWriter` to flush the data to disk sequentially:
1.  **Engine Scope Data**: It records the `WORLD` name, the current `TICK` count, and the `GAME_OVER` boolean.
2.  **Summary Stats**: If the game ended, it records the final `STATS_MONEY` and `STATS_ITEMS`.
3.  **Entity Scope Data (`neighborhood`)**: The method loops through the engine's list of active Sims. Every `Sim` object is serialized into a single, dense comma-separated string containing their state.

### The Sim Array Format
`Sim:[Name],[Age],[Gender],[Job],[Money],[InvCapacity],[Hunger],[Energy],[Fun],[Hygiene],[Social],[DaysAlive]`

*(Note: In the current build, advanced metadata such as Personality Traits, Household relationships/Spouses, and Customized World States are not serialized, meaning they refresh to defaults upon loading).*

## 3. Deserialization (Loading State)
Loading a world relies on `BufferedReader` sweeping the file line by line.
*   **Prefix Parsing**: The system checks the start of each line (`if (line.startsWith("WORLD:"))`) to determine what data it is reading.
*   **Tokenization**: When it finds a `Sim:` line, it strips the prefix and uses `String.split(",")` to break the string into a `data[]` array.
*   **Reconstruction**: 
    *   It parses the strings back into Primitives and Enums (using `Integer.parseInt()` and `Job.valueOf()`).
    *   It instantiates the base `Sim` object.
    *   It forcefully injects the encapsulated values directly into the managers (e.g., `sim.getHunger().setValue(Integer.parseInt(data[6]))`).
*   **State Alignment**: Immediately after injecting the raw Need values, `SaveManager` calls `sim.updateState()`. This forces the newly loaded `NeedsTracker` to instantly evaluate the Health bracket (`GOOD`, `POOR`, `STARVING`) before the game loop even resumes.

## 4. Error Handling & Robustness
The persistence system is designed to fail gracefully:
*   **Directory Checks**: `SaveManager.checkDirectory()` is called before any I/O operation. If the `saves/` folder was deleted, the game recreates it dynamically rather than throwing a `FileNotFoundException`.
*   **Backward Compatibility**: As new modules are added, save strings lengthen. The loading logic protects against array index exceptions by checking bounds dynamically (e.g., `if (data.length > 11)`). If an older save file lacks a field (like `DaysAlive`), the loader gracefully skips it and relies on the default constructor value.
*   **Corrupt Saves**: If an invalid/outdated variable is read (e.g., trying to parse a retired Job enum), an `Exception` is thrown. The overarching `try-catch` block intercepts it, logs a clean warning via the `UIManager`, and returns the player safely to the `MainMenu` instead of crashing the JVM.
