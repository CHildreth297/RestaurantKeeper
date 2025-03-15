import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.zybooks.restaurantkeeper.MediaItem

class CollectionViewModel : ViewModel() {
    var collectionName = mutableStateOf("")
        private set

    var description = mutableStateOf("")
        private set

    val entries = mutableStateListOf<MediaItem.Entry>()

}
