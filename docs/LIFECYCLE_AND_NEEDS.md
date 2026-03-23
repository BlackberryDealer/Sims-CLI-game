# Deep Dive: Lifecycle & Needs System

## 1. Overview
The Sims CLI game tracks what a character physically feels (Needs) and their ongoing age trajectory (Lifecycle). These two concepts heavily utilize the **State Pattern** to dynamically change rules, permissions, and math modifiers without cluttering the main `Sim` class with endless `if/else` checks.

## 2. The Needs Tracker (Component Pattern)
To keep the `Sim` class clean, all biological tracking is extracted into the `NeedsTracker` component.
*   **The `Need` Class**: A base class tracking an integer value from `0` to `100 MAX_VALUE`.
*   **Need Types**: `Hunger`, `Energy`, `Fun`, `Hygiene`, and `Social`.
*   **Decay Physics**: Every in-game hour (tick), the `TimeManager` tells the `NeedsTracker` to decay. 
    *   `Hunger` and `Hygiene` decay fast (-5 per tick).
    *   `Energy` decays slowly (-2 per tick).
    *   Values are clamped cleanly so they never drop below `0` or exceed `100`.

## 3. The State Pattern: Health & State (`SimState`)
A Sim's immediate well-being is defined by the `SimState` enum (`EXCELLENT`, `GOOD`, `POOR`, `STARVING`, `DEAD`). 
*   **Dynamic Evaluation**: After every tick, `NeedsTracker.updateState()` sweeps all 5 needs and finds the **lowest** value. 
    *   If the lowest need is > 70 = `EXCELLENT`.
    *   If the lowest need is < 30 = `POOR`.
*   **Critical Thresholds**: Need values directly enforce survival constraints. If `Hunger` touches `0`, normal state math is ignored, and the state is forcefully overwritten to `STARVING`. If a Sim remains in `STARVING` too long, the state transitions to `DEAD` and triggers a Game Over.

## 4. The State Pattern: Aging (`LifeStage`)
Rather than hardcoding rules like `if (age < 18)`, the game uses polymorphic `LifeStage` objects.
*   **The Interface**: Every stage implements `LifeStage`, providing specific rules:
    1.  `canWork()`: Determines job market eligibility.
    2.  `getEnergyDecayModifier()`: Tweaks the base decay rate of the `Energy` need.
    3.  `getNextStage(int age)`: Handles transitions logically.
*   **The Stages**:
    *   `ChildStage` (Ages 0-12): Returns `canWork() = false`.
    *   `TeenStage` (Ages 13-17): Energy decays 1.2x faster due to adolescent metabolism. Still cannot work.
    *   `AdultStage` (Ages 18-64): Returns `canWork() = true`. Baseline energy decay (1.0).
    *   `ElderStage` (Ages 65+): Automatically retires the Sim. Returns `canWork() = false`. Energy decays slower (0.8x modifier) simulating a slower-paced lifestyle.

## 5. Birthday Math
When a Sim is loaded, `daysAlive` counts upward at the stroke of midnight. When `daysAlive % 3 == 0`, a birthday event triggers, incrementing `age` by 1. 

The `Sim` simply passes its new `age` integer into `currentStage.getNextStage(age)`. If the number breaks a threshold (like turning 65), the `AdultStage` object destroys itself and returns a new `ElderStage` object, permanently changing how the Sim interacts with the world for the rest of the game!
