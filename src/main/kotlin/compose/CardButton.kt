package compose

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.isAltPressed
import androidx.compose.ui.input.pointer.isMetaPressed
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import res.ColorRes

@Composable
fun CardButton(
    modifier: Modifier = Modifier.heightIn(min = 48.dp),
    elevation: Dp = 0.dp,
    color: Color = ColorRes.transparent,
    contentColor: Color = ColorRes.transparent,
    border: BorderStroke? = null,
    shape: Shape = RoundedCornerShape(8.dp),
    onClick: () -> Unit,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    indication: Indication? = LocalIndication.current,
    enabled: Boolean = true,
    onClickLabel: String? = null,
    role: Role? = null,
    content: @Composable () -> Unit,
) {
    CardButton(
        modifier = modifier,
        elevation = elevation,
        color = color,
        contentColor = contentColor,
        border = border,
        shape = shape,
        clickAndSemanticsModifier = Modifier.clickable(
            interactionSource = interactionSource,
            indication = indication,
            enabled = enabled,
            onClickLabel = onClickLabel,
            role = role,
            onClick = onClick
        ),
        content = {
            BoxWithConstraints(
                modifier = modifier,
                contentAlignment = Alignment.Center,
                content = { content() }
            )
        }
    )
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CardButton(
    modifier: Modifier = Modifier.heightIn(min = 48.dp),
    elevation: Dp = 0.dp,
    color: Color = ColorRes.transparent,
    contentColor: Color = ColorRes.transparent,
    border: BorderStroke? = null,
    shape: Shape = RoundedCornerShape(8.dp),
    onMouseClick: MouseClickScope.() -> Unit,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    indication: Indication? = LocalIndication.current,
    enabled: Boolean = true,
    onClickLabel: String? = null,
    role: Role? = null,
    content: @Composable () -> Unit,
) {
    CardButton(
        modifier = modifier,
        elevation = elevation,
        color = color,
        contentColor = contentColor,
        border = border,
        shape = shape,
        clickAndSemanticsModifier = Modifier.clickable(
            interactionSource = interactionSource,
            indication = indication,
            enabled = enabled,
            onClickLabel = onClickLabel,
            role = role,
            onClick = { /* 不做响应, 由 mouseClickable 响应 */ }
        ).mouseClickable { onMouseClick.invoke(this) },
        content = {
            BoxWithConstraints(
                modifier = modifier,
                contentAlignment = Alignment.Center,
                content = { content() }
            )
        }
    )
}

@Composable
private fun CardButton(
    modifier: Modifier,
    shape: Shape,
    color: Color,
    contentColor: Color,
    border: BorderStroke?,
    elevation: Dp,
    clickAndSemanticsModifier: Modifier,
    content: @Composable () -> Unit
) {
    val elevationOverlay = LocalElevationOverlay.current
    val absoluteElevation = LocalAbsoluteElevation.current + elevation
    val backgroundColor = if (color == MaterialTheme.colors.surface && elevationOverlay != null) {
        elevationOverlay.apply(color, absoluteElevation)
    } else {
        color
    }
    CompositionLocalProvider(
        LocalContentColor provides contentColor,
        LocalAbsoluteElevation provides absoluteElevation
    ) {
        Box(
            propagateMinConstraints = true,
            content = { content() },
            modifier = modifier
                .shadow(elevation, shape, clip = false)
                .then(if (border != null) Modifier.border(border, shape) else Modifier)
                .background(color = backgroundColor, shape = shape)
                .clip(shape)
                .then(clickAndSemanticsModifier),
        )
    }
}