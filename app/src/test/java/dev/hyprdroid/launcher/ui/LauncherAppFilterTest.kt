package dev.hyprdroid.launcher.ui

import dev.hyprdroid.launcher.TermuxBridge
import dev.hyprdroid.launcher.HyprDroidConfigParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
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

    @Test
    fun `cpu usage calculates the busy proportion between samples`() {
        assertEquals(50, cpuUsagePercent(CpuSnapshot(100, 70), CpuSnapshot(200, 120)))
    }

    @Test
    fun `cpu usage reports fully busy and idle samples`() {
        assertEquals(100, cpuUsagePercent(CpuSnapshot(100, 80), CpuSnapshot(200, 80)))
        assertEquals(0, cpuUsagePercent(CpuSnapshot(100, 80), CpuSnapshot(200, 180)))
    }

    @Test
    fun `cpu usage returns null when counters do not advance`() {
        assertNull(cpuUsagePercent(CpuSnapshot(100, 80), CpuSnapshot(100, 80)))
    }

    @Test
    fun `freeform bounds are centered and stay inside the display`() {
        assertEquals(FreeformBounds(70, 160, 930, 840), centeredFreeformBounds(1000, 1000))
    }

    @Test
    fun `freeform bounds reject invalid display sizes`() {
        assertNull(centeredFreeformBounds(0, 1000))
        assertNull(centeredFreeformBounds(1000, -1))
    }

    @Test
    fun `hyprdroid config parses appearance bindings and workspace rules`() {
        val config = HyprDroidConfigParser.parse(
            """general {
                accent = purple
                transparency = 0.42
                gap = 16
                border = 2
                dock_count = 6
                freeform = true
            }
            workspace {
                names = main, web, term
                rule.com.example.mail = 2
            }
            bind {
                swipe_up = settings
                swipe_left = workspace_prev
                long_press = drawer
            }"""
        )
        assertEquals(3, config.accentIndex)
        assertEquals(0.42f, config.transparency ?: -1f, 0.001f)
        assertEquals(16, config.gapDp)
        assertEquals(2f, config.borderWidthDp ?: -1f, 0.001f)
        assertEquals(6, config.dockCount)
        assertEquals(true, config.freeformLaunch)
        assertEquals(listOf("main", "web", "term"), config.workspaceNames)
        assertEquals(1, config.workspaceRules["com.example.mail"])
        assertEquals("settings", config.bindings["swipe_up"])
        assertEquals("workspace_prev", config.bindings["swipe_left"])
        assertEquals("drawer", config.bindings["long_press"])
    }

    @Test
    fun `hyprdroid config clamps values and ignores unsafe or invalid rules`() {
        val config = HyprDroidConfigParser.parse(
            """general {
                gap = 500
                border = -2
                dock_count = 99
            }
            workspace {
                rule.com.example.bad = 9
            }
            bind {
                swipe_up = execute_shell
            }"""
        )
        assertEquals(40, config.gapDp)
        assertEquals(0f, config.borderWidthDp ?: -1f, 0.001f)
        assertEquals(7, config.dockCount)
        assertTrue(config.workspaceRules.isEmpty())
        assertTrue(config.bindings.isEmpty())
    }
}
