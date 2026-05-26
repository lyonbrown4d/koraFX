package dev.korafx.framework

import dev.korafx.navigation.PageInstancePolicy
import dev.korafx.navigation.Route
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel

class KoraNavigationBuilder {
    var initialRoute: Route? = null
    var initialPath: String? = null
    var pageInstancePolicy: PageInstancePolicy = PageInstancePolicy.RECREATE
    var persistLocation: Boolean = false
    var preferencesNode: String = "dev.korafx"
    var preferencesKey: String = "navigation.location"
    private val routes = mutableListOf<Route>()

    fun routes(routes: Iterable<Route>) {
        this.routes += routes
    }

    fun routes(vararg routes: Route) {
        this.routes += routes
    }

    internal fun build(): KoraNavigationSpec {
        val initial = initialRoute ?: routes.firstOrNull() ?: KoraRootRoute
        val routeList = (listOf(initial) + routes)
            .distinctBy(Route::id)

        return KoraNavigationSpec(
            initialRoute = initial,
            initialPath = initialPath,
            routes = routeList,
            pageInstancePolicy = pageInstancePolicy,
            persistLocation = persistLocation,
            preferencesNode = preferencesNode,
            preferencesKey = preferencesKey,
        )
    }
}

class KoraLifecycleBuilder {
    private val stopHandlers = mutableListOf<KoraApplication.() -> Unit>()

    fun onStop(handler: KoraApplication.() -> Unit) {
        stopHandlers += handler
    }

    inline fun <reified T : AutoCloseable> close() {
        onStop {
            get<T>().close()
        }
    }

    inline fun <reified T : CoroutineScope> cancel() {
        onStop {
            get<T>().cancel()
        }
    }

    internal fun build(): List<KoraApplication.() -> Unit> = stopHandlers.toList()
}
