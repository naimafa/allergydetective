package com.nfar.allergydetective

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.mutableStateOf
import com.nfar.allergydetective.platform.EXTRA_OPEN_DAY_CHECK

class MainActivity : ComponentActivity() {

    /** True while a reminder tap waits to open the day check. */
    private val openDayCheck = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) handleIntent(intent)

        setContent {
            App(
                openDayCheck = openDayCheck.value,
                onDayCheckOpened = { openDayCheck.value = false },
                // Light icons on a dark app, dark icons on a light one, whatever the system setting is.
                onDarkThemeChange = { dark ->
                    val style = if (dark) {
                        SystemBarStyle.dark(Color.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                    }
                    enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
                },
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.getBooleanExtra(EXTRA_OPEN_DAY_CHECK, false) == true) {
            openDayCheck.value = true
            intent.removeExtra(EXTRA_OPEN_DAY_CHECK)
        }
    }
}
