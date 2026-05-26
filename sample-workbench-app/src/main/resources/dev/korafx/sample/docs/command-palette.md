# Command Palette

`korafx-command-palette` is a lightweight command launcher for desktop workflows.

## What it offers

- Fuzzy filtering across command title/description/group.
- Keyboard and mouse selection.
- Host visibility control + close behavior.
- Command model decoupled from UI rendering.

## Example

```kotlin
val host = CommandPaletteHost(commands = commands)

button("Commands") {
  onAction { host.show() }
}
commandPalette(host)
```

Commands are registered by the application layer, keeping modules focused on rendering.
