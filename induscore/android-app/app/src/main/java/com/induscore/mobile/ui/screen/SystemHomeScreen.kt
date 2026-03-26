package com.induscore.mobile.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.induscore.mobile.ui.components.AgentBrandLogoBadge
import com.induscore.mobile.ui.components.AppPrimaryButton
import com.induscore.mobile.ui.components.DeepIndusBrandHeader
import com.induscore.mobile.ui.components.SemanticIconBadge

data class HomeModule(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val iconTint: Color,
    val isAgentEntry: Boolean = false,
    val isPlaceholder: Boolean = false,
    val onClick: () -> Unit
)

@Composable
fun SystemHomeScreen(
    modules: List<HomeModule>,
    onOpenAgent: () -> Unit
) {
    val agentModule = modules.firstOrNull { it.isAgentEntry }
    val regularModules = modules.filterNot { it.isAgentEntry || it.isPlaceholder }
    val displayModules = if (agentModule == null) {
        regularModules
    } else {
        buildList {
            addAll(regularModules)
            if (regularModules.size % 2 == 0) {
                add(
                    HomeModule(
                        title = "",
                        description = "",
                        icon = regularModules.firstOrNull()?.icon ?: agentModule.icon,
                        iconTint = Color.Transparent,
                        isPlaceholder = true,
                        onClick = {}
                    )
                )
            }
            add(agentModule)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            DeepIndusBrandHeader(titleSizeSp = 20)
            AgentBrandLogoBadge(
                size = 42.dp,
                iconSize = 20.dp,
                modifier = Modifier.clickable(onClick = onOpenAgent)
            )
        }
        Text(
            text = "系统主页",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 8.dp)
        )
        Text(
            text = "请选择功能模块",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
        )

        BoxWithConstraints(modifier = Modifier.weight(1f, fill = true)) {
            val rowCount = ((displayModules.size + 1) / 2).coerceAtLeast(1)
            val rowGap = 8.dp
            val cardHeight = (maxHeight - rowGap * (rowCount - 1)) / rowCount

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(rowGap),
                userScrollEnabled = false,
                modifier = Modifier.fillMaxSize()
            ) {
                items(displayModules) { module ->
                    if (module.isPlaceholder) {
                        Column(modifier = Modifier.height(cardHeight)) {}
                        return@items
                    }
                    Card(
                        modifier = Modifier.height(cardHeight),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = module.title, style = MaterialTheme.typography.titleMedium)
                                if (module.isAgentEntry) {
                                    AgentBrandLogoBadge(size = 30.dp, iconSize = 16.dp)
                                } else {
                                    SemanticIconBadge(icon = module.icon, tint = module.iconTint)
                                }
                            }
                            Text(
                                text = module.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .padding(top = 6.dp, bottom = 8.dp)
                                    .height(46.dp)
                            )
                            AppPrimaryButton(text = "进入", onClick = module.onClick)
                        }
                    }
                }
            }
        }
    }
}
