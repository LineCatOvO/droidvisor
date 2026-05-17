package com.droidvisor

import com.droidvisor.vm.VmConfig
import org.junit.Test
import org.junit.Assert.*

class VmConfigTest {

    @Test
    fun testDefaultVmConfig() {
        val config = VmConfig(
            id = "test-vm",
            name = "Test VM"
        )

        assertEquals("test-vm", config.id)
        assertEquals("Test VM", config.name)
        assertTrue(config.memoryMb > 0)
    }

    @Test
    fun testCustomVmConfig() {
        val config = VmConfig(
            id = "custom-vm",
            name = "Custom VM",
            memoryMb = 2048,
            cpuCores = 4
        )

        assertEquals(2048, config.memoryMb)
        assertEquals(4, config.cpuCores)
    }
}
