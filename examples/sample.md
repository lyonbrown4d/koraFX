# Sample Apps

## Minimal JavaFX App

```kotlin
class MinimalApp : Application() {
    private val scope = MainScope()
    private val themeManager = ThemeManager()
    private val themeController = SceneThemeController(themeManager)

    override fun start(stage: Stage) {
        val root = panel {
            label("KoraFX") {
                styleClasses("headline")
            }
            button("Toggle Theme") {
                onAction {
                    themeManager.toggle()
                }
            }
        }

        val scene = Scene(root, 640.0, 420.0)
        themeController.bind(scene)

        stage.title = "Minimal KoraFX"
        stage.scene = scene
        stage.show()
    }

    override fun stop() {
        scope.cancel()
        themeController.dispose()
    }
}
```

## Runnable Sample

运行完整示例：

```powershell
.\gradlew.bat :sample-workbench-app:run
```

The workbench sample uses property-level state selectors such as `stateText`, `stateVisible`, `stateDisable`, and `stateList` directly where nodes are declared, instead of a separate `bindUi` pass.
