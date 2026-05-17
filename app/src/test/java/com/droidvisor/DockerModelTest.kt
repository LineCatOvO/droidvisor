package com.droidvisor

import com.droidvisor.docker.model.Container
import com.droidvisor.docker.model.ContainerStats
import com.droidvisor.docker.model.DockerInfo
import com.droidvisor.docker.model.Image
import org.junit.Test
import org.junit.Assert.*

class DockerModelTest {

    @Test
    fun testContainerShortId() {
        val container = Container(
            id = "1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef",
            name = "test-container",
            image = "ubuntu:latest",
            status = "running",
            created = System.currentTimeMillis(),
            ports = listOf("8080:80")
        )

        assertEquals("1234567890ab", container.shortId)
    }

    @Test
    fun testContainerDisplayStatus() {
        val runningContainer = Container(
            id = "1",
            name = "test1",
            image = "ubuntu",
            status = "running",
            created = 0
        )
        assertEquals("运行中", runningContainer.displayStatus)

        val stoppedContainer = Container(
            id = "2",
            name = "test2",
            image = "ubuntu",
            status = "exited",
            created = 0
        )
        assertEquals("已停止", stoppedContainer.displayStatus)
    }

    @Test
    fun testImageFullName() {
        val image = Image(
            name = "ubuntu",
            tag = "latest",
            size = "1024 KB",
            created = "2024-01-01T00:00:00Z"
        )
        assertEquals("ubuntu:latest", image.fullName)
    }

    @Test
    fun testContainerStats() {
        val stats = ContainerStats(
            cpuPercent = 50.5f,
            memoryPercent = 25.0f,
            memoryUsage = 1024 * 1024,
            memoryLimit = 4L * 1024 * 1024 * 1024,
            networkRx = 0,
            networkTx = 0
        )
        assertEquals(50.5f, stats.cpuPercent)
    }

    @Test
    fun testDockerInfo() {
        val info = DockerInfo(
            containersTotal = 10,
            containersRunning = 5,
            containersPaused = 2,
            containersStopped = 3,
            imagesTotal = 20,
            serverVersion = "20.10.0",
            memoryTotal = 16L * 1024 * 1024 * 1024,
            memoryUsed = 8L * 1024 * 1024 * 1024,
            cpus = 8
        )
        assertEquals(10, info.containersTotal)
        assertEquals(50.0f, info.memoryPercent, 0.1f)
    }
}
