package dev.hyprdroid.launcher.ui

import dev.hyprdroid.launcher.TermuxBridge
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LauncherAppFilterTest {
    private val apps = listOf(
        LauncherAppInfo("pkg.z", "Zeta"),
        LauncherAppInfo("pkg.b", "Beta"),
        LauncherAppInfo("pkg.a", "Alpha")
    )

    @Test
    fun `empty query returns all apps alphabetically`() {
        assertEquals(
            listOf("Alpha", "Beta", "Zeta"),
            filterLauncherApps(apps, "").map { it.label }
        )
    }

    @Test
    fun `query matches labels without case sensitivity`() {
        assertEquals(
            listOf("Beta"),
            filterLauncherApps(apps, "bEt").map { it.label }
        )
    }

    @Test
    fun `query with only whitespace returns all apps`() {
        assertEquals(3, filterLauncherApps(apps, "   ").size)
    }

    @Test
    fun `package code resolves exact package`() {
        assertEquals("pkg.b", resolveAppCommand(apps, "pkg:pkg.b").app?.packageName)
    }

    @Test
    fun `open command resolves app label`() {
        assertEquals("pkg.b", resolveAppCommand(apps, "open beta").app?.packageName)
    }

    @Test
    fun `run command resolves localized settings label`() {
        val installed = apps + LauncherAppInfo("com.android.settings", "Ayarlar")
        assertEquals("com.android.settings", resolveAppCommand(installed, "run Ayarlar").app?.packageName)
    }

    @Test
    fun `run command resolves a partial Instagram label`() {
        val installed = apps + LauncherAppInfo("com.instagram.android", "Instagram")
        assertEquals("com.instagram.android", resolveAppCommand(installed, "run instagra").app?.packageName)
    }

    @Test
    fun `unknown app code returns a useful result`() {
        val result = resolveAppCommand(apps, "pkg:missing.package")
        assertEquals(null, result.app)
        assertEquals("Bulunamadı: missing.package", result.message)
    }

    @Test
    fun `ambiguous partial code does not launch an arbitrary app`() {
        val overlapping = apps + LauncherAppInfo("pkg.beta.calendar", "Beta Calendar")
        val result = resolveAppCommand(overlapping, "bet")
        assertEquals(null, result.app)
        assertEquals(true, result.message.startsWith("Birden fazla"))
    }

    @Test
    fun `linux command opens Debian prompt after executing`() {
        val script = TermuxBridge.buildProotScript("uname -a")
        assertTrue(script.startsWith("proot-distro login debian -- /bin/bash -lc "))
        assertTrue(script.contains("uname -a\nexec /bin/bash -i"))
    }

    @Test
    fun `single quotes in linux commands are escaped for Termux shell`() {
        val script = TermuxBridge.buildProotScript("printf '%s' 'hello'")
        assertTrue(script.contains("'\\''"))
        assertTrue(script.contains("%s"))
        assertTrue(script.contains("hello"))
    }
}
