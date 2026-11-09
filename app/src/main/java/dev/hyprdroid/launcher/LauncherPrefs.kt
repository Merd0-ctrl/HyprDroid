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

    var gapDp: Int
        get() = prefs.getInt("gap_dp", 12).coerceIn(0, 40)
        set(value) { prefs.edit().putInt("gap_dp", value.coerceIn(0, 40)).apply() }

    var borderWidthDp: Float
        get() = prefs.getFloat("border_width_dp", 1f).coerceIn(0f, 4f)
        set(value) { prefs.edit().putFloat("border_width_dp", value.coerceIn(0f, 4f)).apply() }

    var workspaceNames: List<String>
        get() {
            val names = (prefs.getString("workspace_names", "1,2,3") ?: "1,2,3")
                .split(',').map(String::trim).filter(String::isNotEmpty)
            return (0 until WORKSPACE_COUNT).map { names.getOrNull(it) ?: (it + 1).toString() }
        }
        set(value) {
            val names = (0 until WORKSPACE_COUNT).map { index -> value.getOrNull(index)?.trim().takeUnless { it.isNullOrEmpty() } ?: (index + 1).toString() }
            prefs.edit().putString("workspace_names", names.joinToString(",")).apply()
        }

    var workspaceRules: Map<String, Int>
        get() = (prefs.getStringSet("workspace_rules", emptySet()) ?: emptySet()).mapNotNull { entry ->
            val separator = entry.lastIndexOf('=')
            if (separator <= 0) return@mapNotNull null
            val packageName = entry.substring(0, separator)
            val workspace = entry.substring(separator + 1).toIntOrNull()?.minus(1)
            if (packageName.isBlank() || workspace == null || workspace !in 0 until WORKSPACE_COUNT) null else packageName to workspace
        }.toMap()
        set(value) {
            val entries = value.mapNotNull { (packageName, workspace) ->
                if (packageName.isBlank() || workspace !in 0 until WORKSPACE_COUNT) null else "$packageName=${workspace + 1}"
            }.toSet()
            prefs.edit().putStringSet("workspace_rules", entries).apply()
        }

    var configUri: String?
        get() = prefs.getString("config_uri", null)
        set(value) { prefs.edit().putString("config_uri", value).apply() }

    fun binding(name: String): String {
        val default = DEFAULT_BINDINGS[name] ?: "none"
        return prefs.getString("bind_$name", default)?.takeIf { it in ALLOWED_BINDINGS[name].orEmpty() } ?: default
    }

    fun setBinding(name: String, action: String) {
        if (action in ALLOWED_BINDINGS[name].orEmpty()) prefs.edit().putString("bind_$name", action).apply()
    }

    fun resetBindingsToDefaults() {
        val editor = prefs.edit()
        DEFAULT_BINDINGS.forEach { (name, action) -> editor.putString("bind_$name", action) }
        editor.apply()
    }

    fun workspaceName(index: Int): String = workspaceNames.getOrElse(index) { (index + 1).toString() }

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

    companion object {
        const val WORKSPACE_COUNT = 3
        private val DEFAULT_BINDINGS = mapOf(
            "swipe_up" to "drawer",
            "swipe_left" to "workspace_next",
            "swipe_right" to "workspace_prev",
            "long_press" to "edit"
        )
        private val ALLOWED_BINDINGS = mapOf(
            "swipe_up" to setOf("drawer", "settings", "edit", "none"),
            "swipe_left" to setOf("workspace_next", "workspace_prev", "drawer", "settings", "none"),
            "swipe_right" to setOf("workspace_next", "workspace_prev", "drawer", "settings", "none"),
            "long_press" to setOf("edit", "drawer", "settings", "none")
        )
    }
}
