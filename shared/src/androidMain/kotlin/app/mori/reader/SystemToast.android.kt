package app.mori.reader

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberSystemToast(): (String) -> Unit {
    val context = LocalContext.current.applicationContext
    return remember(context) {
        { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }
}
