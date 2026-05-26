package dev.korafx.examples.navigationtheme

import dev.korafx.components.actionBar
import dev.korafx.components.appShell
import dev.korafx.components.section
import dev.korafx.components.toastHost
import dev.korafx.components.ToastHost
import dev.korafx.components.ToastTone
import dev.korafx.dsl.button
import dev.korafx.dsl.bindDisable
import dev.korafx.dsl.bindText
import dev.korafx.dsl.checkBox
import dev.korafx.dsl.comboBox
import dev.korafx.dsl.label
import dev.korafx.dsl.hbox
import dev.korafx.dsl.panel
import dev.korafx.dsl.menuButton
import dev.korafx.dsl.onAction
import dev.korafx.dsl.slider
import dev.korafx.dsl.paddingAll
import dev.korafx.dsl.scrollPane
import dev.korafx.dsl.sidebar
import dev.korafx.dsl.stateText
import dev.korafx.dsl.styleClasses
import dev.korafx.dsl.tabPane
import dev.korafx.dsl.textArea
import dev.korafx.dsl.textField
import dev.korafx.dsl.toolbar
import dev.korafx.dsl.vbox
import dev.korafx.dsl.ghostButton
import dev.korafx.components.errorState
import dev.korafx.components.loadingState
import dev.korafx.framework.theme.BuiltInThemes
import dev.korafx.framework.theme.KoraTheme
import dev.korafx.framework.theme.SceneThemeController
import dev.korafx.framework.theme.ThemeManager
import dev.korafx.framework.theme.ThemeStyleClass
import dev.korafx.navigation.NavigationDecision
import dev.korafx.navigation.Navigator
import dev.korafx.navigation.PageInstancePolicy
import dev.korafx.navigation.PathRoute
import dev.korafx.navigation.Route
import dev.korafx.navigation.RouteMeta
import dev.korafx.navigation.RoutePattern
import dev.korafx.navigation.NavigationTransitionProfile
import dev.korafx.navigation.RouteDataController
import dev.korafx.navigation.RouteTransition
import dev.korafx.navigation.routerHost
import dev.korafx.navigation.navigationResultKey
import dev.korafx.navigation.scaleDuration
import dev.korafx.navigation.bindContentWithTransition
import dev.korafx.navigation.routeDataHost
import dev.korafx.navigation.routeButton
import dev.korafx.navigation.routeHost
import dev.korafx.navigation.routeMeta
import javafx.application.Application
import javafx.geometry.Insets
import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.stage.Stage
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.util.concurrent.atomic.AtomicInteger

private data class DemoRoute(
    override val id: String,
    override val title: String,
    override val path: String,
    val description: String,
    val docResource: String,
    val sourceResource: String,
    val section: RouteSection,
    override val meta: RouteMeta = RouteMeta.Empty,
) : PathRoute {
    companion object {
        val Overview = DemoRoute(
            id = "overview",
            title = "Overview",
            path = "/",
            description = "Route + theme + transition 的能力总览。",
            docResource = "dev/korafx/examples/navigationtheme/docs/overview.md",
            sourceResource = "dev/korafx/examples/navigationtheme/snippets/overview.kt",
            section = RouteSection.Core,
            meta = routeMeta("level" to "core"),
        )

        val PathRouting = DemoRoute(
            id = "path-routing",
            title = "Path Routing",
            path = "/routes/:projectId/:section?",
            description = "支持动态参数、可选参数和 query/hash 的完整路径导航。",
            docResource = "dev/korafx/examples/navigationtheme/docs/path-routing.md",
            sourceResource = "dev/korafx/examples/navigationtheme/snippets/path-routing.kt",
            section = RouteSection.Core,
        )

        val History = DemoRoute(
            id = "history",
            title = "History",
            path = "/history",
            description = "后退/前进栈与 replace、popToRoot 的交互效果。",
            docResource = "dev/korafx/examples/navigationtheme/docs/history.md",
            sourceResource = "dev/korafx/examples/navigationtheme/snippets/history.kt",
            section = RouteSection.Advanced,
        )

        val Guards = DemoRoute(
            id = "guards",
            title = "Navigation Guards",
            path = "/guards",
            description = "同步/异步 Guard 与可选重定向规则。",
            docResource = "dev/korafx/examples/navigationtheme/docs/guards.md",
            sourceResource = "dev/korafx/examples/navigationtheme/snippets/guards.kt",
            section = RouteSection.Advanced,
        )

        val RouterHost = DemoRoute(
            id = "router-host",
            title = "Router Host",
            path = "/router/:layout?",
            description = "layout + outlet 的子树渲染方式，支持多级共享 shell。",
            docResource = "dev/korafx/examples/navigationtheme/docs/router-host.md",
            sourceResource = "dev/korafx/examples/navigationtheme/snippets/router-host.kt",
            section = RouteSection.Advanced,
        )

        val StateRestoration = DemoRoute(
            id = "state",
            title = "State Restoration",
            path = "/state/:scope?",
            description = "按路由 location 键控的状态持久化与恢复。",
            docResource = "dev/korafx/examples/navigationtheme/docs/state-restoration.md",
            sourceResource = "dev/korafx/examples/navigationtheme/snippets/state-restoration.kt",
            section = RouteSection.Advanced,
        )

        val Transitions = DemoRoute(
            id = "transitions",
            title = "Animation",
            path = "/transitions",
            description = "基于 navigation action 的转场策略。",
            docResource = "dev/korafx/examples/navigationtheme/docs/transitions.md",
            sourceResource = "dev/korafx/examples/navigationtheme/snippets/transitions.kt",
            section = RouteSection.Advanced,
        )

        val RouteData = DemoRoute(
            id = "route-data",
            title = "Route Data",
            path = "/route-data",
            description = "路由级异步数据加载与 loading/error/revalidate。",
            docResource = "dev/korafx/examples/navigationtheme/docs/route-data.md",
            sourceResource = "dev/korafx/examples/navigationtheme/snippets/route-data.kt",
            section = RouteSection.Advanced,
        )

        val LazyRouter = DemoRoute(
            id = "lazy-router",
            title = "Lazy Router",
            path = "/lazy-router",
            description = "路由懒加载与按需初始化 page 的真实示例。",
            docResource = "dev/korafx/examples/navigationtheme/docs/lazy-router.md",
            sourceResource = "dev/korafx/examples/navigationtheme/snippets/lazy-router.kt",
            section = RouteSection.Advanced,
        )

        val RouteResult = DemoRoute(
            id = "route-result",
            title = "Route Result",
            path = "/route-result",
            description = "使用 setResult / results / awaitResult 构建路由返回值。",
            docResource = "dev/korafx/examples/navigationtheme/docs/route-result.md",
            sourceResource = "dev/korafx/examples/navigationtheme/snippets/route-result.kt",
            section = RouteSection.Advanced,
        )

        val all: List<DemoRoute> = listOf(
            Overview,
            PathRouting,
            History,
            Guards,
            RouterHost,
            StateRestoration,
            RouteData,
            LazyRouter,
            RouteResult,
            Transitions,
        )

        fun bySection(section: RouteSection): List<DemoRoute> =
            all.filter { it.section == section }
    }
}

private enum class RouteSection(val label: String) {
    Core("核心能力"),
    Advanced("高级能力"),
}

private enum class LazyRouterDemoRoute(
    override val id: String,
    override val title: String,
    override val path: String,
) : PathRoute {
    Home(id = "lazy-home", title = "Lazy Home", path = "/"),
    Detail(id = "lazy-detail", title = "Lazy Detail", path = "/detail/:itemId"),
    LazyPanel(id = "lazy-panel", title = "Lazy Panel", path = "/panel"),
    ;

    companion object {
        val all: List<LazyRouterDemoRoute> = listOf(Home, Detail, LazyPanel)
    }
}

private enum class RouteResultDemoRoute(
    override val id: String,
    override val title: String,
    override val path: String,
) : PathRoute {
    Entry(id = "result-home", title = "Result Home", path = "/"),
    Picker(id = "result-picker", title = "Result Picker", path = "/picker"),
    ;

    companion object {
        val all: List<RouteResultDemoRoute> = listOf(Entry, Picker)
    }
}

fun main(args: Array<String>) {
    Application.launch(NavigationThemeApp::class.java, *args)
}

class NavigationThemeApp : Application() {
    private val uiScope = MainScope()
    private val themeManager = ThemeManager()
    private val themeController = SceneThemeController(themeManager)
    private val notifications = ToastHost()
    private val blockRouterDemo = MutableStateFlow(true)
    private val transitionPreset = MutableStateFlow(NavigationTransitionProfile.Adaptive)
    private val transitionEnabled = MutableStateFlow(true)
    private val transitionDurationScale = MutableStateFlow(1.0)
    private val navigator = Navigator(
        initialRoute = DemoRoute.Overview,
        routes = DemoRoute.all,
        pageInstancePolicy = PageInstancePolicy.KEEP_ALIVE,
    )
    private val transitionByState = combine(
        navigator.state,
        transitionPreset,
        transitionEnabled,
        transitionDurationScale,
    ) { state, profile, enabled, duration ->
        if (!enabled) {
            RouteTransition.None
        } else {
            profile.resolve(state.navigationType).scaleDuration(duration)
        }
    }

    init {
        navigator.beforeEach { context ->
            if (context.to.route.id == DemoRoute.RouterHost.id && blockRouterDemo.value) {
                NavigationDecision.Block("Router Host 演示当前被守卫拦截。可在 Guards 中关闭后再进入。")
            } else {
                NavigationDecision.Allow
            }
        }
    }

    override fun start(stage: Stage) {
        val root = appShell {
            topBar { topToolbar() }
            navigation { moduleNavigation() }
            content {
                routeHost(
                    scope = uiScope,
                    navigator = navigator,
                    transition = transitionByState,
                    init = {
                        paddingAll(20.0)
                    },
                ) { route ->
                    routeContent(route)
                }
            }
            details {
                tabPane {
                    tab("文档", closable = false) {
                        documentationPane()
                    }
                    tab("源码", closable = false) {
                        sourcePane()
                    }
                    tab("路由状态", closable = false) {
                        stateSnapshotPane()
                    }
                }
            }
            overlay {
                toastHost(uiScope, notifications)
            }
        }

        val scene = Scene(root, 1320.0, 820.0)
        themeController.bind(scene)

        stage.title = "KoraFX Navigation Theme"
        stage.scene = scene
        stage.show()
    }

    private fun routeContent(route: DemoRoute): Node =
        vbox(spacing = 16.0) {
            section(
                title = route.title,
                description = route.description,
            ) {
            when (route) {
                DemoRoute.Overview -> buildOverview()
                DemoRoute.PathRouting -> buildPathRouting()
                DemoRoute.History -> buildHistory()
                DemoRoute.Guards -> buildGuards()
                DemoRoute.RouterHost -> buildRouterHost()
                DemoRoute.StateRestoration -> buildStateRestoration()
                DemoRoute.RouteData -> buildRouteData()
                DemoRoute.LazyRouter -> buildLazyRouter()
                DemoRoute.RouteResult -> buildRouteResultDemo()
                DemoRoute.Transitions -> buildTransitions()
            }
        }
    }

    private fun topToolbar() = toolbar {
            label("KoraFX Navigation + Theme") {
                styleClasses("headline")
            }
            spacer()

            button("返回") {
                onAction { navigator.back() }
                bindDisable(
                    uiScope,
                    navigator.state.map { state -> state.backStack.isEmpty() },
                )
            }
            button("前进") {
                onAction { navigator.forward() }
                bindDisable(
                    uiScope,
                    navigator.state.map { state -> state.forwardStack.isEmpty() },
                )
            }
            button("Home") {
                onAction { navigator.navigate(DemoRoute.Overview.id) }
            }

            menuButton(
                text = "Theme",
                content = {
                    BuiltInThemes.all.forEach { theme ->
                        actionItem(theme.displayName) {
                            setTheme(theme)
                        }
                    }
                },
            )
            ghostButton("Next Theme") {
                onAction { nextTheme() }
            }

            comboBox(
                items = NavigationTransitionProfile.entries.toList(),
                init = {
                    prefWidth = 190.0
                },
            ) {
                render { it.label }
                onSelect { profile ->
                    transitionPreset.value = profile ?: NavigationTransitionProfile.Adaptive
                }
                select(transitionPreset.value)
            }
        }


    private fun moduleNavigation() =
        scrollPane(
            init = {
                isFitToWidth = true
                prefWidth = 270.0
                minWidth = 220.0
                maxWidth = 360.0
            },
        ) {
            content {
                sidebar(width = 250.0, spacing = 12.0) {
                    label("Navigation Routes") {
                        styleClasses(ThemeStyleClass.Headline)
                    }
                    RouteSection.entries.forEach { section ->
                        label(section.label) {
                            styleClasses(ThemeStyleClass.Muted)
                        }
                        DemoRoute.bySection(section).forEach { route ->
                            routeButton(
                                scope = uiScope,
                                navigator = navigator,
                                route = route,
                            ) {
                                maxWidth = Double.MAX_VALUE
                            }
                        }
                    }

                    section("Quick Actions") {
                        button("随机跳转") {
                            onAction {
                                navigator.navigatePath("/routes/${(1000..9999).random()}/files")
                            }
                        }
                        button("清理历史") {
                            onAction {
                                navigator.clearNavigationHistory()
                                notifications.show(
                                    message = "导航历史已清空。",
                                    tone = ToastTone.INFO,
                                )
                            }
                        }
                    }
                }
            }
        }

    private fun documentationPane() =
        scrollPane(
            init = {
                isFitToWidth = true
            },
        ) {
            content {
                panel(spacing = 12.0, padding = 14.0) {
                    label("文档") {
                        styleClasses(ThemeStyleClass.Headline)
                    }
                }.also { container ->
                    container.bindContentWithTransition(
                        scope = uiScope,
                        state = navigator.state,
                        transition = transitionByState,
                    ) { state ->
                        markdownDocument(state.currentRoute.documentation())
                    }
                }
            }
        }

    private fun sourcePane() =
        scrollPane(
            init = {
                isFitToWidth = true
            },
        ) {
            content {
                panel(spacing = 12.0, padding = 14.0) {
                    label("源码片段") {
                        styleClasses(ThemeStyleClass.Headline)
                    }
                    val source = textArea {
                        isEditable = false
                        isWrapText = false
                        prefRowCount = 24
                        this.styleClass += "source-editor-code"
                    }
                    source.bindText(
                        uiScope,
                        navigator.state.map { state -> state.currentRoute.sourceCode() }.distinctUntilChanged(),
                    )
                }
            }
        }

    private fun stateSnapshotPane() =
        scrollPane(
            init = {
                isFitToWidth = true
            },
        ) {
            content {
                panel(spacing = 14.0, padding = 14.0) {
                    label("当前导航状态") {
                        styleClasses(ThemeStyleClass.Headline)
                    }
                    label("Current route").also { label ->
                        label.stateText(uiScope, navigator.state) { state ->
                            "Current route: ${state.currentRoute.title}"
                        }
                    }
                    label("Current path").also { label ->
                        label.stateText(uiScope, navigator.state) { state ->
                            "Current path: ${state.currentLocation.fullPath}"
                        }
                    }
                    label("Navigation type").also { label ->
                        label.stateText(uiScope, navigator.state) { state ->
                            "Navigation type: ${state.navigationType.name}"
                        }
                    }
                    label("Back stack").also { label ->
                        label.stateText(uiScope, navigator.state) { state ->
                            "Back stack: ${state.backStack.size}"
                        }
                    }
                    label("Forward stack").also { label ->
                        label.stateText(uiScope, navigator.state) { state ->
                            "Forward stack: ${state.forwardStack.size}"
                        }
                    }
                    actionBar(alignEnd = false) {
                        button("Can Navigate") {
                            onAction {
                                val decision = navigator.canNavigate(DemoRoute.Overview.id)
                                val message =
                                    when (decision) {
                                        is NavigationDecision.Allow -> "允许导航到 Overview"
                                        is NavigationDecision.Redirect -> "重定向到: ${decision.path ?: decision.routeId}"
                                        is NavigationDecision.Block -> decision.reason ?: "Navigation blocked"
                                    }
                                notifications.show(
                                    message = message,
                                    tone = ToastTone.INFO,
                                )
                            }
                        }
                        button("Can Navigate Path /routes/42/files") {
                            onAction {
                                val decision = navigator.canNavigatePath("/routes/42/files")
                                if (decision == null) {
                                    notifications.show(
                                        message = "No route can match /routes/42/files",
                                        tone = ToastTone.WARNING,
                                    )
                                } else {
                                    notifications.show(
                                        message = when (decision) {
                                            is NavigationDecision.Allow -> "Path 路由可导航"
                                            is NavigationDecision.Redirect -> "Redirect to ${decision.path}"
                                            is NavigationDecision.Block -> decision.reason ?: "blocked"
                                        },
                                        tone = ToastTone.INFO,
                                    )
                                }
                            }
                        }
                        button("Pop To Root") {
                            onAction {
                                if (navigator.popToRoot()) {
                                    notifications.show(
                                        message = "已回到起始路由",
                                        tone = ToastTone.SUCCESS,
                                    )
                                } else {
                                    notifications.show(
                                        message = "已经在起始路由",
                                        tone = ToastTone.INFO,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

    private fun buildOverview() {
        section("功能速览") {
            actionBar(alignEnd = false) {
                button("Path Routing") { onAction { navigator.navigate(DemoRoute.PathRouting.id) } }
                button("History") { onAction { navigator.navigate(DemoRoute.History.id) } }
                button("Guards") { onAction { navigator.navigate(DemoRoute.Guards.id) } }
                button("Lazy Router") { onAction { navigator.navigate(DemoRoute.LazyRouter.id) } }
                button("Route Result") { onAction { navigator.navigate(DemoRoute.RouteResult.id) } }
                button("Route Data") { onAction { navigator.navigate(DemoRoute.RouteData.id) } }
                button("Transitions") { onAction { navigator.navigate(DemoRoute.Transitions.id) } }
            }
        }

        section("运行时能力") {
            label("1) 支持 routeId 与 pathRoute")
            label("2) Back/Forward 历史栈")
            label("3) Guard 与 async guard")
            label("4) routerHost layouts/outlets")
            label("5) 路由级状态持久化与文档化示例")
            label("6) routeLazy 与延迟初始化")
            label("7) 路由结果流(setResult / results / awaitResult)")
        }
    }

    private fun buildPathRouting() {
        lateinit var projectInput: TextField
        lateinit var sectionInput: TextField
        lateinit var tabInput: TextField

        section("路径导航") {
            hbox(spacing = 8.0) {
                projectInput = textField("101")
                sectionInput = textField("overview")
                tabInput = textField("files")
                button("Go by path") {
                    onAction {
                        val project = projectInput.text.trim().takeIf(String::isNotEmpty) ?: "101"
                        val section = sectionInput.text.trim().takeIf(String::isNotEmpty) ?: "overview"
                        val tab = tabInput.text.trim().takeIf(String::isNotEmpty)
                        val fullPath =
                            if (tab == null) {
                                RoutePattern.build("/routes/$project/$section")
                            } else {
                                RoutePattern.build("/routes/$project/$section", query = mapOf("tab" to tab))
                            }
                        navigator.navigatePath(fullPath)
                    }
                }
            }
            section("当前匹配") {
                label("fullPath:").stateText(uiScope, navigator.state) { it.currentLocation.fullPath }
                label("params:").stateText(uiScope, navigator.state) {
                    it.currentLocation.params.entries.joinToString {
                        "${it.key}=${it.value}"
                    }.ifBlank { "无" }
                }
                label("query:").stateText(uiScope, navigator.state) {
                    if (it.currentLocation.query.values.isEmpty()) {
                        "无"
                    } else {
                        it.currentLocation.query.asQueryMap()
                            .entries.joinToString { (key, values) -> "$key=${values.joinToString(",")}" }
                    }
                }
                label("hash:").stateText(uiScope, navigator.state) {
                    it.currentLocation.hash ?: "无"
                }
            }
            actionBar(alignEnd = false) {
                button("加 query") {
                    onAction {
                        val next = navigator.currentLocation.withQuery("tab" to "overview", "sort" to "name")
                        navigator.navigatePath(next)
                    }
                }
                button("加 hash") {
                    onAction {
                        val next = navigator.currentLocation.withHash("line-12")
                        navigator.navigatePath(next)
                    }
                }
            }
        }
    }

    private fun buildHistory() {
        section("Back/Forward + Replace") {
            actionBar(alignEnd = false) {
                button("Push: /routes/${(10..99).random()}/history") {
                    onAction {
                        navigator.navigatePath("/routes/${(10..99).random()}/history")
                    }
                }
                button("Push: /state") {
                    onAction {
                        navigator.navigatePath("/state")
                    }
                }
                button("Replace: /history") {
                    onAction {
                        navigator.replacePath("/history")
                    }
                }
                button("Back") {
                    onAction { navigator.back() }
                    bindDisable(
                        uiScope,
                        navigator.state.map { state -> state.backStack.isEmpty() },
                    )
                }
                button("Forward") {
                    onAction { navigator.forward() }
                    bindDisable(
                        uiScope,
                        navigator.state.map { state -> state.forwardStack.isEmpty() },
                    )
                }
            }
        }

        section("历史面板") {
            label("Back stack").stateText(uiScope, navigator.state) { it.backStack.joinToString(" -> ") { state ->
                state.route.id
            } }
            label("Forward stack").stateText(uiScope, navigator.state) { it.forwardStack.joinToString(" -> ") { state ->
                state.route.id
            } }
        }
        section("高级动作") {
            actionBar(alignEnd = false) {
                button("PopToRoot") {
                    onAction {
                        if (navigator.popToRoot()) {
                            notifications.show(message = "已返回起始页", tone = ToastTone.INFO)
                        } else {
                            notifications.show(message = "当前已是起始页", tone = ToastTone.INFO)
                        }
                    }
                }
                button("Clear history") {
                    onAction {
                        navigator.clearNavigationHistory()
                        notifications.show(message = "历史栈已清空", tone = ToastTone.INFO)
                    }
                }
            }
        }
    }

    private fun buildGuards() {
        section("Guard 示例") {
            val statusLabel: Label =
                label("Guard 状态：Router Host 已放行")
            statusLabel.stateText(uiScope, blockRouterDemo) {
                if (it) "Guard 状态：Router Host 被拦截" else "Guard 状态：Router Host 已放行"
            }
            actionBar(alignEnd = false) {
                button("开启 Guard") {
                    onAction { blockRouterDemo.value = true }
                }
                button("关闭 Guard") {
                    onAction { blockRouterDemo.value = false }
                }
                button("尝试去 RouterHost") {
                    onAction {
                        when (val decision = navigator.canNavigate(DemoRoute.RouterHost.id)) {
                            is NavigationDecision.Allow -> {
                                navigator.navigate(DemoRoute.RouterHost)
                            }
                            is NavigationDecision.Redirect -> {
                                notifications.show(
                                    message = "重定向：${decision.routeId ?: decision.path}",
                                    tone = ToastTone.INFO,
                                )
                            }
                            is NavigationDecision.Block -> {
                                notifications.show(
                                    message = decision.reason ?: "navigation blocked",
                                    tone = ToastTone.WARNING,
                                )
                            }
                        }
                    }
                }
            }
            statusLabel
        }

        section("守卫思路") {
            label("Guard 在同步/异步路径上都可返回 Allow / Block / Redirect。")
            label("建议在权限判断/未保存检查/登录态恢复上集中挂在 navigator.beforeEach。")
        }
    }

    private fun buildRouterHost() {
        section("如何理解 RouterHost") {
            hbox(spacing = 8.0) {
                button("打开 Router Host 示例") {
                    onAction { navigator.navigatePath("/router/project") }
                }
                button("打开子布局示例") {
                    onAction { navigator.navigatePath("/router/dashboard") }
                }
            }
            label("当前示例中 routeHost 已用于主内容切换。")
            label("RouterHost 适合需要 layout + shared shell + 多 outlet 的场景。")
        }
    }

    private fun buildStateRestoration() {
        lateinit var note: TextField
        section("按 location 保存任意状态") {
            note = textField {
                promptText = "当前路由 path 维度存储一段文本"
            }
            actionBar(alignEnd = false) {
                button("Save") {
                    onAction {
                        navigator.saveState("demo-note", note.text.trim().ifEmpty { "" })
                        notifications.show(
                            message = "已保存到 location = ${navigator.currentLocation.fullPath}",
                            tone = ToastTone.SUCCESS,
                        )
                    }
                }
                button("Load") {
                    onAction {
                        note.text = navigator.restoredState<String>("demo-note") ?: ""
                    }
                }
            }
            label("当前 location:").stateText(uiScope, navigator.state) {
                it.currentLocation.fullPath
            }
            label("恢复值:").stateText(uiScope, navigator.state) {
                navigator.restoredState<String>("demo-note", it.currentLocation) ?: "空"
            }
        }
    }

    private fun buildRouteData() {
        val controller = RouteDataController()

        section("routeDataHost 场景化演示") {
            actionBar(alignEnd = false) {
                button("正常加载") {
                    onAction { navigator.navigatePath("/route-data") }
                }
                button("延迟 1.5 秒") {
                    onAction { navigator.navigatePath("/route-data?delay=1500") }
                }
                button("模拟错误") {
                    onAction { navigator.navigatePath("/route-data?mode=error") }
                }
                button("Revalidate") {
                    onAction { controller.revalidate() }
                }
            }

            routeDataHost(
                scope = uiScope,
                navigator = navigator,
                controller = controller,
                cache = true,
                init = {
                    paddingAll(4.0)
                },
                load = { context ->
                    val delayMs = context.query.int("delay") ?: 300
                    delay(delayMs.coerceIn(80, 3000).toLong())
                    if (context.query["mode"] == "error") {
                        throw IllegalStateException("mock loader error")
                    }
                    "load-ok:${context.location.fullPath}:${System.currentTimeMillis()}"
                },
                loading = { context ->
                    loadingState("Loading ${context.route.title}...")
                },
                failed = { context, error ->
                    errorState(
                        title = "${context.route.title} 加载失败",
                        message = error.message.orEmpty(),
                    )
                },
            ) { _, value ->
                vbox(10.0) {
                    label(value)
                }
            }
        }
    }

    private fun buildLazyRouter() {
        val localNavigator = Navigator(
            initialRoute = LazyRouterDemoRoute.Home,
            routes = LazyRouterDemoRoute.all,
            pageInstancePolicy = PageInstancePolicy.KEEP_ALIVE,
        )
        val panelInitCount = AtomicInteger(0)

        section("路由按需加载（routeLazy）") {
            label("本示例使用独立 navigator 演示 routeLazy 何时触发构建。")
            label("进入 Lazy Panel 前，相关内容不会创建。")
            label("离开后再次进入也不会重复初始化。")
            routerHost(
                scope = uiScope,
                navigator = localNavigator,
                transition = RouteTransition.Fade(),
                init = {
                    styleClasses("lazy-route-host")
                },
            ) {
                route(LazyRouterDemoRoute.Home) {
                    vbox(10.0) {
                        label("当前路由：${localNavigator.currentLocation.fullPath}")
                        actionBar(alignEnd = false) {
                            button("打开详情（参数路由）") {
                                onAction {
                                    localNavigator.navigatePath("/detail/007")
                                }
                            }
                            button("打开懒加载面板") {
                                onAction {
                                    localNavigator.navigate(LazyRouterDemoRoute.LazyPanel)
                                }
                            }
                        }
                    }
                }
                route(LazyRouterDemoRoute.Detail) {
                    vbox(10.0) {
                        label("详情页参数：${localNavigator.currentLocation.params["itemId"]}")
                        actionBar(alignEnd = false) {
                            button("返回") {
                                onAction { localNavigator.back() }
                            }
                        }
                    }
                }
                routeLazy(LazyRouterDemoRoute.LazyPanel) {
                    {
                        val currentInit = panelInitCount.incrementAndGet()
                        vbox(10.0) {
                            label("这段内容只在首次导航到该路由时创建。")
                            label("初始化次数：$currentInit")
                            actionBar(alignEnd = false) {
                                button("返回") {
                                    onAction { localNavigator.back() }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun buildRouteResultDemo() {
        val localNavigator = Navigator(
            initialRoute = RouteResultDemoRoute.Entry,
            routes = RouteResultDemoRoute.all,
            pageInstancePolicy = PageInstancePolicy.KEEP_ALIVE,
        )
        val resultKey = navigationResultKey<String>("sample-route-result")
        val resultState = MutableStateFlow("未选择")

        uiScope.launch {
            localNavigator.results(resultKey).collect { value ->
                resultState.value = value
            }
        }

        section("路由返回值") {
            label("路由结果约定：下游页面通过 setResult 写回上游。")
            label("当前结果：").stateText(uiScope, resultState) { "当前结果：$it" }
            actionBar(alignEnd = false) {
                button("打开 Picker 页面") {
                    onAction { localNavigator.navigate(RouteResultDemoRoute.Picker) }
                }
                button("等待下一次 Picker 返回") {
                    onAction {
                        uiScope.launch {
                            val picked = localNavigator.awaitResult(resultKey)
                            notifications.show(
                                message = "awaitResult 接收: $picked",
                                tone = ToastTone.INFO,
                            )
                        }
                    }
                }
            }

            routerHost(
                scope = uiScope,
                navigator = localNavigator,
                transition = RouteTransition.Fade(),
                init = {
                    styleClasses("route-result-host")
                },
            ) {
                route(RouteResultDemoRoute.Entry) {
                    vbox(10.0) {
                        label("当前路由：${localNavigator.currentLocation.fullPath}")
                        label("选择一个项目继续：")
                        button("Picker") {
                            onAction { localNavigator.navigate(RouteResultDemoRoute.Picker) }
                        }
                    }
                }
                route(RouteResultDemoRoute.Picker) {
                    vbox(10.0) {
                        label("选择后会返回上一页并携带结果。")
                        button("选择 Alpha") {
                            onAction {
                                localNavigator.setResult(resultKey, "alpha")
                                localNavigator.back()
                            }
                        }
                        button("选择 Beta") {
                            onAction {
                                localNavigator.setResult(resultKey, "beta")
                                localNavigator.back()
                            }
                        }
                        button("选择 Gamma") {
                            onAction {
                                localNavigator.setResult(resultKey, "gamma")
                                localNavigator.back()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun buildTransitions() {
        section("转场策略") {
            label("NavigationType 说明：")
            vbox(spacing = 6.0) {
                label("- INITIAL：首次加载 / create")
                label("- PUSH：新路由入栈")
                label("- POP：后退/前进")
                label("- REPLACE：同级替换")
            }
            actionBar(alignEnd = false) {
                        button("Adaptive") {
                    onAction { transitionPreset.value = NavigationTransitionProfile.Adaptive }
                }
                button("Push Slide") {
                    onAction { transitionPreset.value = NavigationTransitionProfile.PushSlide }
                }
                button("Fade") {
                    onAction { transitionPreset.value = NavigationTransitionProfile.Fade }
                }
                button("Scale") {
                    onAction { transitionPreset.value = NavigationTransitionProfile.Scale }
                }
                button("None") {
                    onAction { transitionPreset.value = NavigationTransitionProfile.None }
                }
            }

            section("转场参数") {
                hbox(spacing = 12.0) {
                    checkBox("启用转场") {
                        isSelected = transitionEnabled.value
                        selectedProperty().addListener { _, _, selected ->
                            transitionEnabled.value = selected
                        }
                    }
                    slider(
                        min = 0.5,
                        max = 2.0,
                        value = transitionDurationScale.value,
                        init = {
                            prefWidth = 210.0
                            valueProperty().addListener { _, _, value ->
                                transitionDurationScale.value = value.toDouble()
                            }
                        },
                    )
                    label("x1.0")
                    label("当前倍率：").stateText(uiScope, transitionDurationScale) {
                        "x${"%.2f".format(it)}"
                    }
                }
            }
            actionBar(alignEnd = false) {
                button("Push") {
                    onAction { navigator.navigatePath("/routes/${(1000..2000).random()}/overview") }
                }
                button("Replace") {
                    onAction { navigator.replace(DemoRoute.Overview) }
                }
                button("Pop") {
                    onAction { navigator.back() }
                    bindDisable(
                        uiScope,
                        navigator.state.map { state -> state.backStack.isEmpty() },
                    )
                }
            }
        }
    }

    private fun nextTheme() {
        themeManager.nextTheme()
        notifications.show(
            message = "Theme switched to ${themeManager.currentTheme().displayName}.",
            tone = ToastTone.SUCCESS,
        )
    }

    private fun setTheme(theme: KoraTheme) {
        themeManager.setTheme(theme)
        notifications.show(
            message = "Theme switched to ${theme.displayName}.",
            tone = ToastTone.SUCCESS,
        )
    }

    override fun stop() {
        uiScope.cancel()
        themeController.dispose()
    }
}

private fun DemoRoute.documentation(): String = routeResourceText(docResource, fallback = "# Missing documentation\n")

private fun DemoRoute.sourceCode(): String = routeResourceText(sourceResource, fallback = "// Missing source snippet\n")

private val routeDocCache = linkedMapOf<String, String>()

private fun routeResourceText(
    resource: String,
    fallback: String,
): String {
    return routeDocCache.getOrPut(resource) {
        NavigationThemeApp::class.java.classLoader
            .getResourceAsStream(resource)
            ?.bufferedReader()
            ?.readText()
            ?: fallback
    }
}
