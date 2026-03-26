package com.induscore.mobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.SmartToy
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DeepIndusBrandHeader(
    modifier: Modifier = Modifier,
    titleSizeSp: Int = 24
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        DeepIndusLogoIcon()
        Text(
            text = buildAnnotatedString {
                pushStyle(SpanStyle(color = Color.White))
                append("DEEP")
                pushStyle(SpanStyle(color = MaterialTheme.colorScheme.primary))
                append("INDUS")
            },
            fontSize = titleSizeSp.sp,
            fontWeight = FontWeight.Bold,
            fontStyle = FontStyle.Italic
        )
    }
}

@Composable
fun DeepIndusLogoIcon() {
    Box(
        modifier = Modifier
            .size(46.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                brush = Brush.linearGradient(
                    listOf(Color(0xFF2563EB), Color(0xFF4338CA))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Outer industrial frame
        Box(
            modifier = Modifier
                .size(27.dp)
                .border(2.dp, Color(0xFFE2E8F0), RoundedCornerShape(6.dp))
                .padding(3.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(4.dp))
            )
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .align(Alignment.Center)
                    .clip(RoundedCornerShape(99.dp))
                    .background(Color.White)
            )
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .align(Alignment.TopEnd)
                    .clip(RoundedCornerShape(99.dp))
                    .background(Color(0xFF93C5FD))
            )
        }
    }
}

/**
 * Agent 品牌图标：贴近 Web 端风格的紫色圆形机器人标识。
 */
@Composable
fun AgentBrandLogoBadge(
    modifier: Modifier = Modifier,
    size: Dp = 34.dp,
    iconSize: Dp = 18.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF4F46E5), Color(0xFF6366F1))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Rounded.SmartToy,
            contentDescription = "Agent",
            tint = Color.White,
            modifier = Modifier.size(iconSize)
        )
    }
}
