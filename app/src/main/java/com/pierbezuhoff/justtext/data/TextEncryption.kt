package com.pierbezuhoff.justtext.data

import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

private const val AES_GCM_ALGORITHM = "AES/GCM/NoPadding"
private const val DERIVATION_ALGORITHM = "PBKDF2WithHmacSHA256"

private const val PBKDF2_ITERATIONS = 600_000
private const val KEY_LENGTH_BITS = 256
private const val SALT_LENGTH_BYTES = 16 // 128 bits
private const val IV_LENGTH_BYTES = 12 // 96 bits
private const val TAG_LENGTH_BITS = 128

private const val SALT_OFFSET = 0
private const val IV_OFFSET = SALT_LENGTH_BYTES
private const val CIPHERTEXT_OFFSET = IV_OFFSET + IV_LENGTH_BYTES

object TextEncryption {

    private fun deriveKey(
        password: String,
        salt: ByteArray,
    ): SecretKeySpec {
        val keySpec = PBEKeySpec(
            password.toCharArray(),
            salt,
            PBKDF2_ITERATIONS,
            KEY_LENGTH_BITS,
        )
        val keyFactory = SecretKeyFactory.getInstance(DERIVATION_ALGORITHM)
        val secretKey = keyFactory.generateSecret(keySpec)
        return SecretKeySpec(secretKey.encoded, "AES")
    }

    fun encryptWithPassword(
        plainText: String,
        password: String,
    ): ByteArray {
        val random = SecureRandom()
        val salt = ByteArray(SALT_LENGTH_BYTES)
        random.nextBytes(salt)
        val iv = ByteArray(IV_LENGTH_BYTES)
        random.nextBytes(iv)
        val key = deriveKey(password, salt)
        val cipher = Cipher.getInstance(AES_GCM_ALGORITHM)
        cipher.init(Cipher.ENCRYPT_MODE, key,
            GCMParameterSpec(TAG_LENGTH_BITS, iv)
        )
        val plain = plainText.toByteArray()
        val cipherText = cipher.doFinal(plain)
        val output = ByteArray(salt.size + iv.size + cipherText.size)
        salt.copyInto(output, SALT_OFFSET)
        iv.copyInto(output, IV_OFFSET)
        cipherText.copyInto(output, CIPHERTEXT_OFFSET)
        return output
    }

    fun decryptWithPassword(
        encryptedPackage: ByteArray,
        password: String,
    ): String {
        val salt = encryptedPackage.sliceArray(SALT_OFFSET until IV_OFFSET)
        val iv = encryptedPackage.sliceArray(IV_OFFSET until CIPHERTEXT_OFFSET)
        val cipherText = encryptedPackage.sliceArray(CIPHERTEXT_OFFSET until encryptedPackage.size)
        val key = deriveKey(password, salt)
        val cipher = Cipher.getInstance(AES_GCM_ALGORITHM)
        cipher.init(Cipher.DECRYPT_MODE, key,
            GCMParameterSpec(TAG_LENGTH_BITS, iv)
        )
        val plain = cipher.doFinal(cipherText)
        return plain.decodeToString()
    }
}
