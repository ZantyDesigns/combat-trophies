# Combat Trophies

A PlayStation-style trophy tracker for OSRS Combat Achievements.

- **Unlock popup**: a "Trophy Unlocked" toast slides in whenever you finish a combat task.
- **Trophy case panel**: per-tier counts, total points, filter by tier, search by name.
- **Tier mapping**: Easy = Bronze, Medium = Silver, Hard = Gold, Elite = Sapphire, Master = Ruby, Grandmaster = Amethyst.
- **Platinum trophy**: awarded when every tier's tracked count reaches its configured total (set all six under *Tier totals* in the plugin config). Once earned it is never revoked.
- Data is stored per OSRS account via RuneLite's profile config.

## Running
Copy `build.gradle`, `settings.gradle`, `runelite-plugin.properties` and `src/` into a clone of
https://github.com/runelite/example-plugin (to get the Gradle wrapper), then run
`CombatTrophiesPluginTest.main` from your IDE.

## Limitation
Trophies are recorded from the in-game completion chat message, so tasks completed
*before* installing the plugin are not imported.
