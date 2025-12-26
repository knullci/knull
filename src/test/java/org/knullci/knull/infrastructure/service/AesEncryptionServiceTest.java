package org.knullci.knull.infrastructure.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class AesEncryptionServiceTest {

    private AesEncryptionService encryptionService;

    @BeforeEach
    void setUp() {
        encryptionService = new AesEncryptionService();
        // Set the secret key using reflection
        ReflectionTestUtils.setField(encryptionService, "secretKey", "test-secret-key-32-characters-xx");
    }

    @Test
    void testEncrypt_WithValidPlainText_ShouldReturnEncryptedString() {
        // Arrange
        String plainText = "my-secret-password";

        // Act
        String encrypted = encryptionService.encrypt(plainText);

        // Assert
        assertNotNull(encrypted);
        assertNotEquals(plainText, encrypted);
        assertTrue(encrypted.length() > 0);
    }

    @Test
    void testDecrypt_WithValidEncryptedText_ShouldReturnOriginalPlainText() {
        // Arrange
        String originalText = "my-secret-password";
        String encrypted = encryptionService.encrypt(originalText);

        // Act
        String decrypted = encryptionService.decrypt(encrypted);

        // Assert
        assertEquals(originalText, decrypted);
    }

    @Test
    void testEncryptDecrypt_WithEmptyString_ShouldWork() {
        // Arrange
        String emptyText = "";

        // Act
        String encrypted = encryptionService.encrypt(emptyText);
        String decrypted = encryptionService.decrypt(encrypted);

        // Assert
        assertEquals(emptyText, decrypted);
    }

    @Test
    void testEncryptDecrypt_WithSpecialCharacters_ShouldWork() {
        // Arrange
        String specialText = "p@$$w0rd!#$%^&*()_+-=[]{}|;':\",./<>?";

        // Act
        String encrypted = encryptionService.encrypt(specialText);
        String decrypted = encryptionService.decrypt(encrypted);

        // Assert
        assertEquals(specialText, decrypted);
    }

    @Test
    void testEncryptDecrypt_WithUnicodeCharacters_ShouldWork() {
        // Arrange
        String unicodeText = "こんにちは世界 🌍 مرحبا";

        // Act
        String encrypted = encryptionService.encrypt(unicodeText);
        String decrypted = encryptionService.decrypt(encrypted);

        // Assert
        assertEquals(unicodeText, decrypted);
    }

    @Test
    void testEncryptDecrypt_WithLongText_ShouldWork() {
        // Arrange
        String longText = "a".repeat(10000);

        // Act
        String encrypted = encryptionService.encrypt(longText);
        String decrypted = encryptionService.decrypt(encrypted);

        // Assert
        assertEquals(longText, decrypted);
    }

    @Test
    void testEncrypt_SamePlainTextTwice_ShouldProduceSameEncryption() {
        // Arrange
        String plainText = "consistent-encryption";

        // Act
        String encrypted1 = encryptionService.encrypt(plainText);
        String encrypted2 = encryptionService.encrypt(plainText);

        // Assert - AES in ECB mode produces same output for same input
        assertEquals(encrypted1, encrypted2);
    }

    @Test
    void testDecrypt_WithInvalidBase64_ShouldThrowException() {
        // Arrange
        String invalidBase64 = "not-valid-base64!!!";

        // Act & Assert
        assertThrows(RuntimeException.class, () -> encryptionService.decrypt(invalidBase64));
    }

    @Test
    void testDecrypt_WithTamperedEncryptedText_ShouldThrowException() {
        // Arrange
        String plainText = "original-text";
        String encrypted = encryptionService.encrypt(plainText);
        // Tamper with the encrypted text (change a character)
        String tampered = encrypted.substring(0, encrypted.length() - 2) + "XX";

        // Act & Assert
        assertThrows(RuntimeException.class, () -> encryptionService.decrypt(tampered));
    }

    @Test
    void testEncryptDecrypt_WithNewlineCharacters_ShouldWork() {
        // Arrange
        String textWithNewlines = "line1\nline2\r\nline3";

        // Act
        String encrypted = encryptionService.encrypt(textWithNewlines);
        String decrypted = encryptionService.decrypt(encrypted);

        // Assert
        assertEquals(textWithNewlines, decrypted);
    }

    @Test
    void testEncryptDecrypt_WithGithubToken_ShouldWork() {
        // Arrange - Simulate a GitHub personal access token
        String githubToken = "ghp_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx";

        // Act
        String encrypted = encryptionService.encrypt(githubToken);
        String decrypted = encryptionService.decrypt(encrypted);

        // Assert
        assertEquals(githubToken, decrypted);
    }

    @Test
    void testEncrypt_OutputIsBase64Encoded() {
        // Arrange
        String plainText = "test-data";

        // Act
        String encrypted = encryptionService.encrypt(plainText);

        // Assert - Base64 should only contain valid characters
        assertTrue(encrypted.matches("^[A-Za-z0-9+/=]+$"));
    }
}
