package com.nebulabook.cloudos

import com.nebulabook.cloudos.data.ssh.ProvisioningScripts
import com.nebulabook.cloudos.domain.model.desktopApps
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProvisioningScriptsTest {
    @Test fun everyDeckItemHasAProvisioningCommand() {
        assertEquals(7, desktopApps.size)
        desktopApps.forEach { app ->
            assertTrue("Missing install command for ${app.name}", ProvisioningScripts.installCommands.containsKey(app.name))
        }
    }

    @Test fun bootScriptIncludesDesktopReadinessAndContainerPort() {
        val script = ProvisioningScripts.BOOT_WEBTOP_SCRIPT
        assertTrue(script.contains("NEBULABOOK_READY"))
        assertTrue(script.contains("-p 3000:3000"))
        assertTrue(script.contains("lscr.io/linuxserver/webtop:ubuntu-xfce"))
    }
}
