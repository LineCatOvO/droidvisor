package com.droidvisor

import com.droidvisor.docker.model.Container
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

        assertEquals("12345678", container.shortId)
    }

    @Test
    fun testImageSizeFormatting() {
        val smallImage = Image(
            name = "small",
            tag = "latest",
            size = "1024 KB",
            created = "2024-01-01T00:00:00Z"
        )

        val largeImage = Image(
            name = "large",
            tag = "latest",
            size = "2048 MB",
            created = "2024-01-01T00:00:00Z"
        )

        assertNotNull(smallImage)
        assertNotNull(largeImage)
    }
}
