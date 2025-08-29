/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter to find the
 * most up to date changes to the libraries and their usages.
 */

package com.tumnus.heartrateosc.presentation

import Extensions.DataStoreManager
import Extensions.HeartRateListener
import Extensions.OscSender

import android.R

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons

import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

import androidx.compose.ui.text.style.TextAlign
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text

import com.tumnus.heartrateosc.presentation.theme.HeartRateOSCTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

import androidx.wear.compose.material.Button
import androidx.wear.compose.material.Icon
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers


class MainActivity : ComponentActivity() {
     lateinit var heartRateListener: HeartRateListener
     lateinit var oscSender: OscSender

     var currentIp: String = DEFAULT_IP
     var currentPort: Int = DEFAULT_PORT.toInt()
     var currentHR = ""

    companion object {
        const val DEFAULT_IP = "192.168.0.239"
        const val DEFAULT_PORT = "9555"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        setTheme(R.style.Theme_DeviceDefault)

        // ✅ initialize oscSender here before using it
        oscSender = OscSender()

        heartRateListener = HeartRateListener(this) { bpm ->
            currentHR = "$bpm"

            // Send OSC in background
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    oscSender.sendOscMessage(currentIp, currentPort, "/hrtest", currentHR.toFloat())
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        setContent {

            WearApp(ip = DEFAULT_PORT, port = DEFAULT_IP, heartRateListener)
        }
    }
}

@Composable
fun WearApp(ip: String, port: String, heartRateListener: HeartRateListener) {

    var ip by remember { mutableStateOf(MainActivity.DEFAULT_IP) }
    var port by remember { mutableStateOf(MainActivity.DEFAULT_PORT) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(context) {
        DataStoreManager.getIP(context).collectLatest { value ->
            ip = value
            // Update MainActivity
            (context as? MainActivity)?.currentIp = value
        }
    }

    // Collect Port
    LaunchedEffect(context) {
        DataStoreManager.getPort(context).collectLatest { value ->
            port = value
            (context as? MainActivity)?.currentPort = value.toIntOrNull() ?: MainActivity.DEFAULT_PORT.toInt()
        }
    }

    val listState = rememberScalingLazyListState()

    HeartRateOSCTheme {
        ScalingLazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            item {
                TextDisplay("Input IP")
            }
            item {
                IPInput()
            }
            item {
                Spacer(modifier = Modifier.height(8.dp))
            }
            item {
                TextDisplay("Input Port")
            }
            item {
                PortInput()
            }
            item {
                Spacer(modifier = Modifier.height(8.dp))
            }
            item {
                ButtonWithIconSample(listener = heartRateListener)
            }
        }
    }
}

@Composable
fun TextDisplay(toDisplay: String) {
    Text(
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
        color = MaterialTheme.colors.primary,
        text = toDisplay
    )
}

@Composable
fun IPInput() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var ip by remember { mutableStateOf("") }

    // Collect IP from DataStore
    LaunchedEffect(context) {
        DataStoreManager.getIP(context).collectLatest { value ->
            ip = value
        }
    }

    OutlinedTextField(
        value = ip,
        onValueChange = { newValue ->
            ip = newValue
            // Save to DataStore
            scope.launch {
                DataStoreManager.saveIP(context, newValue)
            }
        },
        label = { Text("Enter IP") },
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    )
}

@Composable
fun PortInput() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var port by remember { mutableStateOf("") }

    // Collect Port from DataStore
    LaunchedEffect(context) {
        DataStoreManager.getPort(context).collectLatest { value ->
            port = value
        }
    }

    OutlinedTextField(
        value = port,
        onValueChange = { newValue ->
            port = newValue
            // Save to DataStore
            scope.launch {
                DataStoreManager.savePort(context, newValue)
            }
        },
        label = { Text("Enter Port") },
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    )
}

@Composable
fun ButtonWithIconSample(listener: HeartRateListener) {
    Button(onClick = {
        listener.start()
    }) {
        Icon(
            Icons.Filled.PlayArrow,
            contentDescription = "Start Heart Rate",
            modifier = Modifier.size(ButtonDefaults.IconSize)
        )
        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
    }
}
