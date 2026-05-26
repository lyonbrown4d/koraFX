// Kotlin snippet: path 导航与 query/hash 演示
navigator.navigatePath("/routes/42/files?tab=overview")
navigator.currentLocation.params["projectId"] // "42"
navigator.currentLocation.query["tab"] // "overview"
navigator.currentLocation.fullPath

val next = navigator.currentLocation.withQuery("sort" to "name", "tab" to "overview")
navigator.replacePath(next)
