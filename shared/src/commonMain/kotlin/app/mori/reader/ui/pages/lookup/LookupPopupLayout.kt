package app.mori.reader.ui.pages.lookup

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

internal data class LookupPopupLayout(
    val width: Dp,
    val height: Dp,
    val left: Dp,
    val top: Dp,
)

internal fun calculateLookupPopupLayout(
    selectionLeft: Dp,
    selectionTop: Dp,
    selectionRight: Dp,
    selectionBottom: Dp,
    screenWidth: Dp,
    screenHeight: Dp,
    maxWidth: Dp,
    maxHeight: Dp,
    isVertical: Boolean,
    isFullWidth: Boolean,
    topInset: Dp,
    bottomInset: Dp,
    leftInset: Dp = 0.dp,
    rightInset: Dp = 0.dp,
): LookupPopupLayout {
    val popupPadding = 4.dp
    val screenBorderPadding = 6.dp
    val spaceLeft = selectionLeft - leftInset - popupPadding
    val spaceRight = screenWidth - rightInset - selectionRight - popupPadding
    val spaceAbove = selectionTop - topInset - popupPadding
    val spaceBelow = screenHeight - bottomInset - selectionBottom - popupPadding
    val availableWidth = screenWidth - leftInset - rightInset
    val width =
        when {
            isFullWidth -> availableWidth - screenBorderPadding * 2f
            isVertical -> minOf(maxOf(spaceLeft, spaceRight) - screenBorderPadding, maxWidth)
            else -> minOf(availableWidth - screenBorderPadding * 2f, maxWidth)
        }.coerceAtLeast(1.dp)
    val height =
        when {
            isVertical || isFullWidth -> maxHeight
            else -> minOf(maxOf(spaceAbove, spaceBelow) - screenBorderPadding, maxHeight)
        }.coerceAtLeast(1.dp)

    val centerX: Dp
    val centerY: Dp
    if (isFullWidth) {
        centerX = leftInset + width / 2f + screenBorderPadding
        centerY = screenHeight - bottomInset - height / 2f - screenBorderPadding
    } else if (isVertical) {
        val unclampedX =
            if (spaceRight >= spaceLeft) {
                selectionRight + popupPadding + width / 2f
            } else {
                selectionLeft - popupPadding - width / 2f
            }
        centerX = unclampedX.coerceIn(width / 2f, screenWidth - width / 2f)
            .coerceIn(
                width / 2f + leftInset + screenBorderPadding,
                screenWidth - rightInset - width / 2f - screenBorderPadding,
            )
        val unclampedY = selectionTop + height / 2f
        centerY =
            unclampedY.coerceIn(
                height / 2f + screenBorderPadding + topInset,
                screenHeight - bottomInset - height / 2f - screenBorderPadding,
            )
    } else {
        val unclampedX = selectionLeft + width / 2f
        centerX =
            unclampedX.coerceIn(
                width / 2f + leftInset + screenBorderPadding,
                screenWidth - rightInset - width / 2f - screenBorderPadding,
            )
        val showBelow = spaceBelow >= height
        val unclampedY =
            if (showBelow) {
                selectionBottom + popupPadding + height / 2f
            } else {
                selectionTop - popupPadding - height / 2f
            }
        centerY =
            unclampedY.coerceIn(
                height / 2f + topInset + screenBorderPadding,
                screenHeight - bottomInset - height / 2f - screenBorderPadding,
            )
    }

    return LookupPopupLayout(
        width = width,
        height = height,
        left = centerX - width / 2f,
        top = centerY - height / 2f,
    )
}
