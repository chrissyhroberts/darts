# Changelog

## v0.5.3

UI stability and dashboard exit fix.

- Reserved a fixed-height line for provisional outs and bust warnings so the dartboard does not shift when these messages appear or disappear.
- Reserved a fixed-height status-message box for prompts such as “Check darts, then confirm.” and “X scored Y.”
- Changed the in-game Exit control from a text button to a regular button and explicitly routes back to the dashboard while preserving the saved game.

## v0.5.2

Game persistence and dashboard exit.

- Added local game-state backup using Android SharedPreferences.
- The app now restores the current game after app close/restart.
- Added an Exit button in the game screen to return to the setup dashboard without deleting the saved game.
- Added Resume and Clear controls on the dashboard when a saved game exists.
- Saved state includes players, scores, current player, current turn, finish rule, status message, and selected dart slot.

## v0.5.0

Documentation and consolidation release.

- Consolidated current app into a single Android Studio project package.
- Added README, changelog, roadmap, and design notes.
- Documented scoring rules, checkout logic, and UI behaviour.

## v0.4.0

Manual turn confirmation and editable darts.

- Removed automatic player switch after the third dart.
- Added manual confirmation at the end of each turn.
- Added editable dart slots.
- Tapping Dart 1, Dart 2, or Dart 3 allows reassignment of that dart.
- Checkout recommendation now recalculates after each dart using the provisional remaining score.

## v0.3.0

Tap handling fix.

- Replaced problematic gesture handling with a simpler tap gesture detector.
- Fixed issue where only the first dart registered.

## v0.2.0

Build configuration fixes.

- Enabled AndroidX in `gradle.properties`.
- Aligned Java and Kotlin JVM targets to Java 17.

## v0.1.0

Initial prototype.

- Setup screen.
- Tappable dartboard.
- 301/501 scoring.
- Straight-out and double-out rules.
- Bust handling.
- Player rotation.
- Basic checkout suggestions.

## v0.5.1
- Reserved a fixed message/status row so the dartboard no longer shifts vertically when status text appears or disappears.
- Improved dartboard number contrast: black labels on light board segments and light labels on dark board segments.
