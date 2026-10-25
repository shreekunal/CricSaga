package com.cricsaga.app.ui.utils

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Locale
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object SecurityUtils {

    private const val PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA256"
    private const val PBKDF2_ITERATIONS = 100_000
    private const val SALT_BYTE_LENGTH = 16
    private const val KEY_LENGTH_BITS = 256
    private const val PBKDF2_PREFIX = "pbkdf2:"

    private val secureRandom = SecureRandom()

    /**
     * Hashes a raw password string using NIST-recommended PBKDF2 with HMAC-SHA256
     * and a cryptographically secure random 16-byte salt (100,000 iterations).
     * Output format: "pbkdf2:100000:<saltHex>:<hashHex>"
     */
    fun hashPassword(password: String): String {
        if (password.isBlank()) return "oauth_google_verified"
        val salt = ByteArray(SALT_BYTE_LENGTH)
        secureRandom.nextBytes(salt)
        val hash = pbkdf2(password.toCharArray(), salt, PBKDF2_ITERATIONS, KEY_LENGTH_BITS)
        val saltHex = salt.joinToString("") { "%02x".format(it) }
        val hashHex = hash.joinToString("") { "%02x".format(it) }
        return "$PBKDF2_PREFIX$PBKDF2_ITERATIONS:$saltHex:$hashHex"
    }

    /**
     * Computes legacy unsalted SHA-256 for backward compatibility checks.
     */
    fun hashSha256(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(password.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Verifies an input password against a stored hash (PBKDF2 or legacy SHA-256) in constant time.
     * Supports case-insensitive matching for legacy hex hashes without leaking timing information.
     */
    fun verifyPassword(rawInput: String, storedHashOrPlain: String): Boolean {
        if (storedHashOrPlain.isBlank() || rawInput.isBlank()) return false

        // 1. Modern PBKDF2 salted hash format: "pbkdf2:<iterations>:<saltHex>:<hashHex>"
        if (storedHashOrPlain.startsWith(PBKDF2_PREFIX)) {
            val parts = storedHashOrPlain.split(":")
            if (parts.size == 4) {
                val iterations = parts[1].toIntOrNull() ?: return false
                val saltHex = parts[2]
                val expectedHashHex = parts[3].lowercase(Locale.ROOT)
                val salt = hexToBytes(saltHex) ?: return false

                val computedHash = pbkdf2(rawInput.toCharArray(), salt, iterations, KEY_LENGTH_BITS)
                val computedHashHex = computedHash.joinToString("") { "%02x".format(it) }

                return MessageDigest.isEqual(
                    computedHashHex.toByteArray(Charsets.UTF_8),
                    expectedHashHex.toByteArray(Charsets.UTF_8)
                )
            }
        }

        // 2. Legacy SHA-256 hash (64 hex characters) with case-insensitive normalization
        if (storedHashOrPlain.length == 64 && storedHashOrPlain.all { it in "0123456789abcdefABCDEF" }) {
            val inputHashHex = hashSha256(rawInput)
            val storedLowerHex = storedHashOrPlain.lowercase(Locale.ROOT)
            return MessageDigest.isEqual(
                inputHashHex.toByteArray(Charsets.UTF_8),
                storedLowerHex.toByteArray(Charsets.UTF_8)
            )
        }

        // 3. Backward compatibility for legacy plaintext passwords: constant-time comparison
        val rawInputBytes = rawInput.toByteArray(Charsets.UTF_8)
        val storedBytes = storedHashOrPlain.toByteArray(Charsets.UTF_8)
        return MessageDigest.isEqual(rawInputBytes, storedBytes)
    }

    /**
     * Determines whether a stored credential string requires re-hashing to modern PBKDF2.
     */
    fun isLegacyHash(storedHashOrPlain: String): Boolean {
        return !storedHashOrPlain.startsWith(PBKDF2_PREFIX)
    }

    private fun pbkdf2(password: CharArray, salt: ByteArray, iterations: Int, keyLengthBits: Int): ByteArray {
        val spec = PBEKeySpec(password, salt, iterations, keyLengthBits)
        val skf = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM)
        return skf.generateSecret(spec).encoded
    }

    private fun hexToBytes(hex: String): ByteArray? {
        if (hex.length % 2 != 0) return null
        return try {
            ByteArray(hex.length / 2) { i ->
                hex.substring(i * 2, i * 2 + 2).toInt(16).toByte()
            }
        } catch (_: Exception) {
            null
        }
    }
}