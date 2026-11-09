package dev.hyprdroid.launcher.ui

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.BatteryManager
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.drawable.toBitmap
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.ui.res.stringResource
import dev.hyprdroid.launcher.LauncherPrefs
import dev.hyprdroid.launcher.TermuxBridge

private enum class LauncherPage { HOME, DRAWER, SETTINGS }
private data class PendingTermuxCommand(val command: String?)

data class LauncherAppInfo(val packageName: String, val label: String)

@Composable
fun LauncherApp(
    activity: ComponentActivity,
    prefs: LauncherPrefs,
    appWidgetHost: AppWidgetHost,
    appWidgetManager: AppWidgetManager,
    widgetRevision: Int,
    onPickWidget: (Int) -> Unit,
    onAccentChanged: (Int) -> Unit
) {
    var accentIndex by remember { mutableIntStateOf(prefs.accentIndex) }
    var transparency by remember { mutableFloatStateOf(prefs.transparency) }
    var dockCount by remember { mutableIntStateOf(prefs.dockCount) }
    var dockLabels by remember { mutableStateOf(prefs.showDockLabels) }
    var animations by remember { mutableStateOf(prefs.animationsEnabled) }
    var wallpaperUri by remember { mutableStateOf(prefs.wallpaperUri) }
    var workspace by remember { mutableIntStateOf(prefs.lastWorkspace) }
    var page by remember { mutableStateOf(LauncherPage.HOME) }
    var editMode by remember { mutableStateOf(false) }
    var search by remember { mutableStateOf("") }
    var commandText by remember { mutableStateOf("") }
    var commandStatus by remember { mutableStateOf("") }
    var showCommandHelp by remember { mutableStateOf(false) }
    var pendingTermuxCommand by remember { mutableStateOf<PendingTermuxCommand?>(null) }
    var pendingAdd by remember { mutableStateOf<LauncherAppInfo?>(null) }
    var pendingRemove by remember { mutableStateOf<LauncherAppInfo?>(null) }
    var pendingRemoveWidget by remember { mutableIntStateOf(AppWidgetManager.INVALID_APPWIDGET_ID) }
    var widgetRefresh by remember { mutableIntStateOf(widgetRevision) }
    val apps = remember { findLauncherApps(activity) }
    val accent = AccentColors[accentIndex.coerceIn(0, AccentColors.lastIndex)]
    val termuxPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        val pending = pendingTermuxCommand
        pendingTermuxCommand = null
        if (granted && pending != null) {
            try {
                activity.startService(TermuxBridge.createIntent(pending.command))
                commandStatus = "Komut Termux Debian ortamına gönderildi."
            } catch (error: Exception) {
                commandStatus = "Termux komut köprüsü açılamadı: ${error.message ?: "izinleri kontrol et"}"
            }
        } else if (pending != null) {
            commandStatus = "Termux komut izni verilmedi. Ayarlar → Uygulamalar → HyprDroid → İzinler bölümünden ver."
        }
    }
    val dispatchLinuxCommand: (String?) -> Unit = { linuxCommand ->
        if (!TermuxBridge.isInstalled(activity)) {
            commandStatus = "Linux terminali için Termux kur; ardından Termux’ta proot-distro ile Debian’ı yükle."
        } else if (activity.checkSelfPermission(TermuxBridge.RUN_COMMAND_PERMISSION) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            pendingTermuxCommand = PendingTermuxCommand(linuxCommand)
            termuxPermissionLauncher.launch(TermuxBridge.RUN_COMMAND_PERMISSION)
        } else {
            try {
                activity.startService(TermuxBridge.createIntent(linuxCommand))
                commandStatus = "Komut Termux Debian ortamına gönderildi."
            } catch (error: Exception) {
                commandStatus = "Termux komut köprüsü açılamadı: ${error.message ?: "ayarları kontrol et"}"
            }
        }
    }
    val executeCommand: () -> Unit = {
        val input = commandText.trim()
        when {
            input.isBlank() -> commandStatus = "Önce komut yaz."
            input.equals("help", ignoreCase = true) || input == "?" -> {
                commandStatus = "Komut yardımı açıldı."
                showCommandHelp = true
            }
            input.equals("term", ignoreCase = true) || input.equals("terminal", ignoreCase = true) || input.equals("linux", ignoreCase = true) -> {
                commandText = ""
                dispatchLinuxCommand(null)
            }
            else -> {
                val result = resolveAppCommand(apps, input)
                when {
                    result.app != null -> {
                        launchApp(activity, result.app.packageName)
                        commandStatus = "Açılıyor: ${result.app.label}"
                        commandText = ""
                    }
                    result.message.startsWith("Birden fazla") -> commandStatus = result.message
                    input.startsWith("pkg:", ignoreCase = true) || input.startsWith("app:", ignoreCase = true) -> commandStatus = result.message
                    else -> {
                        val linuxCommand = input.replace(Regex("^run\\s+", RegexOption.IGNORE_CASE), "").trim()
                        if (linuxCommand.isBlank()) commandStatus = "Kullanım: run Ayarlar · ls · uname -a · term"
                        else {
                            commandText = ""
                            dispatchLinuxCommand(linuxCommand)
                        }
                    }
                }
            }
        }
    }

    val wallpaperPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) {
            try {
                activity.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (_: SecurityException) { }
            wallpaperUri = uri.toString()
            prefs.wallpaperUri = wallpaperUri
        }
    }

    LaunchedEffect(widgetRevision) { widgetRefresh = widgetRevision }

    Box(modifier = Modifier.fillMaxSize().background(Background)) {
        WallpaperLayer(activity, wallpaperUri)
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    colors = listOf(Color(0x35080A0F), Color(0x18080A0F), Color(0x68080A0F)),
                    startY = 0f,
                    endY = Float.POSITIVE_INFINITY
                )
            )
        )
        AnimatedContent(
            targetState = page,
            transitionSpec = {
                if (animations) {
                    (fadeIn() + slideInHorizontally { it / 12 }) togetherWith
                        (fadeOut() + slideOutHorizontally { -it / 12 })
                } else EnterTransition.None togetherWith ExitTransition.None
            },
            label = "launcher-page-transition"
        ) { visiblePage ->
        when (visiblePage) {
            LauncherPage.HOME -> HomePage(
                activity = activity,
                prefs = prefs,
                apps = apps,
                appWidgetHost = appWidgetHost,
                appWidgetManager = appWidgetManager,
                workspace = workspace,
                widgetRefresh = widgetRefresh,
                editMode = editMode,
                transparency = transparency,
                dockCount = dockCount,
                dockLabels = dockLabels,
                commandText = commandText,
                commandStatus = commandStatus,
                onCommandText = { commandText = it },
                onExecuteCommand = executeCommand,
                animations = animations,
                onWorkspace = { workspace = it; prefs.lastWorkspace = it },
                onOpenDrawer = { search = ""; page = LauncherPage.DRAWER },
                onOpenSettings = { page = LauncherPage.SETTINGS },
                onPickWidget = onPickWidget,
                onEnterEdit = { editMode = true },
                onExitEdit = { editMode = false },
                onRemoveShortcut = { pendingRemove = it },
                onRemoveWidget = { pendingRemoveWidget = it }
            )
            LauncherPage.DRAWER -> AppDrawerPage(
                activity = activity,
                apps = apps,
                search = search,
                onSearch = { search = it },
                onBack = { page = LauncherPage.HOME },
                onOpenApp = { app -> launchApp(activity, app.packageName); page = LauncherPage.HOME },
                onLongPressApp = { pendingAdd = it }
            )
            LauncherPage.SETTINGS -> SettingsPage(
                accentIndex = accentIndex,
                transparency = transparency,
                dockCount = dockCount,
                dockLabels = dockLabels,
                animations = animations,
                onAccent = { accentIndex = it; prefs.accentIndex = it; onAccentChanged(it) },
                onTransparency = { transparency = it; prefs.transparency = it },
                onDockCount = { dockCount = it; prefs.dockCount = it },
                onDockLabels = { dockLabels = it; prefs.showDockLabels = it },
                onAnimations = { animations = it; prefs.animationsEnabled = it },
                onPickWallpaper = { wallpaperPicker.launch(arrayOf("image/*")) },
                onRestore = {
                    for (i in 0 until LauncherPrefs.WORKSPACE_COUNT) {
                        prefs.widgetIds(i).forEach { id ->
                            try { appWidgetHost.deleteAppWidgetId(id) } catch (_: RuntimeException) { }
                        }
                    }
                    prefs.restoreDefaults()
                    accentIndex = prefs.accentIndex
                    transparency = prefs.transparency
                    dockCount = prefs.dockCount
                    dockLabels = prefs.showDockLabels
                    animations = prefs.animationsEnabled
                    wallpaperUri = prefs.wallpaperUri
                    workspace = 0
                    accentIndex = prefs.accentIndex
                    onAccentChanged(accentIndex)
                    widgetRefresh++
                },
                onBack = { page = LauncherPage.HOME }
            )
        }
        }
    }

    pendingAdd?.let { app ->
        AlertDialog(
            onDismissRequest = { pendingAdd = null },
            containerColor = PanelRaised.copy(alpha = 0.9f),
            title = { Text("Çalışma alanına ekle") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("${app.label} kısayolunu hangi alana ekleyelim?", color = Muted)
                    (0 until LauncherPrefs.WORKSPACE_COUNT).forEach { index ->
                        TextButton(
                            onClick = {
                                prefs.addShortcut(index, app.packageName)
                                widgetRefresh++
                                workspace = index
                                prefs.lastWorkspace = index
                                pendingAdd = null
                                page = LauncherPage.HOME
                                editMode = false
                            }, modifier = Modifier.fillMaxWidth()
                        ) { Text("${index + 1}. çalışma alanı", color = MaterialTheme.colorScheme.primary) }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { pendingAdd = null }) { Text("İptal") } }
        )
    }

    pendingRemove?.let { app ->
        AlertDialog(
            onDismissRequest = { pendingRemove = null },
            containerColor = PanelRaised.copy(alpha = 0.9f),
            title = { Text("Kısayolu kaldır?") },
            text = { Text("${app.label}, bu çalışma alanının ana ekranından kaldırılır. Uygulama silinmez.", color = Muted) },
            confirmButton = {
                TextButton(onClick = {
                    prefs.removeShortcut(workspace, app.packageName)
                    widgetRefresh++
                    pendingRemove = null
                }) { Text("Kaldır", color = MaterialTheme.colorScheme.primary) }
            },
            dismissButton = { TextButton(onClick = { pendingRemove = null }) { Text("Vazgeç") } }
        )
    }

    if (pendingRemoveWidget != AppWidgetManager.INVALID_APPWIDGET_ID) {
        AlertDialog(
            onDismissRequest = { pendingRemoveWidget = AppWidgetManager.INVALID_APPWIDGET_ID },
            containerColor = PanelRaised.copy(alpha = 0.9f),
            title = { Text("Widget’ı kaldır?") },
            text = { Text("Widget bu çalışma alanından kaldırılır.", color = Muted) },
            confirmButton = {
                TextButton(onClick = {
                    prefs.removeWidget(workspace, pendingRemoveWidget)
                    appWidgetHost.deleteAppWidgetId(pendingRemoveWidget)
                    pendingRemoveWidget = AppWidgetManager.INVALID_APPWIDGET_ID
                    widgetRefresh++
                }) { Text("Kaldır", color = MaterialTheme.colorScheme.primary) }
            },
            dismissButton = { TextButton(onClick = { pendingRemoveWidget = AppWidgetManager.INVALID_APPWIDGET_ID }) { Text("Vazgeç") } }
        )
    }

    if (showCommandHelp) {
        AlertDialog(
            onDismissRequest = { showCommandHelp = false },
            containerColor = PanelRaised.copy(alpha = 0.9f),
            title = { Text("Uygulama ve Linux komutları") },
            text = {
                Column(Modifier.heightIn(max = 400.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    Text("Android uygulaması aç: run Ayarlar  ·  run instagra", color = MaterialTheme.colorScheme.primary)
                    Text("Linux komutu: ls  ·  uname -a  ·  apt update", color = MaterialTheme.colorScheme.primary)
                    Text("term yazınca etkileşimli Debian terminali açılır.", color = Color.White)
                    Text("Linux için Termux’u kur. Termux’ta pkg install proot-distro ve proot-distro install debian çalıştır. ~/.termux/termux.properties dosyasına allow-external-apps=true ekle; ardından HyprDroid için ‘Termux ortamında komut çalıştırma’ iznini ver.", color = Muted)
                    Text("Komutlar Android sistemine değil, Termux içindeki PRoot Debian’a gider. Gerçek root veya Android pencere yöneticisi kontrolü sağlamaz.", color = Muted)
                }
            },
            confirmButton = { TextButton(onClick = { showCommandHelp = false }) { Text("Tamam") } }
        )
    }
}

@Composable
private fun WallpaperLayer(activity: ComponentActivity, wallpaperUri: String?) {
    val image by produceState<ImageBitmap?>(initialValue = null, wallpaperUri) {
        value = if (wallpaperUri == null) null else withContext(Dispatchers.IO) {
            try {
                activity.contentResolver.openInputStream(Uri.parse(wallpaperUri))?.use { stream ->
                    BitmapFactory.decodeStream(stream)?.asImageBitmap()
                }
            } catch (_: Exception) { null }
        }
    }
    if (image != null) {
        Image(
            bitmap = image!!,
            contentDescription = "Seçilen duvar kâğıdı",
            modifier = Modifier.fillMaxSize().blur(22.dp).graphicsLayer { alpha = 0.88f },
            contentScale = androidx.compose.ui.layout.ContentScale.Crop
        )
    } else {
        Box(Modifier.fillMaxSize().background(Brush.linearGradient(listOf(Color(0xFFED78B7), Color(0xFF6B5CC6), Color(0xFF146C88), Color(0xFF0C1324)))))
    }
}

@Composable
private fun HomePage(
    activity: ComponentActivity,
    prefs: LauncherPrefs,
    apps: List<LauncherAppInfo>,
    appWidgetHost: AppWidgetHost,
    appWidgetManager: AppWidgetManager,
    workspace: Int,
    widgetRefresh: Int,
    editMode: Boolean,
    transparency: Float,
    dockCount: Int,
    dockLabels: Boolean,
    commandText: String,
    commandStatus: String,
    onCommandText: (String) -> Unit,
    onExecuteCommand: () -> Unit,
    animations: Boolean,
    onWorkspace: (Int) -> Unit,
    onOpenDrawer: () -> Unit,
    onOpenSettings: () -> Unit,
    onPickWidget: (Int) -> Unit,
    onEnterEdit: () -> Unit,
    onExitEdit: () -> Unit,
    onRemoveShortcut: (LauncherAppInfo) -> Unit,
    onRemoveWidget: (Int) -> Unit
) {
    var dragX by remember { mutableFloatStateOf(0f) }
    var dragY by remember { mutableFloatStateOf(0f) }
    val now by produceState(System.currentTimeMillis()) {
        while (true) { value = System.currentTimeMillis(); delay(1000) }
    }
    val batteryPercent by produceState(readBatteryPercent(activity)) {
        while (true) { value = readBatteryPercent(activity); delay(60_000) }
    }
    val allDockPackages = remember(widgetRefresh, prefs, apps) {
        (0 until LauncherPrefs.WORKSPACE_COUNT).flatMap { prefs.shortcuts(it) }.distinct().sorted()
    }
    val dockApps = remember(allDockPackages, apps, dockCount) {
        allDockPackages.mapNotNull { pkg -> apps.find { it.packageName == pkg } }
            .distinctBy { it.packageName }.take(dockCount)
    }
    val panelColor = MaterialTheme.colorScheme.surface.copy(alpha = transparency)

    Column(
        Modifier.fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))
            .padding(horizontal = 22.dp)
            .pointerInput(workspace, editMode) {
                dragX = 0f; dragY = 0f
                detectDragGestures(
                    onDrag = { change, amount ->
                        change.consume()
                        dragX += amount.x
                        dragY += amount.y
                    },
                    onDragEnd = {
                        if (kotlin.math.abs(dragX) > 72 && kotlin.math.abs(dragX) > kotlin.math.abs(dragY)) {
                            val next = (workspace + if (dragX < 0) 1 else -1).coerceIn(0, LauncherPrefs.WORKSPACE_COUNT - 1)
                            onWorkspace(next)
                        } else if (dragY < -72 && kotlin.math.abs(dragY) > kotlin.math.abs(dragX)) {
                            onOpenDrawer()
                        }
                        dragX = 0f; dragY = 0f
                    }
                )
            }
    ) {
        Row(
            Modifier.fillMaxWidth().padding(top = 6.dp, bottom = 7.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Brush.horizontalGradient(listOf(Color(0xA0182034), Color(0x70131B2B), Color(0x90271B42))))
                .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.16f)), RoundedCornerShape(20.dp))
                .padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                Box(Modifier.size(25.dp).clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.primary), contentAlignment = Alignment.Center) {
                    Text("H", color = Background, fontWeight = FontWeight.Black, fontSize = 15.sp)
                }
                Column {
                    Text("HYPR", fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.7.sp)
                    Text("DESKTOP", fontSize = 8.sp, color = Muted, letterSpacing = 1.2.sp)
                }
            }
            Row(Modifier.padding(start = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                repeat(LauncherPrefs.WORKSPACE_COUNT) { index ->
                    val selected = index == workspace
                    Box(
                        Modifier.padding(horizontal = 2.dp).size(width = 25.dp, height = 27.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.92f) else Color.White.copy(alpha = 0.05f))
                            .combinedClickable(onClick = { onWorkspace(index) }, onLongClick = onEnterEdit),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("0${index + 1}", color = if (selected) Background else Color.White.copy(alpha = 0.68f), fontFamily = FontFamily.Monospace, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Text("BAT $batteryPercent%", color = MaterialTheme.colorScheme.primary, fontFamily = FontFamily.Monospace, fontSize = 8.sp, modifier = Modifier.padding(start = 4.dp))
            Spacer(Modifier.weight(1f))
            IconButton(onClick = onOpenDrawer, modifier = Modifier.size(38.dp)) { Icon(Icons.Default.Apps, "Uygulamalar", tint = MaterialTheme.colorScheme.primary) }
            IconButton(onClick = onOpenSettings, modifier = Modifier.size(38.dp)) { Icon(Icons.Default.Settings, "Ayarlar", tint = Color.White.copy(alpha = 0.85f)) }
        }

        CommandBar(
            value = commandText,
            status = commandStatus,
            onValueChange = onCommandText,
            onRun = onExecuteCommand,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(11.dp))
        Surface(
            color = panelColor,
            shape = RoundedCornerShape(25.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.34f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(horizontal = 20.dp, vertical = 17.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(Color(0xFFFF647C), Color(0xFFFFC46B), Color(0xFF71E5B6)).forEach { light ->
                        Box(Modifier.size(7.dp).clip(CircleShape).background(light))
                    }
                    Text("  WORKSPACE-${(workspace + 1).toString().padStart(2, '0')}  ·  HYPRDROID", color = Color.White.copy(alpha = 0.75f), fontFamily = FontFamily.Monospace, fontSize = 9.sp, letterSpacing = 0.7.sp)
                    Spacer(Modifier.weight(1f))
                    Text("● LIVE", color = MaterialTheme.colorScheme.primary, fontFamily = FontFamily.Monospace, fontSize = 8.sp)
                }
                HorizontalDivider(color = Color.White.copy(alpha = 0.12f), modifier = Modifier.padding(top = 12.dp, bottom = 5.dp))
                Text(
                    text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(now)),
                    fontSize = 68.sp,
                    lineHeight = 72.sp,
                    letterSpacing = (-3).sp,
                    fontWeight = FontWeight.Light,
                    fontFamily = FontFamily.Monospace,
                    color = Color.White,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Text(
                    SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()).format(Date(now)).uppercase(Locale.getDefault()),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.92f),
                    fontSize = 10.sp,
                    letterSpacing = 2.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        Row(
            Modifier.fillMaxWidth().padding(top = 19.dp, bottom = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("KISAYOLLAR", color = Muted, fontSize = 10.sp, letterSpacing = 1.7.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.weight(1f))
            if (editMode) Text("Düzenleme açık", color = MaterialTheme.colorScheme.primary, fontSize = 11.sp)
        }
        AnimatedContent(
            targetState = workspace,
            transitionSpec = {
                if (animations) {
                    val direction = if (targetState > initialState) 1 else -1
                    (slideInHorizontally { direction * it / 5 } + fadeIn()) togetherWith
                        (slideOutHorizontally { -direction * it / 5 } + fadeOut())
                } else EnterTransition.None togetherWith ExitTransition.None
            },
            label = "workspace-transition"
        ) { activeWorkspace ->
            val activeShortcuts = remember(activeWorkspace, widgetRefresh) {
                prefs.shortcuts(activeWorkspace).mapNotNull { pkg -> apps.find { it.packageName == pkg } }
                    .sortedBy { it.label.lowercase(Locale.ROOT) }
            }
            val activeWidgetIds = remember(activeWorkspace, widgetRefresh) { prefs.widgetIds(activeWorkspace) }
            WorkspaceContent(
                activity = activity,
                prefs = prefs,
                apps = activeShortcuts,
                widgetIds = activeWidgetIds,
                appWidgetHost = appWidgetHost,
                appWidgetManager = appWidgetManager,
                workspace = activeWorkspace,
                editMode = editMode,
                panelColor = panelColor,
                onOpenDrawer = onOpenDrawer,
                onPickWidget = onPickWidget,
                onEnterEdit = onEnterEdit,
                onRemoveShortcut = onRemoveShortcut,
                onRemoveWidget = onRemoveWidget
            )
        }

        Spacer(
            Modifier.weight(1f).fillMaxWidth().combinedClickable(onClick = {}, onLongClick = onEnterEdit)
        )

        AnimatedVisibility(visible = editMode) {
            Surface(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
            ) {
                Row(Modifier.padding(horizontal = 6.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceEvenly) {
                    SmallAction("Uygulama", Icons.Default.Add, onOpenDrawer)
                    SmallAction("Widget", Icons.Default.Widgets, { onPickWidget(workspace) })
                    SmallAction("Ayarlar", Icons.Default.Settings, onOpenSettings)
                    IconButton(onClick = onExitEdit) { Icon(Icons.Default.Check, "Düzenlemeyi bitir", tint = MaterialTheme.colorScheme.primary) }
                }
            }
        }

        Surface(
            color = panelColor,
            shape = RoundedCornerShape(25.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
            modifier = Modifier.fillMaxWidth().padding(bottom = 7.dp)
        ) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 9.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val displayedDock = if (dockApps.isEmpty()) emptyList() else dockApps
                displayedDock.forEach { app ->
                    AppIconTile(
                        activity = activity, app = app, label = dockLabels, compact = true,
                        onClick = { launchApp(activity, app.packageName) },
                        onLongPress = { onEnterEdit() }
                    )
                }
                if (displayedDock.isEmpty()) {
                    OutlinedButton(onClick = onOpenDrawer, border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))) {
                        Icon(Icons.Default.Apps, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(17.dp))
                        Text(" Uygulamalar", color = Color.White, fontSize = 12.sp)
                    }
                }
            }
        }
        Row(
            Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.Bottom)).padding(top = 4.dp, bottom = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(LauncherPrefs.WORKSPACE_COUNT) { index ->
                val selected = index == workspace
                Box(
                    Modifier.padding(horizontal = 5.dp).size(width = if (selected) 22.dp else 6.dp, height = 6.dp)
                        .clip(CircleShape)
                        .background(if (selected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.28f))
                    .combinedClickable(onClick = { onWorkspace(index) }, onLongClick = onEnterEdit)
                )
            }
        }
        if (editMode) {
            Row(Modifier.fillMaxWidth().padding(bottom = 2.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Text("Uygulama çekmecesi için yukarı kaydır · alan değiştirmek için yana kaydır", color = Muted, fontSize = 10.sp, textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
private fun WorkspaceContent(
    activity: ComponentActivity,
    prefs: LauncherPrefs,
    apps: List<LauncherAppInfo>,
    widgetIds: List<Int>,
    appWidgetHost: AppWidgetHost,
    appWidgetManager: AppWidgetManager,
    workspace: Int,
    editMode: Boolean,
    panelColor: Color,
    onOpenDrawer: () -> Unit,
    onPickWidget: (Int) -> Unit,
    onEnterEdit: () -> Unit,
    onRemoveShortcut: (LauncherAppInfo) -> Unit,
    onRemoveWidget: (Int) -> Unit
) {
    if (apps.isEmpty() && widgetIds.isEmpty()) {
        Surface(color = panelColor, shape = RoundedCornerShape(20.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.24f))) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 17.dp, vertical = 15.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    listOf(Color(0xFFFF647C), Color(0xFFFFC46B), Color(0xFF71E5B6)).forEach { color ->
                        Box(Modifier.size(6.dp).clip(CircleShape).background(color))
                    }
                    Text("FLOATING WINDOW  ·  WS-${(workspace + 1).toString().padStart(2, '0')}", color = Muted, fontFamily = FontFamily.Monospace, fontSize = 8.sp, letterSpacing = 0.7.sp)
                }
                Text("Workspace boş", fontWeight = FontWeight.SemiBold, fontSize = 15.sp, modifier = Modifier.padding(top = 11.dp))
                Text("Uygulama veya Android widget ekleyerek düzenini oluştur.", color = Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 3.dp, bottom = 12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SmallAction("Uygulama ekle", Icons.Default.Add, onOpenDrawer, Modifier.weight(1f))
                    SmallAction("Widget ekle", Icons.Default.Widgets, { onPickWidget(workspace) }, Modifier.weight(1f))
                }
            }
        }
    } else {
        if (apps.isNotEmpty()) {
            Surface(
                color = panelColor,
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 70.dp),
                    modifier = Modifier.fillMaxWidth().heightIn(max = 205.dp).padding(horizontal = 8.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(apps, key = { it.packageName }) { app ->
                        AppIconTile(
                            activity = activity, app = app, label = true,
                            onClick = { launchApp(activity, app.packageName) },
                            onLongPress = { if (editMode) onRemoveShortcut(app) else onEnterEdit() }
                        )
                    }
                }
            }
        }
        if (widgetIds.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(9.dp), modifier = Modifier.padding(top = if (apps.isNotEmpty()) 12.dp else 0.dp)) {
                widgetIds.forEach { id ->
                    val info = appWidgetManager.getAppWidgetInfo(id)
                    if (info != null) {
                        Surface(
                            color = panelColor,
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(Modifier.padding(8.dp)) {
                                if (editMode) {
                                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                        Text(info.loadLabel(activity.packageManager), color = Muted, fontSize = 11.sp, modifier = Modifier.weight(1f))
                                        TextButton(onClick = { onRemoveWidget(id) }) {
                                            Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(15.dp))
                                            Text(" Kaldır", color = MaterialTheme.colorScheme.primary, fontSize = 11.sp)
                                        }
                                    }
                                }
                                AndroidView(
                                    factory = { context -> appWidgetHost.createView(context, id, info).apply { setAppWidget(id, info) } },
                                    modifier = Modifier.fillMaxWidth().heightIn(min = 92.dp, max = 230.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AppDrawerPage(
    activity: ComponentActivity,
    apps: List<LauncherAppInfo>,
    search: String,
    onSearch: (String) -> Unit,
    onBack: () -> Unit,
    onOpenApp: (LauncherAppInfo) -> Unit,
    onLongPressApp: (LauncherAppInfo) -> Unit
) {
    val filtered = remember(apps, search) { filterLauncherApps(apps, search) }
    Column(
        Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing).padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Ana ekrana dön") }
            Column(Modifier.weight(1f)) {
                Text("Uygulamalar", fontSize = 21.sp, fontWeight = FontWeight.SemiBold)
                Text("${apps.size} yüklü uygulama", color = Muted, fontSize = 11.sp)
            }
            Icon(Icons.Default.GridView, null, tint = MaterialTheme.colorScheme.primary)
        }
        Spacer(Modifier.height(12.dp))
        TextField(
            value = search,
            onValueChange = onSearch,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("Uygulama ara…", color = Muted) },
            leadingIcon = { Icon(Icons.Default.Search, null, tint = MaterialTheme.colorScheme.primary) },
            trailingIcon = { if (search.isNotEmpty()) IconButton(onClick = { onSearch("") }) { Icon(Icons.Default.Close, "Aramayı temizle") } },
            shape = RoundedCornerShape(18.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = PanelRaised.copy(alpha = 0.42f),
                unfocusedContainerColor = Panel.copy(alpha = 0.32f),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = MaterialTheme.colorScheme.primary
            )
        )
        Spacer(Modifier.height(12.dp))
        if (filtered.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(if (apps.isEmpty()) "Başlatılabilir uygulama bulunamadı." else "“$search” için sonuç yok.", color = Muted)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 78.dp),
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                items(filtered, key = { it.packageName }) { app ->
                    AppIconTile(
                        activity = activity,
                        app = app,
                        label = true,
                        onClick = { onOpenApp(app) },
                        onLongPress = { onLongPressApp(app) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CommandBar(
    value: String,
    status: String,
    onValueChange: (String) -> Unit,
    onRun: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        Surface(
            color = Panel.copy(alpha = 0.34f),
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.26f))
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 8.dp)) {
                Text(">_", color = MaterialTheme.colorScheme.primary, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                TextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    placeholder = { Text("run Ayarlar  |  ls  |  uname -a  |  term", color = Muted, fontSize = 12.sp) },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                    keyboardActions = KeyboardActions(onGo = { onRun() }),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = MaterialTheme.colorScheme.primary
                    )
                )
                IconButton(onClick = onRun) {
                    Icon(Icons.Default.KeyboardArrowUp, "Komutu çalıştır", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
        Text(
            text = if (status.isBlank()) "run Ayarlar · run instagra · ls · uname -a · term" else status,
            color = if (status.startsWith("Bulunamadı") || status.startsWith("Birden fazla")) Color(0xFFFFC46B) else Muted,
            fontSize = 10.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = 10.dp, top = 4.dp)
        )
    }
}

@Composable
private fun SettingsPage(
    accentIndex: Int,
    transparency: Float,
    dockCount: Int,
    dockLabels: Boolean,
    animations: Boolean,
    onAccent: (Int) -> Unit,
    onTransparency: (Float) -> Unit,
    onDockCount: (Int) -> Unit,
    onDockLabels: (Boolean) -> Unit,
    onAnimations: (Boolean) -> Unit,
    onPickWallpaper: () -> Unit,
    onRestore: () -> Unit,
    onBack: () -> Unit
) {
    Column(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 9.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Geri") }
            Column(Modifier.weight(1f)) {
                Text("Kişiselleştir", fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
                Text("HyprDroid ayarları", color = Muted, fontSize = 11.sp)
            }
            Icon(Icons.Default.Palette, null, tint = MaterialTheme.colorScheme.primary)
        }
        Spacer(Modifier.height(16.dp))
        SettingsSection("VURGU RENGİ", "Hyprland tarzı renk paletinden seç") {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                AccentColors.forEachIndexed { index, color ->
                    val selected = index == accentIndex
                    Box(
                        Modifier.padding(3.dp).size(37.dp).clip(CircleShape)
                            .background(if (selected) color else color.copy(alpha = 0.67f))
                            .combinedClickable(onClick = { onAccent(index) }, onLongClick = { onAccent(index) }),
                        contentAlignment = Alignment.Center
                    ) { if (selected) Icon(Icons.Default.Check, "Seçili vurgu rengi", tint = Background, modifier = Modifier.size(19.dp)) }
                }
            }
        }
        SettingsSection("DUVAR KÂĞIDI", "Bir görsel seç; yalnızca launcher arka planı değişir") {
            Button(onClick = onPickWallpaper, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Wallpaper, null)
                Text("  Galeriden seç")
            }
        }
        SettingsSection("PANEL SAYDAMLIĞI", "Tüm cam paneller · %${((1f - transparency) * 100).toInt()} şeffaf") {
            Slider(value = transparency, onValueChange = onTransparency, valueRange = 0.18f..0.72f)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Daha saydam", color = Muted, fontSize = 10.sp)
                Text("Daha opak", color = Muted, fontSize = 10.sp)
            }
        }
        SettingsSection("DOCK", "Dock’ta gösterilen kısayollar · $dockCount") {
            Slider(value = dockCount.toFloat(), onValueChange = { onDockCount(it.toInt().coerceIn(3, 7)) }, valueRange = 3f..7f, steps = 3)
            SettingSwitch("Uygulama adlarını göster", dockLabels, onDockLabels)
        }
        SettingsSection("HAREKETLER", "Geçişleri tercihine göre ayarla") {
            SettingSwitch("Yumuşak workspace animasyonları", animations, onAnimations)
        }
        SettingsSection("SIFIRLA", "Kişisel düzen ve launcher tercihlerini varsayılana döndür") {
            OutlinedButton(onClick = onRestore, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.RestartAlt, null)
                Text("  Varsayılanlara dön")
            }
        }
        Text("HyprDroid 1.2 · Android HOME launcher", color = Muted, fontSize = 10.sp, modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 4.dp, bottom = 18.dp))
    }
}

@Composable
private fun SettingsSection(title: String, subtitle: String, content: @Composable () -> Unit) {
    Surface(
            color = Panel.copy(alpha = 0.42f),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f)),
        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
    ) {
        Column(Modifier.padding(horizontal = 17.dp, vertical = 15.dp)) {
            Text(title, fontSize = 10.sp, letterSpacing = 1.4.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Text(subtitle, fontSize = 11.sp, color = Muted, modifier = Modifier.padding(top = 3.dp, bottom = 12.dp))
            content()
        }
    }
}

@Composable
private fun SettingSwitch(title: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(title, modifier = Modifier.weight(1f), fontSize = 13.sp)
        Switch(checked = checked, onCheckedChange = onChecked)
    }
}

@Composable
private fun SmallAction(title: String, icon: ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier) {
    TextButton(onClick = onClick, modifier = modifier.defaultMinSize(minHeight = 38.dp)) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
        Text(" $title", color = Color.White, fontSize = 11.sp, maxLines = 1)
    }
}

@Composable
private fun AppIconTile(
    activity: ComponentActivity,
    app: LauncherAppInfo,
    label: Boolean,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    compact: Boolean = false
) {
    val icon: ImageBitmap? = remember(app.packageName) {
        try {
            activity.packageManager.getApplicationIcon(app.packageName).toBitmap(width = 144, height = 144).asImageBitmap()
        } catch (_: Exception) { null }
    }
    Column(
        Modifier.widthIn(min = 58.dp).clip(RoundedCornerShape(18.dp))
            .combinedClickable(onClick = onClick, onLongClick = onLongPress)
            .padding(horizontal = 4.dp, vertical = 5.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (icon != null) {
            Image(icon, contentDescription = app.label, modifier = Modifier.size(if (compact) 38.dp else 46.dp).clip(RoundedCornerShape(13.dp)))
        } else {
            Box(Modifier.size(if (compact) 38.dp else 46.dp).clip(RoundedCornerShape(13.dp)).background(PanelRaised.copy(alpha = 0.52f)).border(BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)), RoundedCornerShape(13.dp)), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Apps, null, tint = MaterialTheme.colorScheme.primary)
            }
        }
        if (label) {
            Text(
                app.label,
                color = Color.White.copy(alpha = 0.88f),
                fontSize = if (compact) 9.sp else 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 5.dp)
            )
        }
    }
}

private fun readBatteryPercent(context: Context): Int = try {
    context.getSystemService(BatteryManager::class.java)
        ?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        ?.coerceIn(0, 100) ?: 0
} catch (_: Exception) { 0 }

private fun findLauncherApps(context: Context): List<LauncherAppInfo> {
    val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
    return try {
        @Suppress("DEPRECATION")
        context.packageManager.queryIntentActivities(intent, 0)
            .map { info -> LauncherAppInfo(info.activityInfo.packageName, info.loadLabel(context.packageManager).toString()) }
            .distinctBy { it.packageName }
            .sortedBy { it.label.lowercase(Locale.ROOT) }
    } catch (_: SecurityException) { emptyList() }
}

internal fun filterLauncherApps(apps: List<LauncherAppInfo>, query: String): List<LauncherAppInfo> =
    apps.filter { query.isBlank() || it.label.contains(query.trim(), ignoreCase = true) }
        .sortedBy { it.label.lowercase(Locale.ROOT) }

internal data class AppCommandResolution(val app: LauncherAppInfo?, val message: String)

internal fun resolveAppCommand(apps: List<LauncherAppInfo>, rawCommand: String): AppCommandResolution {
    val command = rawCommand.trim()
    if (command.equals("help", ignoreCase = true) || command == "?") {
        return AppCommandResolution(null, "Kullanım: run uygulama adı · paket.adı · pkg:paket.adı")
    }
    val target = when {
        command.startsWith("pkg:", ignoreCase = true) || command.startsWith("app:", ignoreCase = true) ->
            command.substringAfter(':').trim()
        else -> command.replace(Regex("^(open|run|launch)\\s+", RegexOption.IGNORE_CASE), "").trim()
    }
    if (target.isBlank()) return AppCommandResolution(null, "Komut boş. Örnek: run Ayarlar")

    apps.firstOrNull { it.packageName.equals(target, ignoreCase = true) }?.let {
        return AppCommandResolution(it, "")
    }

    val exact = apps.filter { it.label.equals(target, ignoreCase = true) }
    if (exact.size == 1) return AppCommandResolution(exact.first(), "")
    if (exact.size > 1) return AppCommandResolution(null, "Birden fazla uygulama eşleşti; paket adını kullan.")

    val partial = apps.filter {
        it.packageName.contains(target, ignoreCase = true) || it.label.contains(target, ignoreCase = true)
    }
    return when (partial.size) {
        1 -> AppCommandResolution(partial.first(), "")
        0 -> AppCommandResolution(null, "Bulunamadı: $target")
        else -> AppCommandResolution(null, "Birden fazla eşleşme (${partial.size}); tam paket adını yaz.")
    }
}

private fun launchApp(context: Context, packageName: String) {
    try {
        val intent = context.packageManager.getLaunchIntentForPackage(packageName) ?: return
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    } catch (_: Exception) { }
}
