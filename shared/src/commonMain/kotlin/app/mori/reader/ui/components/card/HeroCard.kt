package app.mori.reader.ui.components.card

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
internal fun HeroCard(
    title: String,
    subtitle: String,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(MiuixTheme.colorScheme.primaryVariant)
            .padding(22.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(0.72f),
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                color = MiuixTheme.colorScheme.onPrimaryVariant,
            )
            Text(
                text = subtitle,
                color = MiuixTheme.colorScheme.onPrimaryVariant.copy(alpha = 0.72f),
                modifier = Modifier.padding(top = 10.dp),
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(84.dp)
                .clip(CircleShape)
                .background(MiuixTheme.colorScheme.onPrimaryVariant.copy(alpha = 0.12f)),
        )
    }
}
