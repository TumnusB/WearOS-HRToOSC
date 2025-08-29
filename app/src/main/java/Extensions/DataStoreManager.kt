package Extensions


import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "app_prefs")

object DataStoreManager {
    private val IP_KEY = stringPreferencesKey("ip")
    private val PORT_KEY = stringPreferencesKey("port")

    suspend fun saveInfo(context: Context, ip: String, port:String) {
        context.dataStore.edit { prefs ->
            prefs[IP_KEY] = ip
            prefs[PORT_KEY] = port
        }
    }

    fun getInfo(context: Context): Flow<String> {
        return context.dataStore.data.map { prefs ->
            prefs[IP_KEY] ?: ""
            prefs[PORT_KEY] ?: ""
        }
    }

    suspend fun saveIP(context: Context, ip: String) {
        context.dataStore.edit { prefs ->
            prefs[IP_KEY] = ip
        }
    }

    fun getIP(context: Context): Flow<String> {
        return context.dataStore.data.map { prefs ->
            prefs[IP_KEY] ?: ""
        }
    }

    suspend fun savePort(context: Context, port: String) {
        context.dataStore.edit { prefs ->
            prefs[PORT_KEY] = port
        }
    }

        fun getPort(context: Context): Flow<String> {
            return context.dataStore.data.map { prefs ->
                prefs[PORT_KEY] ?: ""
            }
        }
}