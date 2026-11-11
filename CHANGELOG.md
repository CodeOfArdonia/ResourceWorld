## 2.1

- Add custom resource world generation from `resourceworld/presets/<preset>.json` using `/resourceworld create <world> custom <preset> [seed]`.
- Save the complete dimension JSON in the world config so restarts and resets do not require the original preset file.
- Support unregistered `LevelStem` objects through `CustomGenerateOption.fromStem`.
- Add preset name completion and localized validation errors.
