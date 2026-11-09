package dev.hyprdroid.launcher

/** A deliberately small, documented Hyprland-inspired config subset understood by HyprDroid. */
data class HyprDroidConfig(
    val accentIndex: Int? = null,
    val transparency: Float? = null,
    val gapDp: Int? = null,
    val borderWidthDp: Float? = null,
    val dockCount: Int? = null,
    val dockLabels: Boolean? = null,
    val animations: Boolean? = null,
    val freeformLaunch: Boolean? = null,
    val workspaceNames: List<String>? = null,
    val bindings: Map<String, String> = emptyMap(),
    /** Package name to zero-based HyprDroid workspace index. */
    val workspaceRules: Map<String, Int> = emptyMap()
)

object HyprDroidConfigParser {
    const val DEFAULT_TEXT = """# HyprDroid config subset; this is not a real Hyprland configuration.
# Save as Documents/HyprDroid/hyprdroid.conf, then reload it in launcher settings.

general {
    accent = cyan
    transparency = 0.34
    gap = 12
    border = 1
    dock_count = 5
    dock_labels = false
    animations = true
    freeform = false
}

workspace {
    names = main, web, term
    # App package rules use 1-based workspace numbers, for example:
    # rule.com.instagram.android = 2
}

bind {
    swipe_up = drawer
    swipe_left = workspace_next
    swipe_right = workspace_prev
    long_press = edit
}
"""

    private val accentIndices = mapOf(
        "lime" to 0,
        "cyan" to 1,
        "pink" to 2,
        "purple" to 3,
        "amber" to 4,
        "mint" to 5
    )

    private val allowedBindings = mapOf(
        "swipe_up" to setOf("drawer", "settings", "edit", "none"),
        "swipe_left" to setOf("workspace_next", "workspace_prev", "drawer", "settings", "none"),
        "swipe_right" to setOf("workspace_next", "workspace_prev", "drawer", "settings", "none"),
        "long_press" to setOf("edit", "drawer", "settings", "none")
    )

    fun parse(text: String): HyprDroidConfig {
        val values = linkedMapOf<String, String>()
        var section = ""

        text.lineSequence().forEach { rawLine ->
            val line = rawLine.trim()
            if (line.isEmpty() || line.startsWith("#") || line.startsWith(";")) return@forEach
            if (line.endsWith("{")) {
                section = line.removeSuffix("{").trim().lowercase()
                return@forEach
            }
            if (line == "}") {
                section = ""
                return@forEach
            }

            val separator = line.indexOf('=')
            if (separator <= 0) return@forEach
            val rawKey = line.substring(0, separator).trim().lowercase()
            val value = line.substring(separator + 1).substringBefore(" #").substringBefore(" ;").trim()
            val key = if (section.isEmpty() || rawKey.startsWith("$section.")) rawKey else "$section.$rawKey"
            values[key] = value
        }

        fun value(vararg keys: String): String? = keys.firstNotNullOfOrNull { values[it] }
        fun boolean(vararg keys: String): Boolean? = when (value(*keys)?.lowercase()) {
            "true", "yes", "1", "on" -> true
            "false", "no", "0", "off" -> false
            else -> null
        }

        val bindings = allowedBindings.mapNotNull { (name, allowed) ->
            val action = values["bind.$name"]?.lowercase()?.takeIf { it in allowed }
            action?.let { name to it }
        }.toMap()

        val workspaceRules = values.mapNotNull { (key, rawValue) ->
            if (!key.startsWith("workspace.rule.")) return@mapNotNull null
            val packageName = key.removePrefix("workspace.rule.").trim()
            val workspaceIndex = rawValue.toIntOrNull()?.minus(1)
            if (packageName.isBlank() || packageName.any(Char::isWhitespace) || workspaceIndex == null || workspaceIndex !in 0..2) {
                null
            } else {
                packageName to workspaceIndex
            }
        }.toMap()

        val names = value("workspace.names", "workspace_names")
            ?.split(',')
            ?.map(String::trim)
            ?.filter(String::isNotEmpty)
            ?.take(3)
            ?.takeIf { it.isNotEmpty() }

        return HyprDroidConfig(
            accentIndex = value("general.accent", "accent")?.lowercase()?.let(accentIndices::get),
            transparency = value("general.transparency", "transparency")?.toFloatOrNull()?.takeIf { it.isFinite() }?.coerceIn(0.18f, 0.72f),
            gapDp = value("general.gap", "gap")?.toIntOrNull()?.coerceIn(0, 40),
            borderWidthDp = value("general.border", "border")?.toFloatOrNull()?.takeIf { it.isFinite() }?.coerceIn(0f, 4f),
            dockCount = value("general.dock_count", "dock_count")?.toIntOrNull()?.coerceIn(3, 7),
            dockLabels = boolean("general.dock_labels", "dock_labels"),
            animations = boolean("general.animations", "animations"),
            freeformLaunch = boolean("general.freeform", "freeform"),
            workspaceNames = names,
            bindings = bindings,
            workspaceRules = workspaceRules
        )
    }
}
