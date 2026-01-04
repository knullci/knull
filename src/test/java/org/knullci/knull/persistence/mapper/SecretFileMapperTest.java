package org.knullci.knull.persistence.mapper;

import org.junit.jupiter.api.Test;
import org.knullci.knull.domain.model.SecretFile;

import static org.junit.jupiter.api.Assertions.*;

class SecretFileMapperTest {

    @Test
    void testToEntity_WithValidDomainModel_ShouldMapCorrectly() {
        // Arrange
        SecretFile domainModel = new SecretFile();
        domainModel.setId(123L);
        domainModel.setName("test-secret");
        domainModel.setEncryptedContent("secret-content");
        domainModel.setDescription("Test description");

        // Act
        org.knullci.knull.persistence.entity.SecretFile entity = SecretFileMapper.toEntity(domainModel);

        // Assert
        assertNotNull(entity);
        assertEquals(123L, entity.getId());
        assertEquals("test-secret", entity.getName());
        assertEquals("secret-content", entity.getEncryptedContent());
        assertEquals("Test description", entity.getDescription());
    }

    @Test
    void testToEntity_WithNullValues_ShouldHandleGracefully() {
        // Arrange
        SecretFile domainModel = new SecretFile();
        domainModel.setId(null);
        domainModel.setName(null);
        domainModel.setEncryptedContent(null);
        domainModel.setDescription(null);

        // Act
        org.knullci.knull.persistence.entity.SecretFile entity = SecretFileMapper.toEntity(domainModel);

        // Assert
        assertNotNull(entity);
        assertNull(entity.getId());
        assertNull(entity.getName());
        assertNull(entity.getEncryptedContent());
        assertNull(entity.getDescription());
    }

    @Test
    void testFromEntity_WithValidEntity_ShouldMapCorrectly() {
        // Arrange
        org.knullci.knull.persistence.entity.SecretFile entity = new org.knullci.knull.persistence.entity.SecretFile(
                456L,
                "prod-secret",
                "Production secret",
                "FILE",
                "production-content",
                null,
                null);

        // Act
        SecretFile domainModel = SecretFileMapper.fromEntity(entity);

        // Assert
        assertNotNull(domainModel);
        assertEquals(456L, domainModel.getId());
        assertEquals("prod-secret", domainModel.getName());
        assertEquals("production-content", domainModel.getEncryptedContent());
        assertEquals("Production secret", domainModel.getDescription());
    }

    @Test
    void testFromEntity_WithNullEntity_ShouldReturnNull() {
        // Act
        SecretFile domainModel = SecretFileMapper.fromEntity(null);

        // Assert
        assertNull(domainModel);
    }

    @Test
    void testRoundTrip_ShouldPreserveData() {
        // Arrange
        SecretFile original = new SecretFile();
        original.setId(789L);
        original.setName("roundtrip-secret");
        original.setEncryptedContent("roundtrip-content");
        original.setDescription("Roundtrip test");

        // Act
        org.knullci.knull.persistence.entity.SecretFile entity = SecretFileMapper.toEntity(original);
        SecretFile result = SecretFileMapper.fromEntity(entity);

        // Assert
        assertEquals(original.getId(), result.getId());
        assertEquals(original.getName(), result.getName());
        assertEquals(original.getEncryptedContent(), result.getEncryptedContent());
        assertEquals(original.getDescription(), result.getDescription());
    }
}
