# Checkout logic

## Purpose

The app shows a suggested out when the current or provisional remaining score can be completed within three darts.

## Current behaviour

The checkout suggestion is recalculated after every dart using:

```text
current player score - provisional turn score
```

This means that if the player starts with a suggested checkout and misses the intended route, the app updates the route from the new provisional remaining score.

Example:

```text
Starting remaining score: 80
Initial suggested out: T20 D10
First dart hit: S20
Provisional remaining: 60
Updated suggested out: S20 D20, T20, or another valid route depending on ordering
```

## Double-out mode

In double-out mode:

- The final dart in the suggestion must be a double or bull.
- Suggested routes are generated up to 170.
- Scores that cannot be finished under double-out rules return no suggestion.

## Straight-out mode

In straight-out mode:

- Any valid final dart may finish the game.
- Suggestions are generated for one, two, or three dart combinations.

## Current limitation

The route generator is valid but not yet preference-aware. It does not fully prioritise common human checkout choices such as leaving favourite doubles, avoiding awkward numbers, or preferring high-probability routes.

## Future improvement

Replace the simple algorithmic ordering with a curated checkout table for double-out scores from 2 to 170, then use algorithmic fallback for straight-out mode.
