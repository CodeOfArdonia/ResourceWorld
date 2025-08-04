# Resource World

This mod can create resource worlds. These worlds can reset standalone from main worlds.

**Logically support modded dimensions.**

## Features

- Create separated resource worlds from any exists dimensions
- Standalone seeds, game rules, difficulties and world borders
- Reset standalone

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
- `/resourceworld settings <world id> <option> get/set`: Configure resource world settings.

#### All available settings

- `centerX`: Center X of the world border.
- `centerZ`: Center Z of the world border.
- `range`: World border radius.
- `spawnPoint`: Resource world spawn point.
- `cooldown`: `/resourceworld tp` cooldown.
- `hideSeedHash`: Whether hide resource world seed hash from players, open this can partly prevent seed cracking but may
  cause wrong biome sounds and sky colors.
- `allowHomeCommand`: Whether allow using `/resource home` command.

### Developer Usage

If you want to customize the dimension random teleport locator, create a class implement `RandomTeleportEntrypoint`,
then annotate with `@RandomTeleportProvider`(forge) or write into entrypoint `random_teleport_locator`(fabric)