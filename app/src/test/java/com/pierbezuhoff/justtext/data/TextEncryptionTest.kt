package com.pierbezuhoff.justtext.data

import com.pierbezuhoff.justtext.byteArrayOf
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeLargerThan
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.string
import io.kotest.property.forAll

class TextEncryptionTest : FunSpec({
    val password = "секрет"
    val salt = byteArrayOf(80, 122, 59, 143, 173, 24, 132, 18, 199, 208, 63, 205, 35, 170, 2, 164)
    val iv = byteArrayOf(146, 151, 218, 108, 164, 43, 152, 103, 29, 215, 203, 90)
    val plainText = "asdjlkffjakldf\n№1!"
    val cipherText = byteArrayOf(36, 2, 225, 168, 198, 189, 65, 6, 226, 107, 15, 125, 226, 249, 24, 109, 64, 115, 216, 199, 3, 188, 236, 250, 92, 172, 91, 66, 1, 146, 239, 255, 75, 204, 238, 141)

    test("encryption coincides with JS implementation") {
        // result of JS implementation
        val key = TextEncryption.deriveKey(password, salt)
        val encryptedPackage = TextEncryption.encrypt(plainText.toByteArray(), key, salt, iv)
        val cipherText1 = encryptedPackage.sliceArray(TextEncryption.CIPHERTEXT_OFFSET until encryptedPackage.size)
        cipherText1 shouldBe cipherText
    }

    test("decryption of JS-encrypted data") {
        val plainText1 = TextEncryption.decryptWithPassword(
            salt + iv + cipherText,
            password
        )
        plainText1 shouldBe plainText
    }

    test("decrypt(encrypt(x)) = x") {
        val plainGen = Arb.string(0, 100)
        val passwordGen = Arb.string(1, 10)
        forAll(10, plainGen, passwordGen) { plainText: String, password: String ->
            val encryptedPackage = TextEncryption.encryptWithPassword(plainText, password)
            val decryptedPlainText = TextEncryption.decryptWithPassword(encryptedPackage, password)
            decryptedPlainText == plainText
        }
        TextEncryption.encryptWithPassword("abc", "111") shouldBeLargerThan byteArrayOf()
    }

    test("encrypt(decrypt(y)) = y") {
        val encryptedPackage = TextEncryption.encrypt(
            plainText.toByteArray(),
            TextEncryption.deriveKey(password, salt),
            salt, iv
        )
        TextEncryption.encrypt(
            TextEncryption.decryptWithPassword(encryptedPackage, password).toByteArray(),
            TextEncryption.deriveKey(password, salt),
            salt, iv
        ) shouldBe encryptedPackage
    }
})

/* js counterpart:

function arrayBufferToBase64(buffer) {
    const bytes = new Uint8Array(buffer);
    const CHUNK = 0x8000;
    const chunks = [];
    for (let i = 0; i < bytes.length; i += CHUNK) {
        const chunk = bytes.subarray(i, i + CHUNK);
        chunks.push(String.fromCharCode.apply(null, chunk));
    }
    return btoa(chunks.join(""));
}

// why this one isn't chunked?
function base64ToArrayBuffer(base64) {
    const binaryString = atob(base64);
    const bytes = new Uint8Array(binaryString.length);
    for (let i = 0; i < binaryString.length; i++) {
        bytes[i] = binaryString.charCodeAt(i);
    }
    return bytes.buffer;
}

// assumes '='-padded base64
function base64ToHex(base64) {
    const raw = atob(base64);
    let result = "";
    for (let i = 0; i < raw.length; i++) {
        const hex = raw.charCodeAt(i).toString(16);
        result += hex.length === 2 ? hex : "0" + hex;
    }
    return result.toUpperCase();
}

// hexStr must be even length (byte = 8 bits = 2^8 = 16*16)
function hexToBase64(hexStr) {
    let base64 = "";
    for (let i = 0; i < hexStr.length; i += 2) {
        base64 += String.fromCharCode(
            parseInt(hexStr.substring(i, i + 2), 16)
        );
    }
    return btoa(base64);
}

const PBKDF2_ITERATIONS = 600_000;
const SALT_LENGTH = 16; // 128 bits
const IV_LENGTH = 12; // 96 bits

const SALT_OFFSET = 0;
const IV_OFFSET = SALT_LENGTH;
const CIPHERTEXT_OFFSET = IV_OFFSET + IV_LENGTH;

// passphrase and plaintext can be any UTF-8
// -> encryptedData = { salt, iv, ciphertext: Uint8Array }
async function encryptWithPassword(passphrase, plaintext) {
    const salt = crypto.getRandomValues(new Uint8Array(SALT_LENGTH));
    const iv = crypto.getRandomValues(new Uint8Array(IV_LENGTH));
    // Derive key
    const baseKey = await crypto.subtle.importKey(
        "raw",
        new TextEncoder().encode(passphrase),
        "PBKDF2",
        false,
        ["deriveKey"]
    );
    const key = await crypto.subtle.deriveKey(
        {
            name: "PBKDF2",
            salt,
            iterations: PBKDF2_ITERATIONS,
            hash: "SHA-256",
        },
        baseKey,
        { name: "AES-GCM", length: 256 },
        false,
        ["encrypt"]
    );
    // Encrypt data
    const ciphertext = await crypto.subtle.encrypt(
        { name: "AES-GCM", iv },
        key,
        new TextEncoder().encode(plaintext) // Unicode -> UTF-8 bytes
    );
    return {
        salt: salt,
        iv: iv,
        ciphertext: new Uint8Array(ciphertext),
    };
}

// encryptedData is supposed to be the same format as encryptWithPassword output:
// encryptedData = { salt, iv, ciphertext: Uint8Array }
// -> plain text
async function decryptWithPassword(passphrase, encryptedData) {
    const salt = encryptedData.salt;
    const iv = encryptedData.iv;
    const ciphertext = encryptedData.ciphertext;
    // Derive key
    const baseKey = await crypto.subtle.importKey(
        "raw",
        new TextEncoder().encode(passphrase),
        "PBKDF2",
        false,
        ["deriveKey"]
    );
    const key = await crypto.subtle.deriveKey(
        {
            name: "PBKDF2",
            salt,
            iterations: PBKDF2_ITERATIONS,
            hash: "SHA-256",
        },
        baseKey,
        { name: "AES-GCM", length: 256 },
        false,
        ["decrypt"]
    );
    // Decrypt data
    const decrypted = await crypto.subtle.decrypt(
        { name: "AES-GCM", iv },
        key,
        ciphertext
    );
    return new TextDecoder().decode(decrypted); // UTF-8 bytes -> Unicode
}

// -> encryptedPackage: base64 string
function packEncryptedData(encryptedData) {
    const { salt, iv, ciphertext } = encryptedData;
    const package = new Uint8Array(
        salt.length + iv.length + ciphertext.length
    );
    package.set(salt, SALT_OFFSET);
    package.set(iv, IV_OFFSET);
    package.set(ciphertext, CIPHERTEXT_OFFSET);
    return arrayBufferToBase64(package.buffer);
}

// -> encryptedData = { salt, iv, ciphertext: Uint8Array }
function parseEncryptedPackage(encryptedPackage) {
    const packageBytes = new Uint8Array(
        base64ToArrayBuffer(encryptedPackage)
    );
    const salt = packageBytes.slice(SALT_OFFSET, IV_OFFSET);
    const iv = packageBytes.slice(IV_OFFSET, CIPHERTEXT_OFFSET);
    const ciphertext = packageBytes.slice(CIPHERTEXT_OFFSET);
    return {
        salt: salt,
        iv: iv,
        ciphertext: ciphertext,
    };
}

 */