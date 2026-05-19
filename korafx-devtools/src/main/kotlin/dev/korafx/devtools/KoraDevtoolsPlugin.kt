package dev.korafx.devtools

import dev.korafx.framework.KoraApplication
import dev.korafx.framework.KoraApplicationPlugin
import org.koin.core.KoinApplication
import org.koin.dsl.koinApplication
import org.koin.dsl.module

internal class KoraDevtoolsPlugin(
    private val spec: KoraDevtoolsSpec,
) : KoraApplicationPlugin {
    private var koinApplication: KoinApplication? = null

    override fun onStart(app: KoraApplication) {
        if (!spec.enabled) {
            return
        }

        koinApplication = koinApplication {
            modules(devtoolsModule(app, spec))
        }.also { graph ->
            graph.koin.get<KoraDevtoolsController>().install()
        }
    }

    override fun onStop(app: KoraApplication) {
        koinApplication?.let { graph ->
            runCatching {
                graph.koin.get<KoraDevtoolsController>().dispose()
            }
            graph.close()
        }
        koinApplication = null
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
