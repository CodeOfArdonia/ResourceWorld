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

- `/resourceworld create <world id> <type/flat> ... [<seed>]`: Create a resource world with specific dimension options.
  Seed is optional, leave blank for random seed.
- `/resourceworld create <world id> mirror <dimension>`: Create a snapshot of an existing dimension.
- `/resourceworld create <world id> template <template> <dimension>`: Create a snapshot from a template.
- `/resourceworld delete <world id>`: Permanently delete a resource world.
- `/resourceworld reset <world id>`: Reset specific resource world. (Delete previous one and create new with different
  seed.)
- `/resourceworld enable/disable <world id>`: Enable/Disable specific resource world.
- `/resourceworld settings <world id> <option> [<new value>]`: Configure resource world settings.

#### All available settings

- `centerX`: Center X of the world border.
- `centerZ`: Center Z of the world border.
- `range`: World border radius.
- `spawnPoint`: Resource world spawn point.
- `cooldown`: `/resourceworld tp` cooldown.
- `hideSeedHash`: Whether hide resource world seed hash from players, open this can partly prevent seed cracking but may
  cause wrong biome sounds and sky colors.
- `allowHomeCommand`: Whether allow using `/resource home` command.

### Copy world sources

- `mirror <dimension>` snapshots an existing loaded dimension in the current save. The server saves before copying.
- `template <template> <dimension>` loads a snapshot from `<server directory>/resourceworld/<template>`.

Both types copy only `region`, `entities`, and `poi`. Template folders use normal save layouts: the template root is the
overworld, `DIM-1` is the Nether, `DIM1` is the End, and `dimensions/<namespace>/<path>` is any other dimension.
Both types preserve their original source seed and never accept a seed argument. Resetting only refreshes the copied data.
Templates must include their original `level.dat` so `template` can read the seed.

## Developer Usage

If you want to customize the dimension random teleport locator, create a class implement `RandomTeleportEntrypoint`,
then annotate with `@EntryPointProvider(slug="random_teleport_locator")`(forge) or write into entrypoint
`random_teleport_locator`(fabric)
