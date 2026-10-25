package com.cricsaga.app.ui.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SecurityUtilsTest {

    @Test
    fun testHashPasswordProducesPbkdf2FormatWithSalt() {
        val password = "SuperSecret2026!"
        val hash1 = SecurityUtils.hashPassword(password)
        val hash2 = SecurityUtils.hashPassword(password)

        assertTrue("Hash must start with pbkdf2 prefix", hash1.startsWith("pbkdf2:100000:"))
        // Two hashes of the same password must have different salts
        assertNotEquals("Different calls must generate different salts", hash1, hash2)
        assertFalse(SecurityUtils.isLegacyHash(hash1))
    }

    @Test
    fun testVerifyPasswordWithValidPbkdf2Hash() {
        val password = "MySecurePassword#1"
        val hash = SecurityUtils.hashPassword(password)

        assertTrue(SecurityUtils.verifyPassword(password, hash))
    }

    @Test
    fun testVerifyPasswordWithMismatchedPasswordFails() {
        val correctPassword = "CorrectPassword123"
        val wrongPassword = "WrongPassword123"
        val hash = SecurityUtils.hashPassword(correctPassword)

        assertFalse(SecurityUtils.verifyPassword(wrongPassword, hash))
    }

    @Test
    fun testVerifyPasswordWithBlankInputsFails() {
        assertFalse(SecurityUtils.verifyPassword("", ""))
        assertFalse(SecurityUtils.verifyPassword("validPassword", ""))
        assertFalse(SecurityUtils.verifyPassword("", SecurityUtils.hashPassword("something")))
    }

    @Test
    fun testVerifyPasswordWithLegacySha256BothUpperAndLowerCase() {
        val password = "LegacyPassword999"
        val lowerSha256 = SecurityUtils.hashSha256(password).lowercase()
        val upperSha256 = lowerSha256.uppercase()

        assertEquals(64, lowerSha256.length)
        assertTrue(SecurityUtils.isLegacyHash(lowerSha256))
        assertTrue(SecurityUtils.isLegacyHash(upperSha256))

        // Both lowercase and uppercase legacy hashes must verify successfully
        assertTrue(SecurityUtils.verifyPassword(password, lowerSha256))
        assertTrue(SecurityUtils.verifyPassword(password, upperSha256))
        assertFalse(SecurityUtils.verifyPassword("WrongPassword", upperSha256))
    }

    @Test
    fun testVerifyPasswordWithLegacyPlaintextPassword() {
        val legacyPlaintext = "123456"
        assertTrue(SecurityUtils.isLegacyHash(legacyPlaintext))
        assertTrue(SecurityUtils.verifyPassword("123456", legacyPlaintext))
        assertFalse(SecurityUtils.verifyPassword("wrongpass", legacyPlaintext))
    }
}