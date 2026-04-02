import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.alexcemen.cryptoportfolio.data.network.signMexcQuery
import com.alexcemen.cryptoportfolio.di.commonModule
import com.alexcemen.cryptoportfolio.di.webModule
import com.alexcemen.cryptoportfolio.ui.App
import kotlinx.browser.document
import org.koin.core.context.startKoin

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    // Debug: verify HMAC-SHA256 implementation
    val testSig = signMexcQuery("timestamp=1700000000000", "392b5101f2134daf8eae24f1d72b888d")
    println("HMAC test: $testSig")
    println("Expected:  e6ca181d4c60693f62364bcc91f0d38c86d69682d4fcef1c92626e9de0746dd9")

    startKoin {
        modules(commonModule, webModule)
    }

    val root = document.getElementById("root") ?: return
    ComposeViewport(root) {
        App()
    }
}
