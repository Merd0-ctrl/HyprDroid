package dev.hyprdroid.launcher

import android.content.Context

class LauncherPrefs(context: Context) {
    private val prefs = context.getSharedPreferences("hyprdroid", Context.MODE_PRIVATE)

    init {
        val hadPriorPreferences = prefs.all.isNotEmpty()
        val styleVersion = prefs.getInt("ui_style_version", 0)
        val update = prefs.edit()
        if (styleVersion < 2) update.putFloat("transparency", 0.34f)
        if (styleVersion < 3) {
            update.putInt("ui_style_version", 3)
            if (hadPriorPreferences) update.putBoolean("setup_completed", true)
        }
        update.apply()
    }

    var setupCompleted: Boolean
        get() = prefs.getBoolean("setup_completed", false)
        set(value) { prefs.edit().putBoolean("setup_completed", value).apply() }

    var accentIndex: Int
        get() = prefs.getInt("accent", 1)
        set(value) { prefs.edit().putInt("accent", value.coerceIn(0, 5)).apply() }

    var transparency: Float
        get() = prefs.getFloat("transparency", 0.34f).coerceIn(0.18f, 0.72f)
        set(value) { prefs.edit().putFloat("transparency", value.coerceIn(0.18f, 0.72f)).apply() }

    var dockCount: Int
        get() = prefs.getInt("dock_count", 5)
        set(value) { prefs.edit().putInt("dock_count", value.coerceIn(3, 7)).apply() }

    var showDockLabels: Boolean
        get() = prefs.getBoolean("dock_labels", false)
        set(value) { prefs.edit().putBoolean("dock_labels", value).apply() }

    var animationsEnabled: Boolean
        get() = prefs.getBoolean("animations", true)
        set(value) { prefs.edit().putBoolean("animations", value).apply() }

    var freeformLaunchEnabled: Boolean
        get() = prefs.getBoolean("freeform_launch", false)
        set(value) { prefs.edit().putBoolean("freeform_launch", value).apply() }

    var wallpaperUri: String?
        get() = prefs.getString("wallpaper_uri", null)
        set(value) { prefs.edit().putString("wallpaper_uri", value).apply() }

    var lastWorkspace: Int
        get() = prefs.getInt("workspace", 0).coerceIn(0, WORKSPACE_COUNT - 1)
        set(value) { prefs.edit().putInt("workspace", value.coerceIn(0, WORKSPACE_COUNT - 1)).apply() }

    fun shortcuts(workspace: Int): Set<String> =
        prefs.getStringSet("shortcuts_$workspace", emptySet())?.toSet() ?: emptySet()

    fun addShortcut(workspace: Int, packageName: String) {
        val next = shortcuts(workspace).toMutableSet().apply { add(packageName) }
        prefs.edit().putStringSet("shortcuts_$workspace", next).apply()
    }

    fun removeShortcut(workspace: Int, packageName: String) {
        val next = shortcuts(workspace).toMutableSet().apply { remove(packageName) }
        prefs.edit().putStringSet("shortcuts_$workspace", next).apply()
    }

    fun widgetIds(workspace: Int): List<Int> =
        prefs.getStringSet("widgets_$workspace", emptySet())
            ?.mapNotNull(String::toIntOrNull)?.toList() ?: emptyList()

    fun addWidget(workspace: Int, id: Int) {
        val next = widgetIds(workspace).map(Int::toString).toMutableSet().apply { add(id.toString()) }
        prefs.edit().putStringSet("widgets_$workspace", next).apply()
    }

    fun removeWidget(workspace: Int, id: Int) {
        val next = widgetIds(workspace).map(Int::toString).toMutableSet().apply { remove(id.toString()) }
        prefs.edit().putStringSet("widgets_$workspace", next).apply()
    }

    fun restoreDefaults() {
        prefs.edit().clear().putInt("ui_style_version", 3).putBoolean("setup_completed", true).apply()
    }

    companion object { const val WORKSPACE_COUNT = 3 }
}
