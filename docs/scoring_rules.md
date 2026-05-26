# Scoring rules

## Supported starting scores

- 301
- 501

## Turn structure

- Each turn can contain up to three darts.
- A dart may be a board hit or a miss.
- The user confirms the turn manually.
- Score is only committed after confirmation.

## Dart values

- Single segment: segment value
- Double segment: segment value × 2
- Treble segment: segment value × 3
- Outer bull: 25
- Bull: 50
- Miss: 0

## Straight out

In straight-out mode, a player wins if the confirmed turn reduces their score exactly to zero.

Bust conditions:

- Remaining score below zero.

## Double out

In double-out mode, a player wins only if the confirmed turn reduces their score exactly to zero and the final scoring dart is a double or bull.

Bust conditions:

- Remaining score below zero.
- Remaining score equals one.
- Remaining score equals zero but the final scoring dart was not a double or bull.

## Bust behaviour

If a turn busts:

- The player's previous score is preserved.
- The turn is cleared.
- Play moves to the next player.

## Player rotation

Play moves to the next player only after the user confirms the turn.
