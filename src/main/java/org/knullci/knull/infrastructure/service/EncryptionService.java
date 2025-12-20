package org.knullci.knull.infrastructure.service;

public interface EncryptionService {
    String encrypt(String plainText);
    
    String decrypt(String encryptedText);
}
