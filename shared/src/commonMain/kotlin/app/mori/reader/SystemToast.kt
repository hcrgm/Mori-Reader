package app.mori.reader

import androidx.compose.runtime.Composable

@Composable
expect fun rememberSystemToast(): (String) -> Unit
