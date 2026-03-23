# Deep Dive: Core Engine, Time, & Entities

## 1. The Game Engine Loop
The heartbeat of the entire application is housed inside `GameEngine.java`. When the `MainMenu` passes control over to a loaded or generated world, the `engine.run()` method takes over.

This is structured as an **idempotent infinite loop**:
```java
while (!isGameOver) {
    // 1. Display Current Status (Time, Location, Needs)
    // 2. Prompt for User Input
    // 3. Evaluate Command Result
}
```
The game will sit idle indefinitely waiting for input. The environment does not "tick" in real-time (like a timer); it is entirely **turn-based**. Every action is evaluated, and the engine only moves forward when a command specifically returns a flag telling time to advance.

## 2. Time Management (`TimeManager`)
Time is handled strictly via **ticks**, where 1 tick = 1 in-game hour.
*   **Day/Night Cycle Math**: `TimeManager` handles all modulo math. The `getCurrentDay()` translates raw ticks into days (e.g., `tick 25` = `Day 2`).
*   **Time String Translation**: A helper method translates the modulo to a 24-hour clock (e.g., `tick 14` -> `14:00`), allowing the UI to present human-readable times dynamically.
*   **Sleep Mechanics**: Sleeping acts as a time-skip wrapper. Instead of instantly jumping the clock forward, sleeping sets a target tick (e.g., `08:00` the next morning) and then repeatedly calls `advanceTick()` in a rapid `while` loop, allowing needs to drain naturally over the skipped timeframe.

## 3. Entity Encapsulation (The Sim Facade)
A core Object-Oriented principle heavily applied in the game is using `Sim.java` as a **Facade**. 

Rather than writing thousands of lines of logic directly into the `Sim` file, the character acts as a central coordinator pointing to specialized "Managers" (components):
1.  **`CareerProfile` Component**: Tracks job history, checks if the office is closed (weekends), tracks missed shifts (Truancy), and handles salary calculations based on promotion tiers.
2.  **`InventoryManager` Component**: Tracks backpack slots. It enforces strict capacity constraints, meaning interactions fail dynamically if a Sim runs out of space without breaking the general item system.
3.  **`NeedsTracker` Component**: Tracks biological requirements (Hunger, Energy).

When the engine calls `activePlayer.tick()`, the `Sim` quietly delegates the math down into its components, passing along any relevant modifiers (like Age or Traits) to ensure calculations stay decoupled.

## 4. World Geometry (`WorldManager` & Properties)
The physical map of the game is fixed array-driven graph known as the `CityMap`.
*   **Initialization**: When a new game starts, `WorldManager.setupWorld()` executes. It manually instantiates the core buildings (`The Shared Dorm`, `The Bungalow`, `Town Supermarket`, `The Bookshop`) and injects their default parameters.
*   **Rooms & Interactions**: Residential properties dynamically generate `Room` objects (Kitchen, Bedroom, Living Room), and instantiate static `Interactable` objects (like Beds or Fridges) locking them to specific physical locations.
*   **Location Awareness**: The engine constantly tracks `currentLocation`. Travel commands simply swap this pointer. All interaction logic verifies this pointer before execution (e.g., preventing a player from cooking food if they are standing in a bookstore).
