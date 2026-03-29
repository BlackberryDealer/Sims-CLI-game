# 🏠 SimCLI — A Sims-Inspired CLI Simulation Game

> A turn-based life simulation game built entirely in Java, played through the terminal. Manage a household of Sims — feed them, find them careers, build relationships, raise children, and try to keep everyone alive.

---

## 📖 Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Getting Started](#getting-started)
- [How to Play](#how-to-play)
- [Architecture & Design Patterns](#architecture--design-patterns)
- [Project Structure](#project-structure)
- [Testing](#testing)
- [Contributors](#contributors)

---

## Overview

**SimCLI** is a Java Object-Oriented Programming project that recreates the core loop of *The Sims* as a command-line experience. Players create a household of up to 4 Sims, navigate a small city (home, shops, park), manage five decaying needs, pursue careers with promotions, build relationships with NPCs, get married, have children, and watch their Sims age through four life stages — all rendered via ASCII art in the terminal.

The project emphasises clean OOP architecture, making heavy use of established design patterns including **Command**, **State**, **Facade**, **Builder**, and **Strategy**.

---

## Features

### 🧑 Sim Management
- Create up to **4 Sims** per household with custom names, ages, and genders
- Each Sim is assigned a **random personality trait** (Active, Lazy, Socialite) that modifies gameplay
- **5 decaying needs**: Hunger, Energy, Hygiene, Happiness, Social — each with unique decay rules and cross-penalties
- **Health system**: needs reaching critical levels cause health damage; starvation for 3+ consecutive ticks is fatal

### 💼 Career System
- **4 careers**: Software Engineer, Hardware Technician, Freelance Photographer, Personal Trainer
- Each job has unique salary, energy drain, working hours, age limits, and up to **5 promotion tiers**
- **25% promotion chance** per shift — salary scales with a per-career multiplier
- Overwork penalties and forced retirement when a Sim exceeds the job's maximum age

### 🏙️ World & Locations
| Location | Type | Interactables |
|---|---|---|
| The Shared Dorm | Residential (free) | Bed, Fridge, Shower, Weight Bench |
| The Bungalow | Residential ($5,000) | Bed, Fridge, Shower, Computer |
| Town Supermarket | Commercial | Grocery Shelf (buy food) |
| The Bookshop | Commercial | Bookshop Shelf (buy books) |
| City Park | Park | Park Bench (socialize with NPCs) |

- **Room system** — residential buildings contain rooms (Bedroom, Kitchen, Garage, Bathroom), each with furniture capacity
- **Room upgrades** — expand storage capacity for $500
- **Property purchasing** — buy the Bungalow to upgrade your living situation

### 💕 Relationships & Family
- **Social actions**: Chat, Joke, Argue, Flirt — each with distinct stat effects
- **Relationship progression**: Stranger → Acquaintance → Friend → Romantic → Married
- **Marriage** at 100 relationship score; spouse interactions include gift-giving and reproduction
- **Children**: 50% conception chance, born as non-playable infants who age into teens and become playable members of the household
- **NPC pool** of up to 8 park visitors to socialize and build relationships with

### 🎂 Lifecycle & Aging
- **4 life stages** via the State Pattern: Child → Teen (13) → Adult (18) → Elder (65)
- Sims age 1 year every **3 in-game days**
- **Elder penalties**: increased energy decay, forced job retirement
- **Retirement pension**: $100/age-tick for unemployed elders
- **Death by old age** at 90 years

### 🎲 Random Events
Each tick has a 5% chance to trigger one of six events:
- 💰 Found money (+$50)
- 🦶 Stubbed toe (happiness −15)
- 💡 Stroke of inspiration (energy +20)
- 🤧 Mild cold (all needs −10)
- 🔥 Stove fire (hunger −20, energy −25, health −10)
- 🎰 Lottery win (+$100–500, happiness +30)

### 💾 Save System
- **Text-based persistence** — saves to `saves/<worldName>.txt`
- Full state serialization: Sim stats, inventory, relationships, spouses, children, building ownership, room states, world position
- **Autosave** every 10 ticks
- Create, load, and delete saves from the main menu

### 🎨 ASCII Art Rendering
- Dynamic ASCII art driven by an extensible provider system
- Contextual rendering based on location and Sim action state

---

## Getting Started

### Prerequisites
- **Java JDK 8+** (tested with JDK 17+)
- Windows OS (batch scripts provided)

### Run the Game

**Option 1 — Using the batch script:**
```bash
run.bat
```

**Option 2 — Manual compilation:**
```bash
# Compile
javac -d bin -sourcepath src src/simcli/Main.java

# Run
java -cp bin simcli.Main
```

### Run Tests
```bash
run-tests.bat
```
This compiles JUnit 5 tests from the `test/` directory and runs them using the standalone JUnit Platform Console launcher located in `lib/`.

### Generate Javadoc
```bash
javadoc -d docs -sourcepath src -subpackages simcli
```
This generates the full API documentation into the `docs/` directory. Open `docs/index.html` in a browser to browse the Javadoc.

---

## How to Play

### Main Menu
```
[1] Create New World    — Start a new game with a fresh household
[2] Load Existing World — Resume a previously saved game
[3] Delete Saved World  — Permanently remove a save file
[4] Exit Game           — Quit the application
```

### In-Game Commands
| Key | Action |
|-----|--------|
| `W` | Go to work (earn money, risk overwork) |
| `J` | Browse the job market / change career |
| `T` | Travel to another location |
| `M` | Move to a different room (residential only) |
| `H` | View house info and room details |
| `I` | View character status and detailed stats |
| `V` | Open inventory (eat food, use/drop items) |
| `U` | Upgrade room storage capacity ($500) |
| `K` | Switch active Sim to another household member |
| `L` | Spouse interactions (gift, reproduce) |
| `S` | Save and exit to main menu |
| `1-9` | Interact with numbered objects in the current location |

### Gameplay Tips
- **Keep hunger above 20** — dropping to zero starts a 3-tick death countdown
- **Sleep regularly** — sleeping fast-forwards time to 8 AM and restores energy
- **Socialize at the park** — the Socialite trait gives bonus relationship and happiness gains
- **Save up for the Bungalow** ($5,000) — it comes with a Computer for extra interactions
- **Watch your Sim's age** — elders are forced into retirement and have increased energy decay

---

## Architecture & Design Patterns

### 🏗️ Design Patterns Used

| Pattern | Where | Purpose |
|---------|-------|---------|
| **Command** | `engine/commands/` | Each player action (Work, Travel, Interact, etc.) is encapsulated as an `ICommand` object, dispatched polymorphically by `InputHandler` |
| **State** | `entities/lifecycle/` | `LifeStage` interface with `ChildStage`, `TeenStage`, `AdultStage`, `ElderStage` — Sims swap behaviour on age-up transitions |
| **Facade** | `GameEngine` | Single entry point for the game loop, hiding subsystem complexity (TimeManager, WorldManager, GameLoop, etc.) |
| **Builder** | `CommandContext.Builder` | Fluent construction of the shared context object passed to all commands — validates required fields at build time |
| **Strategy** | `Need` subclasses | Each need type (Hunger, Energy, Hygiene, etc.) defines its own `calculateDecay()` algorithm via abstract method override |
| **Template Method** | `BaseCommand` | Provides shared validation and execution scaffolding, with subclasses overriding specific steps |
| **Factory** | `InteractableFactory` | Creates interactable objects for rooms and buildings |
| **Dependency Injection** | Throughout | `IRenderer`, `IWorldManager`, `IInputHandler` interfaces allow constructor injection of mocks for testing |

### 🧩 Key Architectural Decisions
- **No circular dependencies** — commands receive a `CommandContext` instead of a `GameEngine` reference; engine mutations are handled via `Consumer<Sim>` callbacks
- **Separation of Concerns** — lifecycle aging lives in `LifecycleManager`, needs tracking in `SimsNeedsTracker`, career logic in `CareerManager`, relationships in `RelationshipManager`
- **Centralized constants** — all magic numbers are consolidated in `GameConstants` for easy tuning

---

## Project Structure

```
Sims-CLI-game/
├── run.bat                          # Compile and run the game
├── run-tests.bat                    # Compile and run JUnit 5 tests
├── docs/                            # Generated Javadoc (javadoc -d docs ...)
├── lib/                             # JUnit 5 standalone JAR
├── saves/                           # Persistent save files (auto-created)
├── src/simcli/
│   ├── Main.java                    # Entry point
│   ├── engine/                      # Core game engine
│   │   ├── GameEngine.java          # Facade — main game coordinator
│   │   ├── GameLoop.java            # Per-tick simulation processing
│   │   ├── InputHandler.java        # Maps input → Command objects
│   │   ├── TimeManager.java         # Tick/day/time-of-day tracking
│   │   ├── WorldManager.java        # City map and location management
│   │   ├── LifecycleManager.java    # Aging, death, retirement
│   │   ├── RandomEventManager.java  # Random event generation
│   │   ├── SimulationLogger.java    # Buffered message logging
│   │   ├── CommandResult.java       # Enum result type for commands
│   │   ├── IInputHandler.java       # Input handler interface
│   │   ├── IWorldManager.java       # World manager interface
│   │   └── commands/                # Command pattern implementations
│   │       ├── ICommand.java        # Command interface
│   │       ├── BaseCommand.java     # Template base class
│   │       ├── CommandContext.java   # Builder-pattern context object
│   │       ├── WorkCommand.java
│   │       ├── TravelCommand.java
│   │       ├── InteractCommand.java
│   │       ├── InventoryCommand.java
│   │       ├── MoveRoomCommand.java
│   │       ├── SwitchSimCommand.java
│   │       ├── SpouseInteractionCommand.java
│   │       ├── JobMarketCommand.java
│   │       ├── HouseInfoCommand.java
│   │       ├── CharacterStatusCommand.java
│   │       └── UpgradeRoomCommand.java
│   ├── entities/                    # Game entities and data models
│   │   ├── actors/
│   │   │   ├── Sim.java             # Core Sim entity (player/NPC)
│   │   │   ├── NPCSim.java          # NPC specialization
│   │   │   └── ISimBehaviour.java   # Sim behaviour interface
│   │   ├── items/
│   │   │   ├── Item.java            # Base item class
│   │   │   ├── Consumable.java      # Edible items (Food inherits)
│   │   │   ├── Food.java            # Food item
│   │   │   └── Furniture.java       # Furniture items
│   │   ├── lifecycle/               # State Pattern — life stages
│   │   │   ├── LifeStage.java       # Interface
│   │   │   ├── ChildStage.java
│   │   │   ├── TeenStage.java
│   │   │   ├── AdultStage.java
│   │   │   └── ElderStage.java
│   │   ├── managers/
│   │   │   ├── CareerManager.java   # Job, promotions, shifts
│   │   │   ├── InventoryManager.java
│   │   │   ├── RelationshipManager.java
│   │   │   ├── NPCManager.java      # NPC pool management
│   │   │   └── NPCProvider.java     # NPC access interface
│   │   └── models/                  # Enums and data classes
│   │       ├── Job.java             # Career definitions
│   │       ├── SimState.java        # HEALTHY, HUNGRY, TIRED, DEAD
│   │       ├── ActionState.java     # IDLE, WORKING, SLEEPING, etc.
│   │       ├── Gender.java
│   │       ├── Trait.java           # ACTIVE, LAZY, SOCIALITE
│   │       ├── SocialAction.java    # CHAT, JOKE, ARGUE, FLIRT
│   │       ├── Relationship.java
│   │       ├── RelationshipStatus.java
│   │       └── WorkResult.java
│   ├── needs/                       # Strategy Pattern — need decay
│   │   ├── Need.java               # Abstract base (0–100 scale)
│   │   ├── Hunger.java
│   │   ├── Energy.java
│   │   ├── Hygiene.java
│   │   ├── Happiness.java
│   │   ├── Social.java
│   │   └── SimsNeedsTracker.java   # Aggregates all needs + cross-penalties
│   ├── persistence/
│   │   ├── SaveManager.java        # Text-file serialization/deserialization
│   │   └── Savable.java            # Item save/load contract
│   ├── ui/
│   │   ├── MainMenu.java           # Main menu controller
│   │   ├── UIManager.java          # Centralized print utilities
│   │   ├── IRenderer.java          # Renderer interface (DI)
│   │   ├── TerminalRenderer.java   # Terminal-based HUD rendering
│   │   ├── AsciiArt.java           # Logo and static ASCII art
│   │   ├── MenuPagination.java     # Paginated list display
│   │   └── ascii/                  # ASCII art engine and providers
│   │       ├── AsciiEngine.java
│   │       ├── IAsciiProvider.java
│   │       └── providers/
│   ├── utils/
│   │   ├── GameConstants.java      # All tunable game parameters
│   │   └── GameRandom.java         # Shared Random instance
│   └── world/                      # World and location model
│       ├── Building.java           # Abstract base for all locations
│       ├── Residential.java        # Homes with rooms
│       ├── Commercial.java         # Shops
│       ├── Park.java               # Social areas with NPCs
│       ├── Room.java               # Rooms inside residential buildings
│       └── interactables/          # Things Sims can use
│           ├── Interactable.java   # Interface
│           ├── AbstractShop.java   # Base shop shelf
│           ├── Bed.java
│           ├── Fridge.java
│           ├── Shower.java
│           ├── Computer.java
│           ├── WeightBench.java
│           ├── ParkBench.java      # NPC social interactions
│           ├── GroceryShelf.java
│           ├── BookshopShelf.java
│           ├── StorageChest.java
│           ├── Container.java
│           └── InteractableFactory.java
└── test/simcli/                    # JUnit 5 test suite
    ├── IntegrationTest.java
    ├── engine/
    │   ├── TimeManagerTest.java
    │   └── commands/
    ├── entities/
    │   ├── actors/
    │   ├── components/
    │   ├── items/
    │   └── lifecycle/
    ├── needs/
    │   ├── NeedTest.java
    │   └── SimsNeedsTrackerTest.java
    ├── persistence/
    │   └── SaveManagerTest.java
    └── world/
        ├── RoomTest.java
        └── interactables/
```

---

## Testing

The project includes a **JUnit 5** test suite covering:

- **Unit tests** — individual components like `TimeManager`, `Need`, `Room`, and `SaveManager`
- **Integration tests** — end-to-end flows exercising the engine, actors, and world together
- **Entity tests** — Sim lifecycle, inventory, and career management

Run all tests:
```bash
run-tests.bat
```

The test runner uses the JUnit Platform Console Standalone JAR (`lib/junit-platform-console-standalone.jar`) — no Maven or Gradle required.

---

## Contributors

| Author |
|--------|
| AfifLotfi |
| BlackberryDealer |
| colossalcap |
| Dylan Ho Sheng Xue |
| Johndose-val |
| lucas |
| macfreze12 |
| Muhammad Hasan Bin Suwandi |

---

## License

This project was developed as an academic Object-Oriented Programming assignment.
