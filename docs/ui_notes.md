# UI notes

## Setup screen

The setup screen allows selection of:

- Number of players
- Player names
- Starting score
- Finish rule

## Game screen

The game screen shows:

- Current player
- Current remaining score
- Live checkout suggestion
- Player score cards
- Three dart slots
- Provisional turn score
- Provisional remaining score
- Tappable dartboard
- Miss, undo, and confirm controls

## Dart slots

The three dart slots show the recorded throws for the current turn.

Tapping a slot selects it for reassignment. The next board tap or miss action should replace that selected dart rather than appending a new one.

If no slot is selected, the next dart is added to the first available empty slot.

## Confirmation model

The app deliberately does not commit a turn automatically after the third dart. This allows the user to check whether all three darts were registered correctly before confirming.

## Board marker labels

Board hits are marked with labels showing dart order:

- `1` for first dart
- `2` for second dart
- `3` for third dart

Repeated hits in the same target area should show combined labels where possible.

## Design direction

The app is intended to be usable in a pub or casual match context, so the interface should prioritise:

- Large targets
- Low cognitive load
- Easy correction
- Clear player state
- Minimal setup friction


## v0.5.2 dashboard and persistence UI

- The in-game top-right control is now `Exit`, returning the user to the setup dashboard without clearing the saved game.
- When a saved game exists, the dashboard shows a saved-game card with the current player and score.
- The saved-game card provides `Resume` and `Clear` actions.
- The game screen continues to reserve vertical status space so the dartboard does not jump when messages appear.
