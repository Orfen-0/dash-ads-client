import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.core.content.ContextCompat

fun isConnected(context: Context): Boolean {
    val connectivityManager = ContextCompat.getSystemService(context, ConnectivityManager::class.java)
    val network = connectivityManager?.activeNetwork
    val capabilities = connectivityManager?.getNetworkCapabilities(network)
    return capabilities != null && (
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
            )
}