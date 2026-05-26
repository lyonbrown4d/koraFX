// Kotlin snippet: routeDataHost 与 revalidate
val controller = RouteDataController()

routeDataHost(
    scope = uiScope,
    navigator = navigator,
    controller = controller,
    cache = true,
    load = { context ->
        val delayMs = context.query.int("delay") ?: 300
        delay(delayMs.toLong())
        if (context.query["mode"] == "error") {
            throw IllegalStateException("mock loader error")
        }
        "Loaded: ${context.location.fullPath}"
    },
    loading = { context ->
        loadingState("Loading ${context.route.title}...")
    },
    failed = { context, error ->
        errorState(
            title = "${context.route.title} failed",
            message = error.message.orEmpty(),
        )
    },
) { context, value ->
    vbox(10.0) {
        label("Path: ${context.location.fullPath}")
        label("Value: $value")
    }
}
