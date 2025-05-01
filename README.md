# Resource World

This mod can create resource worlds. These worlds can reset standalone from main worlds.

**Logically support modded dimensions.**

## GameRules

- `resource_world:tp_cooldown_seconds`: `/resource tp` command cooldown for players.
- `resource_world:hide_seed_hash`: Whether hide resource world seed hash from players, open this can partly prevent seed
  cracking but may cause wrong biome sounds and sky colors.

## Commands

Root command: `/resource`

### Player Commands

- `/resource home`: Teleport to your spawn point. This is the only way to exit resource worlds without other mods.
- `/resource tp <world id>`: Random teleport to resource worlds.

### Admin Commands

- `/resource create <world id> <target dimension> (<seed>)`: Create a resource world with specific dimension options.
  Seed is optional, leave blank for random seed.
- `/resource delete <world id>`: Permanently delete a resource world.
- `/resource reset <world id>`: Reset specific resource world. (Delete previous one and create new with different seed.)
- `/resource enable/disable <world id>`: Enable/Disable specific resource world.
- `/resource range get/set-range/set-center <world id> ...`: Configure resource world borders.
