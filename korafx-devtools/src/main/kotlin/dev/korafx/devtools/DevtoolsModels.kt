package dev.korafx.devtools

import dev.korafx.navigation.Route
import javafx.scene.Node
import org.kordamp.ikonli.bootstrapicons.BootstrapIcons
import java.util.Locale

internal data class NodeDescriptor(
    val node: Node,
) {
    override fun toString(): String =
        buildString {
            append(node.javaClass.simpleName)
            node.id?.takeIf(String::isNotBlank)?.let { append("#$it") }
            if (node.styleClass.isNotEmpty()) {
                append(node.styleClass.joinToString(prefix = ".", separator = "."))
            }
        }
}

internal data class RouteRow(
    val route: Route,
    val path: String,
    val active: Boolean,
)

internal data class DevtoolsRoute(
    override val id: String,
    override val title: String,
    val panel: KoraDevtoolsPanel,
    val icon: BootstrapIcons,
) : Route

internal fun KoraDevtoolsPanel.toRoute(messages: DevtoolsMessages): DevtoolsRoute =
    when (this) {
        KoraDevtoolsPanel.SceneGraph ->
            DevtoolsRoute("scene-graph", messages.sceneGraph, this, BootstrapIcons.DIAGRAM_3)
        KoraDevtoolsPanel.Inspector ->
            DevtoolsRoute("inspector", messages.inspector, this, BootstrapIcons.BINOCULARS)
        KoraDevtoolsPanel.Navigation ->
            DevtoolsRoute("navigation", messages.navigation, this, BootstrapIcons.SIGNPOST_SPLIT)
        KoraDevtoolsPanel.Theme ->
            DevtoolsRoute("theme", messages.theme, this, BootstrapIcons.PALETTE)
        KoraDevtoolsPanel.Performance ->
            DevtoolsRoute("performance", messages.performance, this, BootstrapIcons.SPEEDOMETER2)
    }

internal data class DevtoolsMessages(
    val title: String,
    val subappBadge: String,
    val statusText: String,
    val shortcutHelp: String,
    val sceneGraph: String,
    val inspector: String,
    val navigation: String,
    val theme: String,
    val performance: String,
    val autoRefresh: String,
    val refreshIntervalMs: String,
    val topRoutes: String,
    val historyLimit: String,
    val recentSamples: String,
    val copy: String,
    val dockLeft: String,
    val dockRight: String,
    val dockBottom: String,
    val openWindow: String,
    val liveTree: String,
    val pickNode: String,
    val clear: String,
    val refresh: String,
    val selectedNode: String,
    val selectNodeHint: String,
    val registeredRoutes: String,
    val navigate: String,
    val navigatePath: String,
    val back: String,
    val forward: String,
    val activeTheme: String,
    val currentRoute: String,
    val currentLocation: String,
    val routePath: String,
    val pageInstancePolicy: String,
    val routes: String,
    val backStack: String,
    val forwardStack: String,
    val noNodeSelected: String,
    val noPerformanceData: String,
    val nodeType: String,
    val nodeId: String,
    val nodeStyleClass: String,
    val visible: String,
    val managed: String,
    val disabled: String,
    val focused: String,
    val hover: String,
    val pseudoClassStates: String,
    val cssMetadata: String,
    val displayName: String,
    val colors: String,
    val typography: String,
    val spacing: String,
    val radii: String,
) {
    companion object {
        fun forLanguage(language: KoraDevtoolsLanguage): DevtoolsMessages =
            when (language.resolve()) {
                KoraDevtoolsLanguage.CHINESE -> chinese()
                KoraDevtoolsLanguage.ENGLISH,
                KoraDevtoolsLanguage.SYSTEM,
                -> english()
            }

        private fun KoraDevtoolsLanguage.resolve(): KoraDevtoolsLanguage =
            when (this) {
                KoraDevtoolsLanguage.SYSTEM ->
                    if (Locale.getDefault().language.equals("zh", ignoreCase = true)) {
                        KoraDevtoolsLanguage.CHINESE
                    } else {
                        KoraDevtoolsLanguage.ENGLISH
                    }
                else -> this
            }

        private fun english(): DevtoolsMessages =
            DevtoolsMessages(
                title = "KoraFX DevTools",
                subappBadge = "Subapp",
                statusText = "Inspect live JavaFX state without leaving the application.",
                shortcutHelp = "Open: Ctrl+Shift+I | Pick: Ctrl+Shift+C",
                sceneGraph = "Scene Graph",
                inspector = "Inspector",
                navigation = "Navigation",
                theme = "Theme",
                performance = "Performance",
                autoRefresh = "Auto Refresh",
                refreshIntervalMs = "Refresh Interval (ms)",
                topRoutes = "Top Routes",
                historyLimit = "History",
                recentSamples = "Recent Samples",
                copy = "Copy",
                dockLeft = "Left",
                dockRight = "Right",
                dockBottom = "Bottom",
                openWindow = "Window",
                liveTree = "Live JavaFX node tree",
                pickNode = "Pick Node",
                clear = "Clear",
                refresh = "Refresh",
                selectedNode = "Selected Node",
                selectNodeHint = "Select a node in Scene Graph or use Pick Node.",
                registeredRoutes = "Registered routes",
                navigate = "Navigate",
                navigatePath = "Navigate path",
                back = "Back",
                forward = "Forward",
                activeTheme = "Active theme",
                currentRoute = "currentRoute",
                currentLocation = "currentLocation",
                routePath = "path",
                pageInstancePolicy = "pageInstancePolicy",
                routes = "routes",
                backStack = "backStack",
                forwardStack = "forwardStack",
                noNodeSelected = "No node selected.",
                noPerformanceData = "No render metrics yet.",
                nodeType = "type",
                nodeId = "id",
                nodeStyleClass = "styleClass",
                visible = "visible",
                managed = "managed",
                disabled = "disabled",
                focused = "focused",
                hover = "hover",
                pseudoClassStates = "pseudoClassStates",
                cssMetadata = "cssMetadata",
                displayName = "displayName",
                colors = "colors",
                typography = "typography",
                spacing = "spacing",
                radii = "radii",
            )

        private fun chinese(): DevtoolsMessages =
            DevtoolsMessages(
                title = "KoraFX 开发者工具",
                subappBadge = "子应用",
                statusText = "在应用运行时检查 JavaFX 状态。",
                shortcutHelp = "打开: Ctrl+Shift+I | 选择节点: Ctrl+Shift+C",
                sceneGraph = "节点树",
                inspector = "检查器",
                navigation = "导航",
                theme = "主题",
                performance = "性能",
                autoRefresh = "自动刷新",
                refreshIntervalMs = "刷新间隔 (ms)",
                topRoutes = "Top 路由数",
                historyLimit = "历史长度",
                recentSamples = "最近样本",
                copy = "复制",
                dockLeft = "左侧",
                dockRight = "右侧",
                dockBottom = "底部",
                openWindow = "窗口",
                liveTree = "实时 JavaFX 节点树",
                pickNode = "选择节点",
                clear = "清空",
                refresh = "刷新",
                selectedNode = "当前节点",
                selectNodeHint = "在节点树中选择节点，或使用选择节点模式。",
                registeredRoutes = "已注册路由",
                navigate = "跳转",
                navigatePath = "按路径跳转",
                back = "后退",
                forward = "前进",
                activeTheme = "当前主题",
                currentRoute = "当前路由",
                currentLocation = "当前位置",
                routePath = "路径",
                pageInstancePolicy = "页面实例策略",
                routes = "路由数量",
                backStack = "后退栈",
                forwardStack = "前进栈",
                noNodeSelected = "未选择节点。",
                noPerformanceData = "尚无渲染指标数据。",
                nodeType = "类型",
                nodeId = "id",
                nodeStyleClass = "样式类",
                visible = "可见",
                managed = "参与布局",
                disabled = "禁用",
                focused = "聚焦",
                hover = "悬停",
                pseudoClassStates = "伪类状态",
                cssMetadata = "CSS 元数据",
                displayName = "显示名称",
                colors = "颜色",
                typography = "字体",
                spacing = "间距",
                radii = "圆角",
            )
    }
}
