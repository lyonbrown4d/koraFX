# KoraFX Examples

这些示例展示 KoraFX 当前推荐的使用方式：DSL 优先，MVVM 保持轻量，组件按需组合。

## Index

- [DSL Examples](dsl.md): layout、form、inputs、menu/tree、table、dialogs。
- [Binding Examples](bindings.md): `StateFlow` binding、`RenderState` rendering。
- [MVVM Examples](mvvm.md): constructor injection、factory、deterministic tests、JavaFX view binding。
- [Navigation And Components](navigation-components.md): navigator、route host、route state host、feedback、surfaces。
- [Theme Examples](theme.md): theme wiring、custom tokens。
- [Sample Apps](sample.md): minimal JavaFX app、runnable workbench sample。

## Runnable Examples

```powershell
.\gradlew.bat :examples:dsl-basic-app:run
.\gradlew.bat :examples:mvvm-counter-app:run
.\gradlew.bat :sample-workbench-app:run
```

## Recommended Reading Order

1. Start with [DSL Examples](dsl.md).
2. Add dynamic state with [Binding Examples](bindings.md).
3. Structure screen logic with [MVVM Examples](mvvm.md).
4. Compose app-level UI with [Navigation And Components](navigation-components.md).
