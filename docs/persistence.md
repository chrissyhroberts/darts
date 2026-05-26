# Persistence design

Version: v0.5.2

The app stores the active game locally using Android `SharedPreferences`. The saved payload is a JSON representation of `GameState`.

## Saved fields

- player names and remaining scores
- current player index
- current turn darts
- starting score
- finish rule
- status message
- selected dart slot, if any

## Behaviour

- Game state is saved whenever the in-memory `gameState` changes.
- Closing and reopening the app leaves the saved game available from the dashboard.
- Pressing `Exit` returns to the dashboard but does not delete the saved game.
- Pressing `Clear` on the dashboard removes the saved game from local storage.

## Current limitations

- There is no full throw history yet.
- There is no previous-turn undo yet.
- Persistence is local to the device only.
