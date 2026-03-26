package com.induscore.mobile.ui

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.AssignmentTurnedIn
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.QueryStats
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navOptions
import com.induscore.mobile.di.ServiceLocator
import com.induscore.mobile.ui.screen.FeaturePlaceholderScreen
import com.induscore.mobile.ui.screen.AgentChatScreen
import com.induscore.mobile.ui.screen.HomeModule
import com.induscore.mobile.ui.screen.LoginScreen
import com.induscore.mobile.ui.screen.MetricsScreen
import com.induscore.mobile.ui.screen.NotificationsScreen
import com.induscore.mobile.ui.screen.ProfileScreen
import com.induscore.mobile.ui.screen.ReviewTaskDetailScreen
import com.induscore.mobile.ui.screen.ReviewTaskListScreen
import com.induscore.mobile.ui.screen.SamplingUploadScreen
import com.induscore.mobile.ui.screen.SystemHomeScreen
import com.induscore.mobile.ui.screen.UploadStatusScreen
import com.induscore.mobile.ui.screen.WorkbenchScreen

private object Route {
    const val Login = "login"
    const val Home = "home"
    const val Workbench = "workbench"
    const val TaskList = "task_list"
    const val Upload = "upload"
    const val UploadStatus = "upload_status"
    const val Notifications = "notifications"
    const val Metrics = "metrics"
    const val Profile = "profile"
    const val Agent = "agent?scene={scene}&taskId={taskId}&idempotencyKey={idempotencyKey}&prefill={prefill}"
    const val TaskDetail = "task_detail/{taskId}"
}

private val metricsAdminRoles = setOf("super_admin", "admin")

private fun canAccessMetrics(roleCodes: Set<String>): Boolean {
    return roleCodes.any { metricsAdminRoles.contains(it) }
}

private fun buildAgentRoute(
    scene: String = "general",
    taskId: Long? = null,
    idempotencyKey: String? = null,
    prefill: String? = null
): String {
    val encodedScene = Uri.encode(scene)
    val encodedTaskId = Uri.encode(taskId?.toString() ?: "")
    val encodedKey = Uri.encode(idempotencyKey ?: "")
    val encodedPrefill = Uri.encode(prefill ?: "")
    return "agent?scene=$encodedScene&taskId=$encodedTaskId&idempotencyKey=$encodedKey&prefill=$encodedPrefill"
}

@Composable
fun InduscoreMobileApp() {
    val navController = rememberNavController()
    val roleCodes by ServiceLocator.sessionManager.roleCodesFlow
        .collectAsStateWithLifecycle(initialValue = emptySet())
    NavHost(
        navController = navController,
        startDestination = Route.Login
    ) {
        composable(Route.Login) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(
                        Route.Home,
                        navOptions {
                            popUpTo(Route.Login) { inclusive = true }
                        }
                    )
                }
            )
        }
        composable(Route.Home) {
            val modules = buildList {
                add(
                    HomeModule(
                        title = "工作台",
                        description = "查看待复核、告警与上传队列",
                        icon = Icons.Outlined.Dashboard,
                        iconTint = Color(0xFF3B82F6)
                    ) { navController.navigate(Route.Workbench) }
                )
                add(
                    HomeModule(
                        title = "复核任务",
                        description = "查看并处理复核任务",
                        icon = Icons.Outlined.AssignmentTurnedIn,
                        iconTint = Color(0xFF8B5CF6)
                    ) { navController.navigate(Route.TaskList) }
                )
                add(
                    HomeModule(
                        title = "抽检上传",
                        description = "上传抽检图片并触发检测",
                        icon = Icons.Outlined.CloudUpload,
                        iconTint = Color(0xFF0EA5E9)
                    ) { navController.navigate(Route.Upload) }
                )
                add(
                    HomeModule(
                        title = "上传状态",
                        description = "查看上传任务处理状态",
                        icon = Icons.Outlined.TaskAlt,
                        iconTint = Color(0xFF10B981)
                    ) { navController.navigate(Route.UploadStatus) }
                )
                add(
                    HomeModule(
                        title = "通知中心",
                        description = "查看严重告警与提醒",
                        icon = Icons.Outlined.Notifications,
                        iconTint = Color(0xFFEF4444)
                    ) { navController.navigate(Route.Notifications) }
                )
                if (canAccessMetrics(roleCodes)) {
                    add(
                        HomeModule(
                            title = "试点指标",
                            description = "查看 A-01 试点达标情况",
                            icon = Icons.Outlined.QueryStats,
                            iconTint = Color(0xFFF59E0B)
                        ) { navController.navigate(Route.Metrics) }
                    )
                }
                add(
                    HomeModule(
                        title = "个人信息",
                        description = "查看当前账号与角色信息",
                        icon = Icons.Outlined.AccountCircle,
                        iconTint = Color(0xFF38BDF8)
                    ) { navController.navigate(Route.Profile) }
                )
                add(
                    HomeModule(
                        title = "AI 助手",
                        description = "跨模块业务问答与分析建议",
                        icon = Icons.Outlined.TaskAlt,
                        iconTint = Color(0xFF8B5CF6),
                        isAgentEntry = true
                    ) { navController.navigate(buildAgentRoute()) }
                )
            }
            SystemHomeScreen(
                modules = modules,
                onOpenAgent = { navController.navigate(buildAgentRoute()) }
            )
        }
        composable(Route.Workbench) {
            WorkbenchScreen(
                onOpenTasks = { navController.navigate(Route.TaskList) },
                onBackHome = { navController.navigate(Route.Home) }
            )
        }
        composable(Route.TaskList) {
            ReviewTaskListScreen(
                onOpenTaskDetail = { taskId ->
                    navController.navigate("task_detail/$taskId")
                },
                onBackHome = { navController.navigate(Route.Home) }
            )
        }
        composable(Route.Upload) {
            SamplingUploadScreen(onBackHome = { navController.navigate(Route.Home) })
        }
        composable(Route.UploadStatus) {
            UploadStatusScreen(
                onBackHome = { navController.navigate(Route.Home) },
                onAskAgent = { idempotencyKey, prefill ->
                    navController.navigate(
                        buildAgentRoute(
                            scene = "upload_status",
                            idempotencyKey = idempotencyKey,
                            prefill = prefill
                        )
                    )
                }
            )
        }
        composable(Route.Notifications) {
            NotificationsScreen(onBackHome = { navController.navigate(Route.Home) })
        }
        composable(Route.Metrics) {
            if (canAccessMetrics(roleCodes)) {
                MetricsScreen(onBackHome = { navController.navigate(Route.Home) })
            } else {
                FeaturePlaceholderScreen(
                    title = "权限受限",
                    description = "试点指标仅管理员可见，请联系管理员开通权限。",
                    onBackHome = {
                        navController.navigate(Route.Home) {
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
        composable(Route.Profile) {
            ProfileScreen(onBackHome = { navController.navigate(Route.Home) })
        }
        composable(
            route = Route.Agent,
            arguments = listOf(
                navArgument("scene") { type = NavType.StringType; defaultValue = "general" },
                navArgument("taskId") { type = NavType.StringType; defaultValue = "" },
                navArgument("idempotencyKey") { type = NavType.StringType; defaultValue = "" },
                navArgument("prefill") { type = NavType.StringType; defaultValue = "" }
            )
        ) { backStackEntry ->
            val sceneArg = backStackEntry.arguments?.getString("scene").orEmpty().ifBlank { "general" }
            val taskIdArg = backStackEntry.arguments?.getString("taskId").orEmpty().toLongOrNull()
            val idempotencyKeyArg = backStackEntry.arguments?.getString("idempotencyKey").orEmpty().ifBlank { null }
            val prefillArg = backStackEntry.arguments?.getString("prefill").orEmpty().ifBlank { null }
            AgentChatScreen(
                initialScene = sceneArg,
                initialTaskId = taskIdArg,
                initialIdempotencyKey = idempotencyKeyArg,
                initialPrefill = prefillArg,
                onBackHome = {
                    navController.navigate(Route.Home) {
                        launchSingleTop = true
                    }
                }
            )
        }
        composable(
            route = Route.TaskDetail,
            arguments = listOf(navArgument("taskId") { type = NavType.LongType })
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getLong("taskId") ?: return@composable
            ReviewTaskDetailScreen(
                taskId = taskId,
                onBack = { navController.popBackStack() },
                onBackHome = {
                    navController.navigate(Route.Home) {
                        launchSingleTop = true
                    }
                },
                onAskAgent = { askTaskId, prefill ->
                    navController.navigate(
                        buildAgentRoute(
                            scene = "review_detail",
                            taskId = askTaskId,
                            prefill = prefill
                        )
                    )
                }
            )
        }
    }
}
