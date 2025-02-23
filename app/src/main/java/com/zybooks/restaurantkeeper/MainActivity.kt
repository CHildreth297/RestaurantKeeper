import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.zybooks.restaurantkeeper.EntryScreen
import com.zybooks.restaurantkeeper.ui.theme.RestaurantKeeperTheme

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RestaurantKeeperTheme {
                Surface (
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    EntryScreen(
                        onSave = { entryData ->
                            // Handle the saved data here
                            // For now, we'll just print it to the console
                            println("Saved entry: $entryData")}
                    )
                }
            }
        }
    }
}

