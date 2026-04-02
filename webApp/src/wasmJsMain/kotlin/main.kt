import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.alexcemen.cryptoportfolio.di.commonModule
import com.alexcemen.cryptoportfolio.di.webModule
import com.alexcemen.cryptoportfolio.ui.App
import kotlinx.browser.document
import org.koin.core.context.startKoin

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    startKoin {
        modules(commonModule, webModule)
    }

    val root = document.getElementById("root") ?: return
    ComposeViewport(root) {
        App()
    }
}
