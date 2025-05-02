# Resource World

This mod can create resource worlds. These worlds can reset standalone from main worlds.

**Logically support modded dimensions.**

## GameRules

- `resource_world:tp_cooldown_seconds`: `/resourceworld tp` command cooldown for players.
- `resource_world:hide_seed_hash`: Whether hide resource world seed hash from players, open this can partly prevent seed
  cracking but may cause wrong biome sounds and sky colors.

## Commands

Root command: `/resourceworld`

### Player Commands

- `/resourceworld home`: Teleport to your spawn point. This is the only way to exit resource worlds without other mods.
- `/resourceworld tp <world id>`: Random teleport to resource worlds.

### Admin Commands

- `/resourceworld create <world id> <target dimension> (<seed>)`: Create a resource world with specific dimension
  options. Seed is optional, leave blank for random seed.
- `/resourceworld delete <world id>`: Permanently delete a resource world.
- `/resourceworld reset <world id>`: Reset specific resource world. (Delete previous one and create new with different
  seed.)
- `/resourceworld enable/disable <world id>`: Enable/Disable specific resource world.
- `/resourceworld range get/set-range/set-center <world id> ...`: Configure resource world borders.
