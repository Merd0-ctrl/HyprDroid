package dev.hyprdroid.launcher

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import dev.hyprdroid.launcher.ui.HyprDroidTheme
import dev.hyprdroid.launcher.ui.LauncherApp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

class MainActivity : ComponentActivity() {
    private lateinit var widgetHost: AppWidgetHost
    private lateinit var widgetManager: AppWidgetManager
    private lateinit var launcherPrefs: LauncherPrefs
    private var pendingWidgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID
    private var pendingWorkspace: Int = 0
    private var widgetChanged: (() -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
        launcherPrefs = LauncherPrefs(this)
        widgetManager = AppWidgetManager.getInstance(this)
        widgetHost = AppWidgetHost(this, HOST_ID)

        setContent {
            var widgetRevision by remember { mutableIntStateOf(0) }
            var accentIndex by remember { mutableIntStateOf(launcherPrefs.accentIndex) }
            widgetChanged = { widgetRevision++ }
            HyprDroidTheme(accentIndex) {
                LauncherApp(
                    activity = this,
                    prefs = launcherPrefs,
                    appWidgetHost = widgetHost,
                    appWidgetManager = widgetManager,
                    widgetRevision = widgetRevision,
                    onPickWidget = ::pickWidget,
                    onAccentChanged = { accentIndex = it }
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        widgetHost.startListening()
    }

    override fun onStop() {
        widgetHost.stopListening()
        super.onStop()
    }

    private fun pickWidget(workspace: Int) {
        pendingWorkspace = workspace
        pendingWidgetId = widgetHost.allocateAppWidgetId()
        val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_PICK).apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, pendingWidgetId)
        }
        @Suppress("DEPRECATION")
        startActivityForResult(intent, REQUEST_PICK_WIDGET)
    }

    @Deprecated("Handled for the system app-widget picker and configuration flow")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_PICK_WIDGET) {
            if (resultCode != RESULT_OK) {
                releasePendingWidget()
                return
            }
            val id = data?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, pendingWidgetId)
                ?: pendingWidgetId
            pendingWidgetId = id
            val info: AppWidgetProviderInfo? = widgetManager.getAppWidgetInfo(id)
            val configure = info?.configure
            if (configure != null) {
                val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE).apply {
                    component = configure
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
                }
                @Suppress("DEPRECATION")
                startActivityForResult(intent, REQUEST_CONFIGURE_WIDGET)
            } else {
                savePendingWidget()
            }
        } else if (requestCode == REQUEST_CONFIGURE_WIDGET) {
            if (resultCode == RESULT_OK) savePendingWidget() else releasePendingWidget()
        }
    }

    private fun savePendingWidget() {
        if (pendingWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            launcherPrefs.addWidget(pendingWorkspace, pendingWidgetId)
            widgetChanged?.invoke()
        }
        pendingWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    }

    private fun releasePendingWidget() {
        if (pendingWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            widgetHost.deleteAppWidgetId(pendingWidgetId)
        }
        pendingWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    }

    companion object {
        private const val HOST_ID = 0x485944
        private const val REQUEST_PICK_WIDGET = 481
        private const val REQUEST_CONFIGURE_WIDGET = 482
    }
}
