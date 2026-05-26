# Darts Scorer

Current version: v0.5.3

A simple offline Android darts scoring app built with Kotlin and Jetpack Compose.

Current internal version: **v0.5.1**

## Current features

- Game-state backup and restore after app close.
- Dashboard resume/clear controls for saved games.
- Exit-to-dashboard button during a game.

- Game setup screen
  - 1 to 8 players
  - Player names
  - 301 or 501 starting score
  - Straight-out or double-out finish rule
- In-game scoring screen
  - Tappable dartboard
  - Three editable dart slots
  - Manual turn confirmation
  - Miss and undo controls
  - Bust handling
  - Player rotation after confirmed turn
  - Live provisional remaining score
  - Live checkout suggestion recalculated after each dart
- Offline-only
- No backend
- No account or network dependency

## Build instructions

1. Unzip this folder.
2. Open the `DartsScorer_Complete` folder in Android Studio.
3. Let Gradle sync.
4. Run the `app` configuration on an emulator or Android device.

## Technical stack

- Kotlin
- Jetpack Compose
- Material 3
- Gradle Kotlin DSL
- AndroidX enabled
- JVM target aligned to Java 17

## Project structure

```text
DartsScorer_Complete/
  app/
    src/main/java/com/example/dartsscorer/MainActivity.kt
    src/main/AndroidManifest.xml
    build.gradle.kts
  docs/
    scoring_rules.md
    checkout_logic.md
    ui_notes.md
  README.md
  CHANGELOG.md
  ROADMAP.md
  settings.gradle.kts
  build.gradle.kts
  gradle.properties
```

## Main source file

The current prototype keeps the app in a single source file:

```text
app/src/main/java/com/example/dartsscorer/MainActivity.kt
```

This is intentional for early prototyping. Once the design settles, the next sensible refactor is to split the file into:

- `model/` for `Player`, `DartHit`, `GameState`, `FinishRule`
- `logic/` for scoring, bust handling, and checkout suggestions
- `ui/` for setup screen, game screen, dartboard, and score panels

## Known limitations

- Checkout suggestions are generated algorithmically and may not always match human-preferred routes.
- There is no saved game state after app restart.
- There is no match history yet.
- No legs or sets yet.
- No per-player statistics yet.
- The dartboard is functional but visually simple.

## Next priority improvements

1. Improve checkout preference ordering.
2. Add previous-turn undo.
3. Add legs and sets.
4. Add player statistics.
5. Add persistent game state.
6. Add landscape/tablet layout.
