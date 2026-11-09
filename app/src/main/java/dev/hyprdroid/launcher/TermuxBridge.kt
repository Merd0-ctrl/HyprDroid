package dev.hyprdroid.launcher

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager

internal object TermuxBridge {
    const val PACKAGE_NAME = "com.termux"
    const val RUN_COMMAND_PERMISSION = "com.termux.permission.RUN_COMMAND"
    private const val PREFIX = "/data/data/com.termux/files"
    private const val ACTION_RUN_COMMAND = "com.termux.RUN_COMMAND"

    @Suppress("DEPRECATION")
    fun isInstalled(context: Context): Boolean = try {
        context.packageManager.getPackageInfo(PACKAGE_NAME, 0)
        true
    } catch (_: PackageManager.NameNotFoundException) {
        false
    }

    fun createIntent(command: String?): Intent {
        val prootScript = buildProotScript(command)
        return Intent(ACTION_RUN_COMMAND).apply {
            component = ComponentName(PACKAGE_NAME, "com.termux.app.RunCommandService")
            putExtra("com.termux.RUN_COMMAND_PATH", "$PREFIX/usr/bin/bash")
            putExtra("com.termux.RUN_COMMAND_ARGUMENTS", arrayOf("-lc", prootScript))
            putExtra("com.termux.RUN_COMMAND_WORKDIR", "$PREFIX/home")
            putExtra("com.termux.RUN_COMMAND_BACKGROUND", false)
            putExtra("com.termux.RUN_COMMAND_SESSION_ACTION", "0")
            putExtra("com.termux.RUN_COMMAND_LABEL", "HyprDroid Linux")
            putExtra("com.termux.RUN_COMMAND_DESCRIPTION", "Komutu Debian PRoot userland ortamında açar.")
        }
    }

    fun buildProotScript(command: String?): String {
        if (command.isNullOrBlank()) return "exec proot-distro login debian"
        val interactiveCommand = "$command\nexec /bin/bash -i"
        return "proot-distro login debian -- /bin/bash -lc ${shellQuote(interactiveCommand)}"
    }

    private fun shellQuote(value: String): String = "'" + value.replace("'", "'\\''") + "'"
}
