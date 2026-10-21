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
- **Health system**: low hunger slowly drains health; starving (hunger at 0) drains it faster every consecutive hour, and a Sim dies when health reaches 0 — infants included
- **Autonomy**: household members you aren't controlling look after themselves. They eat the most filling food in a fridge at home when hungry, shower when filthy, nap when exhausted, go to bed when you do, and feed hungry babies. If the fridge is empty, they'll tell you.

### 🎯 Skills
| Skill | Trained by | Pays off |
|---|---|---|
| Fitness | Working out on the Weight Bench (Active Sims learn 50% faster) | Cheaper workouts; Personal Trainer pay and promotions |
| Logic | Studying at a Computer | Software Engineer and Hardware Technician pay and promotions |
| Charisma | Chatting, joking, flirting, dates (Socialites learn 50% faster) | Jokes and flirts land more often; relationships grow faster; Photographer pay and promotions |

- Skills go from level 0 to 10, and working a shift trains your career's skill a little
- Each level of your career's skill adds **+5% pay** and **+5% promotion chance**
- Promotion past tier 2 needs skill: tier 3 needs level 2, tier 4 needs level 4, tier 5 needs level 6
- See every skill's level and progress on the status screen (`I`)

### 🎯 Goals
- Every Sim has a **life aspiration**: Career Star (top tier of a career), Big Family (3 children), Homeowner (buy the Bungalow), Master of One (a skill at level 10) or Social Butterfly (5 friends at 50+). You pick it when creating a Sim; babies and spouses get a random one
- Fulfilling an aspiration pays **$1,000** and a big mood boost
- **Legacy complete**: once every playable Sim in the household has fulfilled their aspiration, you've won. You can keep playing afterwards
- **12 achievements** for household milestones (first job, wedding, first baby, Bungalow, $10,000 saved, ...)
- Press `G` to see everyone's progress and the achievements list

### 💼 Career System
- **4 careers**: Software Engineer, Hardware Technician, Freelance Photographer, Personal Trainer
- **3 part-time jobs open to teens**: Barista, Lifeguard, Library Assistant
- Each job has unique salary, energy drain, working hours, age limits, and up to **5 promotion tiers**
- Each career rewards one **skill**: promotion chance starts at 25% per shift and rises with that skill (up to 75%), and pay scales with both tier and skill
- Overwork penalties and forced retirement when a Sim exceeds the job's maximum age

### 🏙️ World & Locations
| Location | Type | Interactables |
|---|---|---|
| The Shared Dorm | Residential (free) | Bed, Fridge, Shower, Weight Bench |
| The Bungalow | Residential ($5,000) | Bed, Fridge, Shower, Computer |
| Town Supermarket | Commercial | Grocery Shelf (ready food, raw ingredients, furniture) |
| The Bookshop | Commercial | Bookshop Shelf (buy books) |
| City Park | Park | Park Bench (socialize with NPCs) |

- **Room system** — residential buildings contain rooms (Bedroom, Kitchen, Garage, Bathroom), each with furniture capacity
- **Room upgrades** — expand storage capacity for $500
- **Property purchasing** — buy the Bungalow to upgrade your living situation

### 💸 Money & Food
- **Weekly bills** every Monday for each home you own: $100 for the Dorm, $250 for the Bungalow. The richest Sim pays first. If the household can't cover the bill, everyone loses happiness. `H` shows what's due
- **Cooking**: raw Groceries ($15) and Raw Steak ($35) barely fill you up raw, but cooked at a fridge (`C`) they make a Home-cooked Meal (+55 hunger) or a Steak Dinner (+80). Ready food like Microwave Dinners costs more for less
- **Spoilage**: food stays fresh for a few days and loses 1 day of freshness per day in a fridge, 2 per day in a bag or chest. Food that runs out of freshness is thrown away

### 💕 Relationships & Family
- **Social actions**: Chat (always safe), Joke (60% to land), Flirt (adults who know each other, 40%+ to land, best relationship boost) and Argue. The menu shows each action's odds. Charisma raises them, and a flop costs a little relationship and happiness
- **Relationship progression**: Stranger → Acquaintance → Friend → Romantic → Married
- **Marriage** at 100 relationship score; the Family menu (`L`) lets you go on dates, try for a baby, feed the babies and **give gifts** from your bag (pricier gifts build more relationship)
- **Children**: 50% conception chance, born as non-playable infants who grow a year every day and become playable at 13. Teens go to **school** on weekdays (+Logic) and can take **part-time jobs**
- **A living park**: 8 visitors a day, drawn from a town that remembers who you've met. Each morning 3 visitors leave and others arrive, either old acquaintances or new faces. Townsfolk age, move away at 80, and have personalities shown in the park list. Socialites are easier to win over (+15% on jokes and flirts) and Lazy Sims harder (−10%)

### 🎂 Lifecycle & Aging
- **4 life stages** via the State Pattern: Child → Teen (13) → Adult (18) → Elder (65)
- Children age 1 year **every day**; teens, adults and elders age 1 year every **3 in-game days**
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

Separately, each turn has a 4% chance of a **household event** that hits everyone, picked from those that fit the time:
- ☀️ Sunny morning (06:00–11:00): happiness +10
- 🥧 Neighbour's pie (12:00–17:00): social +10, and a pie in the fridge
- 🎉 Weekend party (Sat/Sun evenings): social +25, happiness +15, energy −10 for everyone awake
- 🌩️ Power cut (21:00–05:00): hygiene −10, and fridge food loses 2 days of freshness
- 🤒 Flu (any time): energy −15, happiness −10

### 💾 Save System
- **Text-based persistence** — saves to `saves/<worldName>.txt`
- Full state serialization: Sim stats, inventory, relationships, spouses, children, building ownership, room states, world position
- **Autosave** every 10 ticks
- Create, load, and delete saves from the main menu

### 🎨 ASCII Art Rendering
- Dynamic ASCII art driven by an extensible provider system
- Contextual rendering based on location and Sim action state: every building and every activity
  (sleeping, eating, working, studying, socializing, working out, showering, having fun) has its own scene
- A Sims-style **plumbob** above your Sim's head that turns green, yellow or red with their mood
- Short **animations** when your Sim acts, travels, or sleeps through the night
- A colour-coded HUD: date and time banner, need bars, household summary, and an action menu grouped by purpose

### 🖥️ Display settings
- Colours and animations can be toggled from **Settings** in the main menu
- Colours are detected automatically (Windows Terminal, VS Code, most Linux/macOS terminals);
  force them with the environment variable `SIMCLI_COLOR=always` or `SIMCLI_COLOR=never` (`NO_COLOR` is honoured too)
- On consoles that can't show box-drawing characters, the UI falls back to plain ASCII

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
The scripts find a JDK automatically: `JAVA_HOME` first, then `javac` on the `PATH`, then the JDK bundled
with VS Code's Java extension, then the usual install folders. If none is found, install one
(e.g. `winget install EclipseAdoptium.Temurin.21.JDK`) or set `JAVA_HOME`.

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
[1] New world     — Start a new game with a fresh household
[2] Load world    — Resume a saved game (pick it by number or name)
[3] Delete world  — Permanently remove a save file
[4] Settings      — Turn colours and animations on or off
[0] Quit          — Quit the application
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
| `G` | Goals: aspirations and achievements |
| `V` | Open inventory (eat food, use/drop items) |
| `U` | Upgrade room storage capacity ($500) |
| `K` | Switch active Sim to another household member |
| `L` | Family: date your spouse, try for a baby, feed the babies, give a gift |
| `S` | Save and exit to main menu |
| `1-9` | Use the numbered things listed under **HERE** (bed, fridge, shop, park bench...) |

Every sub-menu uses `0` to go back. Backing out of a shop, the fridge, a storage chest or the park
without doing anything costs no time.

### Gameplay Tips
- **Keep hunger above 20** — hunger at zero drains health faster every hour until your Sim dies
- **Cook instead of buying ready meals** — raw ingredients cooked at the fridge are the cheapest way to stay fed
- **Keep money aside for Monday** — unpaid bills make the whole household miserable
- **Sleep regularly** — sleeping fast-forwards time to 8 AM and restores energy
- **Socialize at the park** — the Socialite trait gives bonus relationship and happiness gains
- **Train your career's skill** — without it you'll stall at tier 2 (check `I` to see what the next promotion needs)
- **Keep the fridge stocked** — idle household members feed themselves (and the babies) from it
- **Save up for the Bungalow** ($5,000) — it comes with a Computer for studying Logic
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
│   │   ├── AutonomyManager.java     # Idle Sims eat, sleep, shower, feed babies
│   │   ├── InputHandler.java        # Maps input → Command objects
│   │   ├── TimeManager.java         # Tick/day/time-of-day tracking
│   │   ├── WorldManager.java        # City map and location management
│   │   ├── LifecycleManager.java    # Aging, death, retirement
│   │   ├── RandomEventManager.java  # Random and household-wide events
│   │   ├── UpkeepManager.java       # Weekly bills, food spoilage
│   │   ├── GoalManager.java         # Aspirations, achievements, legacy
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
│   │       ├── GoalsCommand.java     # Aspirations and achievements screen
│   │       └── UpgradeRoomCommand.java
│   ├── entities/                    # Game entities and data models
│   │   ├── actors/
│   │   │   ├── Sim.java             # Core Sim entity (player/NPC)
│   │   │   ├── NPCSim.java          # NPC specialization
│   │   │   └── ISimBehaviour.java   # Sim behaviour interface
│   │   ├── items/
│   │   │   ├── Item.java            # Base item class
│   │   │   ├── Consumable.java      # Edible items (Food inherits)
│   │   │   ├── Food.java            # Food item that goes off
│   │   │   ├── Ingredient.java      # Raw food, cooked into meals
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
│   │   │   ├── SkillManager.java    # Skill experience and levels
│   │   │   ├── NPCManager.java      # Park visitors and townsfolk
│   │   │   └── NPCProvider.java     # NPC access interface
│   │   └── models/                  # Enums and data classes
│   │       ├── Job.java             # Career definitions
│   │       ├── SimState.java        # HEALTHY, HUNGRY, TIRED, DEAD
│   │       ├── ActionState.java     # IDLE, WORKING, SLEEPING, etc.
│   │       ├── Gender.java
│   │       ├── Trait.java           # ACTIVE, LAZY, SOCIALITE
│   │       ├── SocialAction.java    # CHAT, JOKE, FLIRT, ARGUE
│   │       ├── SkillType.java       # FITNESS, LOGIC, CHARISMA
│   │       ├── Aspiration.java      # Life goals
│   │       ├── Achievement.java     # Household milestones
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
