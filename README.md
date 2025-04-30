# Resource World

This mod add three dimensions as resource worlds. These worlds can reset standalone from main worlds.

## GameRules

- `resource_world:tp_cooldown_seconds`: `/resource tp` command cooldown for players.
- `resource_world:hide_seed_hash`: Whether hide resource world seed hash from players, open this can partly prevent seed
  cracking but may cause wrong biome sounds and sky colors.

## Commands

Root command: `/resource`

### Player Commands

- `/resource home`: Teleport to your spawn point. This is the only way to exit resource worlds without other mods.
- `/resource tp (overworld|nether|end)`: Random teleport to resource worlds.

### Admin Commands

- `/resource reset (overworld|nether|end)`: Reset specific resource world. (Delete previous one and create new with
  different seed.)
- `/resource enable/disable (overworld|nether|end)`: Enable/Disable specific resource world.
- `/resource range get/set-range/set-center (overworld|nether|end) ...`: Configure resource world borders.