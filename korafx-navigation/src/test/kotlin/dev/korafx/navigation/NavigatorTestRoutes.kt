package dev.korafx.navigation

internal data class NavigatorTestRoute(
    override val id: String,
    override val title: String,
) : Route {
    companion object {
        val Home = NavigatorTestRoute(id = "home", title = "Home")
        val Settings = NavigatorTestRoute(id = "settings", title = "Settings")

        val all: List<NavigatorTestRoute>
            get() = listOf(Home, Settings)
    }
}

internal data class PathTestRoute(
    override val id: String,
    override val title: String,
    override val path: String,
    override val meta: RouteMeta = RouteMeta.Empty,
) : PathRoute {
    companion object {
        val Home = PathTestRoute("home", "Home", "/")
        val Project = PathTestRoute(
            id = "project",
            title = "Project",
            path = "/projects/:projectId/:tab?",
            meta = routeMeta("project" to true),
        )
        val ProjectSettings = PathTestRoute("project-settings", "Project Settings", "/projects/settings")
        val Files = PathTestRoute("files", "Files", "/files/*")
        val LocalizedWorkspace = PathTestRoute("localized-workspace", "Localized Workspace", "/:locale?/workspace/:section")
        val Settings = PathTestRoute("settings", "Settings", "/settings")
        val Admin = PathTestRoute(
            id = "admin",
            title = "Admin",
            path = "/admin",
            meta = routeMeta("requiresAuth" to true),
        )
        val Login = PathTestRoute("login", "Login", "/login")

        val all: List<PathTestRoute>
            get() = listOf(Home, Project, ProjectSettings, Files, LocalizedWorkspace, Settings, Admin, Login)
    }
}

internal data class NestedPathRouteSpec(
    override val id: String,
    override val title: String,
    override val path: String,
    override val parentRouteId: String? = null,
    override val isIndexRoute: Boolean = false,
) : NestedPathRoute {
    override val meta: RouteMeta = RouteMeta.Empty

    companion object {
        val Root = NestedPathRouteSpec(id = "nested-root", title = "Nested Root", path = "/")
        val Workspace = NestedPathRouteSpec(
            id = "nested-workspace",
            title = "Workspace",
            path = "workspace",
            parentRouteId = Root.id,
        )
        val WorkspaceIndex = NestedPathRouteSpec(
            id = "nested-workspace-index",
            title = "Workspace Index",
            path = "",
            parentRouteId = Workspace.id,
            isIndexRoute = true,
        )
        val WorkspaceSection = NestedPathRouteSpec(
            id = "nested-workspace-section",
            title = "Workspace Section",
            path = "users/:userId",
            parentRouteId = Workspace.id,
        )

        val all: List<NestedPathRouteSpec>
            get() = listOf(Root, Workspace, WorkspaceIndex, WorkspaceSection)
    }
}
