package com.cricsaga.app.ui.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class UpdateManagerTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun testCalculateFileSha256ComputesAccurateDigest() {
        val testFile = tempFolder.newFile("sample_update.apk")
        testFile.writeText("CricSaga-OTA-Sample-Payload-Verification-2026")

        val expectedSha256 = "bb9c08ea35e0d9ad4743a1e07e96bd07e16ac555faa3fa6a3a06476be0d5afeb"
        val computedSha256 = UpdateManager.calculateFileSha256(testFile)

        assertEquals(expectedSha256.lowercase(), computedSha256.lowercase())
    }

    @Test
    fun testCorruptedFileGeneratesDifferentHash() {
        val validFile = tempFolder.newFile("valid.apk")
        validFile.writeText("Original Trusted APK binary bytes")

        val corruptedFile = tempFolder.newFile("corrupted.apk")
        corruptedFile.writeText("Tampered / altered binary bytes")

        val validHash = UpdateManager.calculateFileSha256(validFile)
        val corruptedHash = UpdateManager.calculateFileSha256(corruptedFile)

        assertNotEquals(validHash, corruptedHash)
    }

    @Test
    fun testAppUpdateInfoPreservesSha256() {
        val info = AppUpdateInfo(
            latestVersionCode = 20,
            latestVersionName = "2.0.0",
            downloadUrl = "https://example.com/cricsaga.apk",
            changelog = "Security update",
            isForceUpdate = false,
            isUpdateAvailable = true,
            sha256Checksum = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
        )

        assertEquals("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855", info.sha256Checksum)
        assertTrue(info.isUpdateAvailable)
    }
}
