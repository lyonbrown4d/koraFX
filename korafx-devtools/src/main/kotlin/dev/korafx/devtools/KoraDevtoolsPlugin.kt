package dev.korafx.devtools

import dev.korafx.framework.KoraApplication
import dev.korafx.framework.KoraApplicationPlugin
import org.koin.core.module.Module
import org.koin.dsl.module

internal class KoraDevtoolsPlugin(
    private val spec: KoraDevtoolsSpec,
) : KoraApplicationPlugin {
    override fun modules(app: KoraApplication): List<Module> =
        if (spec.enabled) {
            listOf(devtoolsModule(app, spec))
        } else {
            emptyList()
        }

    override fun onStart(app: KoraApplication) {
        if (!spec.enabled) {
            return
        }

        app.get<KoraDevtoolsController>().install()
    }

    override fun onStop(app: KoraApplication) {
        if (!spec.enabled) {
            return
        }

        app.get<KoraDevtoolsController>().dispose()
    }
}

private fun devtoolsModule(
    app: KoraApplication,
    spec: KoraDevtoolsSpec,
) = module {
    single { app }
    single { spec }
    single { DevtoolsMessages.forLanguage(spec.language) }
    single { DevtoolsSelectionModel() }
    single { NodeHighlighter(app.stage) }
    single {
        KoraDevtoolsController(
            app = get(),
            spec = get(),
            messages = get(),
            selection = get(),
            highlighter = get(),
        )
    }
}
