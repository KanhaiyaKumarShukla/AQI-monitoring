package com.example.sih.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ChipColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.util.lerp
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb

@Composable
fun colorScheme(darkTheme: Boolean, themeColor: ThemeColor): ColorScheme {
    return if (darkTheme) {
        darkColorScheme(
            primary = themeColor.color,
            onPrimary = Color.White,
            secondary = themeColor.color.copy(alpha = 0.8f),
            tertiary = themeColor.color.copy(alpha = 0.6f),
            background = Color(0xFF121212),
            surface = themeColor.color.copy(alpha = 0.2f),
            onSurface = Color.White,
            onSecondary = Color.LightGray,
            primaryContainer = themeColor.color.copy(alpha = 0.2f),
            secondaryContainer = themeColor.color.copy(alpha = 0.1f),
            onSurfaceVariant = Color.LightGray.copy(alpha = 0.8f)
        )
    } else {
        lightColorScheme(
            primary = themeColor.color,
            onPrimary = Color.White,
            secondary = themeColor.color.copy(alpha = 0.8f),
            tertiary = themeColor.color.copy(alpha = 0.6f),
            background = Color.White,
            surface = themeColor.color.copy(alpha = 0.1f),
            onSurface = Color.Black,
            onSecondary = Color.DarkGray,
            primaryContainer = themeColor.color.copy(alpha = 0.1f),
            secondaryContainer = themeColor.color.copy(alpha = 0.05f),
            onSurfaceVariant = Color.DarkGray.copy(alpha = 0.8f)
        )
    }
}


@Composable
fun AppTheme(
    darkTheme: Boolean = ThemeManager.darkTheme.value,
    themeColor: ThemeColor = ThemeManager.themeColor.value,
    content: @Composable () -> Unit
) {
    val colors = colorScheme(darkTheme, themeColor)

    MaterialTheme(
        colorScheme = colors,
        typography = Typography
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = colors.background
        ) {
            content()
        }
    }
}



@Composable
fun CustomSurface(
    modifier: Modifier = Modifier,
    darkTheme: Boolean,
    themeColor: ThemeColor,
    content: @Composable () -> Unit
) {
    val surfaceColor = if (darkTheme) {
        themeColor.color.copy(alpha = 0.2f)
    } else {
        themeColor.color.copy(alpha = 0.1f)
    }

    Surface(
        modifier = modifier,
        color = surfaceColor
    ) {
        content()
    }
}


@Composable
fun buttonColors(themeColor: ThemeColor, darkTheme: Boolean): ButtonColors {
    val buttonBackground = if (darkTheme) {
        themeColor.color.copy(alpha = 0.8f)
    } else {
        themeColor.color
    }

    return ButtonDefaults.buttonColors(
        containerColor = buttonBackground,
        contentColor = Color.White
    )
}

@Composable
fun buttonShape(): RoundedCornerShape {
    return RoundedCornerShape(12.dp)
}

@Composable
fun tabColors(
    themeColor: ThemeColor,
    darkTheme: Boolean,
    selectedIndex: Int,
    tabCount: Int
) {
    val indicatorColor = themeColor.color
    val shape = RoundedCornerShape(50)

    TabRow(
        selectedTabIndex = selectedIndex,
        indicator = { tabPositions ->
            val currentTabPosition = tabPositions[selectedIndex]
            Box(
                Modifier
                    .tabIndicatorOffset(currentTabPosition)
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(shape)
                    .drawBehind {
                        drawIntoCanvas { canvas ->
                            drawCircle(
                                brush = SolidColor(indicatorColor),  // Use brush parameter
                                radius = size.height / 2f,
                                center = center
                            )
                        }
                    }
            )
        }
    ) {
        repeat(tabCount) { index ->
            Tab(
                selected = index == selectedIndex,
                onClick = { /* Handle tab click */ },
                text = { androidx.compose.material3.Text("Tab $index") }
            )
        }
    }
}


/*
private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40,
    surface = Color(0xFFFFFBFE),
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFE7E0EC)
)

// Define your custom shapes
private val AppShapes = Shapes(
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(8.dp),
    large = RoundedCornerShape(16.dp)
)

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = AppShapes,  // Use the defined shapes
        content = content
    )
}

 */

@Composable
fun AppThemee(
    darkTheme: Boolean = ThemeManager.darkTheme.value,
    themeColor: ThemeColor = ThemeManager.themeColor.value,
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) {
        darkColorScheme(
            primary = themeColor.color,
            secondary = themeColor.color.copy(alpha = 0.8f),
            tertiary = themeColor.color.copy(alpha = 0.6f)
        )
    } else {
        lightColorScheme(
            primary = themeColor.color,
            secondary = themeColor.color.copy(alpha = 0.8f),
            tertiary = themeColor.color.copy(alpha = 0.6f)
        )
    }

    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        content = content
    )
}



