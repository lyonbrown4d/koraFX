// Kotlin snippet: 同步与异步守卫
val stopHandle = navigator.beforeEach { context ->
    if (context.to.route.id == "router-host") {
        NavigationDecision.Block("router host currently disabled")
    } else {
        NavigationDecision.Allow
    }
}

val asyncHandle = navigator.beforeEachAsync { context ->
    if (context.to.route.id == "state" && context.type == NavigationType.PUSH) {
        NavigationDecision.Redirect(path = "/overview")
    } else {
        NavigationDecision.Allow
    }
}

// 关闭时记得 stopHandle.close() / asyncHandle.close()
