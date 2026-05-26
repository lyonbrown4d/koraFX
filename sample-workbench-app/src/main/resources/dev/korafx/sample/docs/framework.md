# Framework

`korafx-framework` is the application layer that wires the JavaFX runtime, DI, theme and navigation.

## What you get

- `koraApplication` entry with lifecycle hooks.
- `installKoin` for application DI setup.
- Theme registration and live theme swapping via `ThemeManager`.
- Navigation host configuration and initial route bootstrap.
- Optional platform-specific window chrome integration.

## Typical setup

```kotlin
koraApplication(args) {
  installKoin {
    modules(workbenchModule())
  }

  theme {
    presets(BuiltInThemes.all)
    default(BuiltInThemes.MaterialLight)
  }

  navigation {
    initialRoute = WorkbenchRoute.Overview
    routes(WorkbenchRoute.all)
  }

  content {
    WorkbenchRootView(WorkbenchAppGraph.from(this)).buildRoot()
  }
}
```

## Best practice

- Keep framework wiring at the app boundary and expose domain services through a typed graph object.
- Let module pages stay pure UI (render logic only), and inject all external services through ViewModels.
